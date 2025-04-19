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

@RestController
@RequestMapping("/game")
public class GameController {

    private static final Logger gameLogger = LoggerFactory.getLogger("gameLog");

    @Autowired
    private GameService gameService;

    @Autowired
    private UserStatusDao userStatusDao;

    @Autowired
    private EndingDao endingDao;

    @GetMapping("/start")
    public ResponseEntity<StartGameResponse> startGame() {
        StartGameResponse response = gameService.startNewGame();
        Long userId = response.getUserStatus().getUserId();
        String timestamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        String gameLogFile = userId + "_" + timestamp;
        MDC.put("gameLogFile", gameLogFile);
        try {
            gameLogger.info("Game started for user {} with log file identifier {}.", userId, gameLogFile);
            return ResponseEntity
                    .ok()
                    .header("Game-Log-File", gameLogFile)
                    .body(response);
        } finally {
            MDC.remove("gameLogFile");
        }
    }

    @GetMapping("/common")
    public ResponseEntity<NextEventResponse> getNextEvent(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "gameLogFile", required = false) String gameLogFile
    ) {
        if (gameLogFile != null && !gameLogFile.isEmpty()) {
            MDC.put("gameLogFile", gameLogFile);
        }
        try {
            NextEventResponse resp = gameService.getNextEvent(userId);
            gameLogger.info("User {} received event {}: {}. Available choices: {}.",
                    userId,
                    resp.getEvent().getEventId(),
                    resp.getEvent().getTitle(),
                    resp.getChoices());
            return ResponseEntity.ok(resp);
        } finally {
            MDC.remove("gameLogFile");
        }
    }

    @GetMapping("/special")
    public ResponseEntity<SpecialEventResponse> triggerSpecialEvent(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "gameLogFile", required = false) String gameLogFile
    ) {
        if (gameLogFile != null && !gameLogFile.isEmpty()) {
            MDC.put("gameLogFile", gameLogFile);
        }
        try {
            SpecialEventResponse resp = gameService.triggerSpecialEventIfAny(userId);
            gameLogger.info("User {} triggered special event: {}.", userId, resp.getTitle());
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            gameLogger.info("User {} did not trigger any special event. Reason: {}", userId, e.getMessage());
            return ResponseEntity.noContent().build();
        } finally {
            MDC.remove("gameLogFile");
        }
    }

    @PostMapping("/choice")
    public ResponseEntity<GameUpdateResponse> processChoice(
            @RequestBody ChoiceRequest request,
            @RequestParam(value = "gameLogFile", required = false) String gameLogFile
    ) {
        if (gameLogFile != null && !gameLogFile.isEmpty()) {
            MDC.put("gameLogFile", gameLogFile);
        }
        try {
            GameUpdateResponse resp = gameService.processChoice(request.getUserStatusId(), request.getChoiceId());
            gameLogger.info("User {} selected choice {}. Updated status: {}.",
                    request.getUserStatusId(),
                    request.getChoiceId(),
                    resp.getUserStatus());
            return ResponseEntity.ok(resp);
        } finally {
            MDC.remove("gameLogFile");
        }
    }

    @GetMapping("/ending")
    public ResponseEntity<Ending> getEndingEvent(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "gameLogFile", required = false) String gameLogFile
    ) {
        if (gameLogFile != null && !gameLogFile.isEmpty()) {
            MDC.put("gameLogFile", gameLogFile);
        }
        try {
            // UserStatus 엔티티 가져오기
            UserStatus status = userStatusDao.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            // endingId 결정
            int endingId = gameService.determineEndingId(status);
            if (endingId == 0) {
                gameLogger.warn("User {} does not meet any ending condition. Skipping ending event.", userId);
                return ResponseEntity.noContent().build();
            }
            // Ending 조회
            Ending ending = endingDao.findById((long) endingId)
                    .orElseThrow(() -> new RuntimeException("Ending not found with id: " + endingId));
            gameLogger.info("User {} triggered ending {}: {} (imgUrl={}).",
                    userId, ending.getId(), ending.getTitle(), ending.getImgUrl());
            return ResponseEntity.ok(ending);
        } finally {
            MDC.remove("gameLogFile");
        }
    }

}
