package com.klpj.blueplanet.model.services;


import com.klpj.blueplanet.model.dao.*;
import com.klpj.blueplanet.model.dto.*;
import com.klpj.blueplanet.model.responses.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class GameService {

    @Autowired
    private UserStatusDao userStatusDao;

    @Autowired
    private EndingDao endingDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private ChoiceDao choiceDao;

    @Autowired
    private SpecialEventDao specialEventDao;

    @Autowired
    private SpecialEventConditionDao specialEventConditionDao;

    @Autowired
    private UserChoiceHistoryDao userChoiceHistoryDao;

    @Autowired
    private GptService gptService;

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private List<SpecialEventCondition> cachedConditions; // 특별 이벤트 조건들을 미리 캐싱해두기 위한 변수

    private Random random = new Random();

    /**
     * 새로운 게임을 시작합니다.
     * - 초기 게임 상태를 생성 (모든 수치 50, turnCount 1)
     * - 프롤로그 데이터(첫 번째 Prologue)를 조회
     * - prologue에서는 nextEvent를 1로 설정 (상시 이벤트)
     */
    public StartGameResponse startNewGame() {
        // 초기 게임 상태 생성
        UserStatus status = new UserStatus();
        status.setAir(50);
        status.setWater(50);
        status.setBiology(50);
        status.setPopularity(50);
        status.setTurnCount(1);

        UserStatus savedStatus = userStatusDao.save(status);

        return new StartGameResponse(savedStatus, 2);
    }

    // 모든 특별 이벤트 발동 조건 미리 메모리에 로딩
    @Bean
    public ApplicationRunner loadSpecialEventConditionsAfterStartup() {
        return args -> {
            try {
                cachedConditions = specialEventConditionDao.findAll();
                System.out.println("✅ 캐싱된 조건 수: " + cachedConditions.size());
                logger.info("✅ 캐싱된 조건 수: {}", cachedConditions.size());
            } catch (Exception e) {
                System.err.println("❌ 캐싱 실패: " + e.getMessage());
                logger.error("❌ 캐싱 실패", e);
                e.printStackTrace(); // 에러 로그 확인
            }
        };
    }


    public List<SpecialEvent> getTriggeredSpecialEvents(UserStatus status) {
        // 1. 조건들을 이벤트 ID 기준으로 그룹핑 (Map<special_event_id, 조건리스트>)
        Map<Long, List<SpecialEventCondition>> groupedConditions = cachedConditions.stream()
                .collect(Collectors.groupingBy(cond -> cond.getSpecialEvent().getId()));

        // 2. 조건을 모두 만족하는 이벤트 ID 추출
        List<Long> satisfiedEventIds = groupedConditions.entrySet().stream()
                .filter(entry -> allConditionsMatch(entry.getValue(), status))
                .map(Map.Entry::getKey)
                .filter(eventId -> !status.getUsedSpecialEventIds().contains(eventId))
                .collect(Collectors.toList());


        // 3. DB에서 해당 이벤트들 조회
        return specialEventDao.findAllById(satisfiedEventIds);
    }

    private boolean allConditionsMatch(List<SpecialEventCondition> conditions, UserStatus status) {
        return conditions.stream().allMatch(cond -> {
            int currentValue = switch (cond.getStatusType().toLowerCase().trim()) {
                case "air" -> status.getAir();
                case "water" -> status.getWater();
                case "biology" -> status.getBiology(); // 혼용 대비
                case "popularity" -> status.getPopularity();
                default -> 0;
            };

            return switch (cond.getOperator().trim()) {
                case ">" -> currentValue > cond.getVariation();
                case ">=" -> currentValue >= cond.getVariation();
                case "<" -> currentValue < cond.getVariation();
                case "<=" -> currentValue <= cond.getVariation();
                case "==" -> currentValue == cond.getVariation();
                default -> false;
            };
        });
    }


    // 조건을 만족하는 특별 이벤트 가져오는 메서드
    public SpecialEventResponse triggerSpecialEventIfAny(Long userId) {
        UserStatus userStatus = userStatusDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SpecialEvent> triggered = getTriggeredSpecialEvents(userStatus);
        if (triggered.isEmpty()) {
            throw new RuntimeException("No special event triggered.");
        }

        triggered.sort(new Comparator<SpecialEvent>() {
            @Override
            public int compare(SpecialEvent o1, SpecialEvent o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        SpecialEvent event = triggered.get(0); // 하나만 처리

        // 수치 반영 (turnCount는 변경 ❌)
        userStatus.setAir(userStatus.getAir() + event.getAirImpact());
        userStatus.setWater(userStatus.getWater() + event.getWaterImpact());
        userStatus.setBiology(userStatus.getBiology() + event.getBiologyImpact());
        userStatus.setPopularity(userStatus.getPopularity() + event.getPopularityImpact());

        userStatus.getUsedSpecialEventIds().add(event.getId());
        userStatusDao.save(userStatus);

        int nextEvent = determineNextEventType(userStatus);
        return new SpecialEventResponse(event, userStatus, nextEvent);
    }


    /**
     * 사용자의 userId를 기반으로, 아직 제공되지 않은 이벤트 중 하나를 랜덤으로 선택한 후,
     * 해당 이벤트 정보를 포함한 NextEventResponse를 반환합니다.
     *
     * @param userId 사용자의 ID (쿼리 파라미터로 전달)
     * @return NextEventResponse DTO
     */
    public NextEventResponse getNextEvent(Long userId) {
        // UserStatus(또는 UserData) 조회
        UserStatus userStatus = userStatusDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 모든 이벤트 조회
        List<Event> allEvents = eventDao.findAll();
        // 아직 사용되지 않은 이벤트 필터링 (UserStatus에 저장된 usedEventIds를 기준으로)
        List<Event> availableEvents = allEvents.stream()
                .filter(event -> !userStatus.getUsedEventIds().contains(event.getId()))
                .toList();

        if (availableEvents.isEmpty()) {
            throw new RuntimeException("No available event found.");
        }
        // 랜덤으로 이벤트 선택
        Event selectedEvent = availableEvents.get(random.nextInt(availableEvents.size()));

        // 이벤트 정보를 EventResponse DTO로 생성
        EventResponse eventResponse = new EventResponse(
                selectedEvent.getId(),
                selectedEvent.getTitle(),
                selectedEvent.getContent(),
                selectedEvent.getWriter()
        );

        // 선택지 단순화: Choice를 ChoiceSimpleResponse로 매핑 (id와 text만)
        List<ChoiceSimpleResponse> choiceResponses = selectedEvent.getChoices()
                .stream()
                .map(choice -> new ChoiceSimpleResponse(choice.getId(), choice.getContent()))
                .collect(Collectors.toList());

        // NextEventResponse를 생성하여 반환 (턴 수나 nextEvent 값은 FE에서 로직으로 결정)
        return new NextEventResponse(eventResponse, choiceResponses);
    }

    /**
     * 사용자의 userStatusId와 선택된 choiceId를 기반으로 게임 상태를 업데이트하고,
     * 엔딩 조건을 판단하여 엔딩 ID(1~10)를 결정한 후,
     * GameUpdateResponse DTO로 업데이트된 UserStatus와 엔딩 ID를 반환합니다.
     * <p>
     * 엔딩 조건:
     * - 어느 한 수치 (air, water, biology, popularity)가 100 이상이거나 0 이하인 경우:
     * * air high → endingId = 1
     * * air low  → endingId = 2
     * * water high → endingId = 3
     * * water low  → endingId = 4
     * * biology high → endingId = 5
     * * biology low  → endingId = 6
     * * popularity high → endingId = 7
     * * popularity low  → endingId = 8
     * <p>
     * - 만약 위 조건에 해당하지 않고, turnCount가 20 이상일 경우:
     * * 네 수치의 평균이 50 이상이면 → endingId = 9
     * * 네 수치의 평균이 50 미만이면  → endingId = 10
     * <p>
     * - 조건을 충족하지 않으면 endingId는 0 (정상 진행)입니다.
     *
     * @param userStatusId 사용자 상태의 ID
     * @param choiceId     선택된 선택지의 ID
     * @return GameUpdateResponse DTO (업데이트된 UserStatus와 결정된 endingId)
     */
    public GameUpdateResponse processChoice(Long userStatusId, Long choiceId) {
        // 1. UserStatus 조회
        UserStatus userStatus = userStatusDao.findById(userStatusId)
                .orElseThrow(() -> new RuntimeException("User status not found with id: " + userStatusId));

        // 2. 선택지 조회
        Choice choice = choiceDao.findById(choiceId)
                .orElseThrow(() -> new RuntimeException("Choice not found with id: " + choiceId));

        // 3. 선택지 효과 반영
        userStatus.setAir(userStatus.getAir() + choice.getAirImpact());
        userStatus.setWater(userStatus.getWater() + choice.getWaterImpact());
        userStatus.setBiology(userStatus.getBiology() + choice.getBiologyImpact());
        userStatus.setPopularity(userStatus.getPopularity() + choice.getPopularityImpact());
        userStatus.setTurnCount(userStatus.getTurnCount() + 1);

        // 4. 업데이트된 상태 저장
        UserStatus updatedStatus = userStatusDao.save(userStatus);

        // 5. 선택 이력 기록 (새로 추가된 부분)
        // 선택한 이벤트의 ID는 선택지(choice)가 속한 이벤트의 ID를 통해 확인할 수 있습니다.
        UserChoiceHistory history = new UserChoiceHistory();
        history.setUserStatusId(userStatusId);
        history.setEventId(choice.getEvent().getId());
        history.setChoiceId(choiceId);
        // history.setChosenAt(new Date()); // 기본값 생성자로 이미 현재 시각이 할당됨

        // 선택된 이벤트 ID를 사용된 이벤트 목록에 추가 및 저장
        userStatus.getUsedEventIds().add(choice.getEvent().getId());
        userStatusDao.save(userStatus);

        userChoiceHistoryDao.save(history);  // UserChoiceHistoryDao를 주입 받아 사용합니다.

        // 다음 이벤트 타입 판단
        int nextEventType = determineNextEventType(updatedStatus);

        return new GameUpdateResponse(updatedStatus, choice.getResult(), nextEventType);
    }

    // 다음 이벤트 판별하는 메서드
    public int determineNextEventType(UserStatus userStatus) {
        // 1. 엔딩 조건
        if (userStatus.getAir() >= 100) return 3;
        if (userStatus.getAir() <= 0) return 3;
        if (userStatus.getWater() >= 100) return 3;
        if (userStatus.getWater() <= 0) return 3;
        if (userStatus.getBiology() >= 100) return 3;
        if (userStatus.getBiology() <= 0) return 3;
        if (userStatus.getPopularity() >= 100) return 3;
        if (userStatus.getPopularity() <= 0) return 3;

        if (userStatus.getTurnCount() >= 20) {
            int sum = userStatus.getAir() + userStatus.getWater() +
                    userStatus.getBiology() + userStatus.getPopularity();
            int average = sum / 4;
            return 3; // 엔딩으로 이동 (엔딩 ID는 따로 판단할 수 있음)
        }

        // 2. 특별 이벤트 조건
        if (!getTriggeredSpecialEvents(userStatus).isEmpty()) {
            return 2;
        }

        // 3. 상시 이벤트
        return 1;
    }

    // 어떤 엔딩이벤트를 반환해야하는지 판단하는 메서드
    public int determineEndingId(UserStatus userStatus) {
        if (userStatus.getAir() <= 0) return 1;
        if (userStatus.getAir() >= 100) return 2;
        if (userStatus.getWater() <= 0) return 3;
        if (userStatus.getWater() >= 100) return 4;
        if (userStatus.getBiology() <= 0) return 5;
        if (userStatus.getBiology() >= 100) return 6;
        if (userStatus.getPopularity() <= 0) return 7;
        if (userStatus.getPopularity() >= 100) return 8;

        if (userStatus.getTurnCount() >= 20) {
            int sum = userStatus.getAir() + userStatus.getWater() +
                    userStatus.getBiology() + userStatus.getPopularity();
            int average = sum / 4;
            return (average >= 50) ? 9 : 10;
        }

        return 0; // 0이면 엔딩 조건 미충족
    }


    /**
     * DB에서 저장된 엔딩 중 하나를 랜덤으로 선택하여 반환합니다.
     */
    public Ending getEnding() {
        List<Ending> endings = endingDao.findAll();
        if (endings.isEmpty()) {
            throw new RuntimeException("No ending found.");
        }
        return endings.get(random.nextInt(endings.size()));
    }

    public String summarizeUserFlow(Long userId) {
        List<UserChoiceHistory> historyList = userChoiceHistoryDao.findByUserStatusIdOrderByChosenAtAsc(userId);

        StringBuilder sb = new StringBuilder();
        sb.append(
                "당신은 게임의 스토리텔러입니다. 아래 선택 내용을 기반으로 두 가지를 작성해주세요:\n\n" +
                        "1. 플레이어 성향을 창의적으로 한 문장으로 요약하세요.\n" +
                        "- 출력 형식: 반드시 다음 형식으로 작성 (예시 아님):\n" +
                        "  1. \"당신은 ○○한 사람입니다.\"\n\n" +
                        "- 반드시 **한 가지 특징**만 사용하여 작성하세요.\n" +
                        "- '그리고', '및', '하고', '또는', '같은 복수 표현'은 사용하지 마세요.\n" +
                        "- 예: \"당신은 책임감 있는 사람입니다.\" ← ✅ OK\n" +
                        "      \"당신은 창의적이고 책임감 있는 사람입니다.\" ← ❌ 금지"+
                        "2. 플레이어의 선택을 바탕으로 몰입이 가능한 엔딩 장면처럼 분석된 문단을 작성해주세요.\n" +
                        "- 분량: 250자 이상 300자 이하\n" +
                        "- 반드시 서론 없이 **본론부터 시작**하세요.\n" +
                        "- 아래 표현은 **절대 사용하지 마세요**:\n" +
                        "  - '사용자의 선택 이력:', '선택 이력:', '사용자 27의 선택 이력:', '사용자', '이 선택은...', '분석해보겠습니다', '이러한 선택들은...'\n" +
                        "  - '번호가 붙은 사용자 지칭', 예: '사용자 12', '플레이어 1' 등의 표현도 금지합니다.\n"+
                        "- 예시처럼 시작해주세요: \n" +
                        "  - \"당신은 ○○한 정책을 도입하셔서~\"\n" +
                        "  - \"○○을 추진함으로써 도시 환경을 개선하셨습니다.\"\n" +
                        "- 문장은 반드시 '당신'으로 시작하고, 전부 **격식을 갖춘 존댓말**을 사용해야 합니다.\n" +
                        "- 전체 응답은 반드시 **'하십시오체' 문체(격식 높임체)**를 사용하여 작성하세요.\n" +
                        "- '~하셨습니다', '~해주셨습니다', '~하였습니다'처럼 **격식을 갖춘 종결어미**만 사용 가능합니다.\n" +
                        "- '~했다', '~하였다', '~했어요' 등은 절대 사용하지 마세요 (해체, 해요체 금지)." +
                        "- 예: '기여하셨습니다', '개선하셨습니다', '도움을 주셨습니다', '살렸습니다' 등의 존댓말로 끝나야 합니다.\n\n" +
                        "📌 최종 출력 형식:\n" +
                        "1. \"당신은 ○○한 사람입니다.\"\n" +
                        "2. [바로 본론 시작, 서론 없이, 전부 존댓말로]\n\n"+
                        "- 💥 위 조건을 어길 경우 잘못된 응답으로 간주됩니다. 반드시 지침을 철저히 지켜서 작성해주세요."
        );

        sb.append("사용자 ").append(userId).append("의 선택 이력 :\n\n");

        for (UserChoiceHistory history : historyList) {
            Event event = eventDao.findById(history.getEventId()).orElse(null);
            Choice choice = choiceDao.findById(history.getChoiceId()).orElse(null);


            String eventTitle = event != null ? event.getTitle() : "알 수 없는 이벤트";
            String choiceText = choice != null ? choice.getContent() : "알 수 없는 선택지";

            sb.append("이벤트 : ").append(eventTitle).append("\n")
                    .append("선택 : ").append(choiceText).append("\n\n");
        }
        return sb.toString();
    }

}