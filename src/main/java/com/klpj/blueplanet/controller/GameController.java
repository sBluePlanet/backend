package com.klpj.blueplanet.controller;

import com.klpj.blueplanet.model.dao.*;
import com.klpj.blueplanet.model.dto.*;
import com.klpj.blueplanet.model.requests.*;
import com.klpj.blueplanet.model.responses.*;
import com.klpj.blueplanet.model.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * GameController는 게임 진행에 필요한 엔드포인트를 제공합니다.
 * - /game/start: 게임 시작 시, 초기 게임 상태와 프롤로그를 반환하며, 게임 로그 식별자를 MDC에 설정합니다.
 * - /game/common: 상시 이벤트(일반 이벤트)를 랜덤으로 조회하여 반환 (요청 시 게임 로그 식별자 전달)
 * - /game/choice: 선택지를 처리하여 게임 상태를 업데이트한 후 로그를 남깁니다.
 * - /game/ending: 저장된 엔딩 중 하나를 조회하며 로그에 기록합니다.
 */
@RestController
@RequestMapping("/game")
public class GameController {

    // gameLog 전용 로거 (logback-spring.xml의 SiftingAppender 기반 로그)
    private static final Logger gameLogger = LoggerFactory.getLogger("gameLog");

    @Autowired
    private GameService gameService;

    @Autowired
    private EndingDao endingDao;

    @Autowired
    private UserStatusDao userStatusDao;


    /**
     * 게임 시작 시 고유한 로그 식별자를 생성하고 MDC에 설정합니다.
     * 이 식별자는 클라이언트에 전달되어 이후 모든 요청에 포함되어야 합니다.
     */
    @GetMapping("/start")
    public ResponseEntity<StartGameResponse> startGame() {
        StartGameResponse response = gameService.startNewGame();
        Long userId = response.getUserStatus().getUserId();
        String timestamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        String gameLogFileName = userId + "_" + timestamp;

        // 게임 시작 시 MDC에 고유 식별자 설정
        MDC.put("gameLogFile", gameLogFileName);

        gameLogger.info("Game started for user {} with log file identifier {}.", userId, gameLogFileName);
        // 클라이언트에 gameLogFile 값을 함께 전달하여, 이후 요청 시 해당 값을 보내도록 함.
        // 예를 들어 StartGameResponse에 필드를 추가하거나, HTTP 헤더로 전달할 수 있음.
        return ResponseEntity.ok(response);
    }

    /**
     * /common 엔드포인트는 클라이언트에서 gameLogFile 식별자를 쿼리 파라미터 혹은 헤더로 함께 전송한다고 가정합니다.
     * 여기서 MDC를 재설정하여 해당 게임 세션의 로그 파일에 기록되도록 합니다.
     */
    @GetMapping("/common")
    public ResponseEntity<NextEventResponse> getNextEvent(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "gameLogFile", required = false) String gameLogFile) {
        // 만약 요청에 gameLogFile 값이 포함되어 있다면 MDC에 설정
        if (gameLogFile != null && !gameLogFile.isEmpty()) {
            MDC.put("gameLogFile", gameLogFile);
        }
        NextEventResponse response = gameService.getNextEvent(userId);

        String eventId = response.getEvent() != null ? String.valueOf(response.getEvent().getEventId()) : "N/A";
        String eventTitle = response.getEvent() != null ? response.getEvent().getTitle() : "N/A";
        gameLogger.info("User {} received event {}: {}. Available choices: {}.",
                userId, eventId, eventTitle, response.getChoices());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/special")
    public ResponseEntity<SpecialEventResponse> triggerSpecialEvent(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "gameLogFile", required = false) String gameLogFile) {
        // MDC 값 재설정
        if (gameLogFile != null && !gameLogFile.isEmpty()) {
            MDC.put("gameLogFile", gameLogFile);
        }
        try {
            SpecialEventResponse response = gameService.triggerSpecialEventIfAny(userId);
            // 수정: SpecialEventResponse의 getTitle()을 이용하여 제목을 기록
            gameLogger.info("User {} triggered special event: {}.", userId, response.getTitle());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            gameLogger.info("User {} did not trigger any special event. Reason: {}", userId, e.getMessage());
            return ResponseEntity.noContent().build();
        }
    }



    /**
     * /choice 엔드포인트에서도 클라이언트가 gameLogFile 식별자를 함께 보내야 합니다.
     * 이를 바탕으로 MDC를 재설정하고 로그를 기록합니다.
     */
    @PostMapping("/choice")
    public ResponseEntity<GameUpdateResponse> processChoice(
            @RequestBody ChoiceRequest request,
            @RequestParam(value = "gameLogFile", required = false) String gameLogFile) {
        if (gameLogFile != null && !gameLogFile.isEmpty()) {
            MDC.put("gameLogFile", gameLogFile);
        }

        GameUpdateResponse response = gameService.processChoice(request.getUserStatusId(), request.getChoiceId());

        gameLogger.info("User {} selected choice {}. Updated status: {}.",
                request.getUserStatusId(), request.getChoiceId(), response.getUserStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * /ending 엔드포인트 역시 gameLogFile 식별자를 이용하여 MDC를 설정합니다.
     */
    @GetMapping("/ending")
    public ResponseEntity<Ending> getEndingEvent(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "gameLogFile", required = false) String gameLogFile) {

        if (gameLogFile != null && !gameLogFile.isEmpty()) {
            MDC.put("gameLogFile", gameLogFile);
        }

        // 1. 사용자 상태 조회
        UserStatus userStatus = userStatusDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 2. 엔딩 조건 판단
        int endingId = gameService.determineEndingId(userStatus);

        if (endingId == 0) {
            gameLogger.warn("User {} does not meet any ending condition. Skipping ending event.", userId);
            return ResponseEntity.noContent().build(); // or throw error
        }

        // 3. 엔딩 데이터 조회
        Ending ending = endingDao.findById((long) endingId)
                .orElseThrow(() -> new RuntimeException("Ending not found with id: " + endingId));

        // 4. 로깅
        gameLogger.info("User {} triggered ending {}: {}",
                userId, ending.getId(), ending.getTitle());

        // 5. 응답 반환
        return ResponseEntity.ok(ending);
    }
}
