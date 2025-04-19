package com.klpj.blueplanet.model.services;

import com.klpj.blueplanet.model.dao.*;
import com.klpj.blueplanet.model.dto.*;
import com.klpj.blueplanet.model.responses.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataService {

    @Autowired
    private TooltipDao tooltipDao;

    @Autowired
    private UserStatusDao userStatusDao;

    @Autowired
    private UserChoiceHistoryDao userChoiceHistoryDao;

    @Autowired
    private ChoiceDao choiceDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private SpecialEventDao specialEventDao;

    @Autowired
    private AdviceEmailDao adviceEmailDao;

    /**
     * 모든 툴팁 정보를 TooltipResponse DTO 형태로 반환합니다.
     */
    public List<TooltipResponse> getAllTooltips() {
        return tooltipDao.findAll().stream()
                .map(t -> new TooltipResponse(t.getKeyword(), t.getContent()))
                .collect(Collectors.toList());
    }

    /**
     * 사용자(userId)가 이미 수신한 이메일 목록(이벤트 요약: eventId, title)을 EmailSummaryResponse 형태로 반환합니다.
     */
    public List<EmailSummaryResponse> getEmailList(Long userId) {
        UserStatus userStatus = userStatusDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 1. 이벤트 메일: UserChoiceHistory 기준
        List<UserChoiceHistory> histories = userChoiceHistoryDao.findByUserStatusId(userId);
        List<EmailSummaryResponse> eventMails = histories.stream()
                .map(h -> {
                    Event e = eventDao.findById(h.getEventId()).orElse(null);
                    if (e == null) return null;
                    return new EmailSummaryResponse(
                            e.getId(),
                            e.getTitle(),
                            "event",
                            h.getCreatedAt()  // 사용자 기준 수신 시간
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 2. 조언 메일
        List<AdviceEmail> adviceEmails = adviceEmailDao.findByUserId(userId);
        List<EmailSummaryResponse> adviceMails = adviceEmails.stream()
                .map(a -> new EmailSummaryResponse(
                        a.getId(),
                        a.getTitle(),
                        "advice",
                        a.getCreatedAt()
                ))
                .collect(Collectors.toList());

        // 3. 합치고 정렬 (최신순)
        List<EmailSummaryResponse> merged = new ArrayList<>();
        merged.addAll(eventMails);
        merged.addAll(adviceMails);

        merged.sort(Comparator.comparing(EmailSummaryResponse::getCreatedAt).reversed());

        return merged;
    }


    /**
     * 사용자(userId)와 이벤트(eventId)를 받아 해당 이메일(이벤트) 상세 정보를 반환합니다.
     * 추가로, 사용자가 해당 이벤트에서 선택했던 선택지 정보가 있으면 함께 포함합니다.
     */
    public EmailDetailResponse getEmailDetail(Long userId, Long id, String type) {
        if (type.equals("advice")) {
            // 조언 메일 상세 조회
            AdviceEmail advice = adviceEmailDao.findById(id)
                    .orElseThrow(() -> new RuntimeException("조언 메일을 찾을 수 없습니다."));
            return new EmailDetailResponse(
                    advice.getEventId(),
                    advice.getTitle(),
                    "과학자 박병호", // 또는 "AI 조언"
                    advice.getContent(),
                    List.of(), // 선택지 없음
                    null
            );
        } else if (type.equals("event")) {
            // 일반 이벤트 메일 상세 조회
            Event event = eventDao.findById(id)
                    .orElseThrow(() -> new RuntimeException("이벤트를 찾을 수 없습니다."));

            List<ChoiceSimpleResponse> choices = event.getChoices()
                    .stream()
                    .map(c -> new ChoiceSimpleResponse(c.getId(), c.getContent()))
                    .collect(Collectors.toList());

            List<UserChoiceHistory> histories = userChoiceHistoryDao.findByUserStatusIdAndEventId(userId, id);

            ChoiceSimpleResponse selectedChoice = null;
            if (!histories.isEmpty()) {
                UserChoiceHistory history = histories.get(0);
                Choice choice = choiceDao.findById(history.getChoiceId()).orElse(null);
                if (choice != null) {
                    selectedChoice = new ChoiceSimpleResponse(choice.getId(), choice.getContent());
                }
            }

            return new EmailDetailResponse(
                    event.getId(),
                    event.getTitle(),
                    event.getWriter(),
                    event.getContent(),
                    choices,
                    selectedChoice
            );
        }

        // 그 외 타입이면 예외 처리
        throw new IllegalArgumentException("알 수 없는 type: " + type);
    }

    /**
     * 사용자(userId)가 수신한 특수 이벤트(뉴스) 목록을 NewsSummaryResponse 형태로 반환합니다.
     */
    public List<NewsSummaryResponse> getNewsList(Long userId) {
        UserStatus userStatus = userStatusDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Set<Long> usedSpecialEventIds = userStatus.getUsedSpecialEventIds();
        List<SpecialEvent> specialEvents = specialEventDao.findAllById(usedSpecialEventIds);

        return specialEvents.stream()
                .map(se -> new NewsSummaryResponse(se.getId(), se.getTitle()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 뉴스(특수 이벤트) ID를 받아, 상세 정보를 NewsDetailResponse 형태로 반환합니다.
     */
    public NewsDetailResponse getNewsDetail(Long specialEventId) {
        SpecialEvent specialEvent = specialEventDao.findById(specialEventId)
                .orElseThrow(() -> new RuntimeException("Special event not found with id: " + specialEventId));
        return new NewsDetailResponse(
                specialEvent.getId(),
                specialEvent.getTitle(),
                specialEvent.getContent(),
                specialEvent.getImgUrl()
        );
    }
}
