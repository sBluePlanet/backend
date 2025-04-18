package com.klpj.blueplanet.controller;

import com.klpj.blueplanet.model.dao.EndingDao;
import com.klpj.blueplanet.model.dto.Ending;
import com.klpj.blueplanet.model.requests.ChoiceRequest;
import com.klpj.blueplanet.model.responses.GameUpdateResponse;
import com.klpj.blueplanet.model.responses.NextEventResponse;
import com.klpj.blueplanet.model.responses.StartGameResponse;
import com.klpj.blueplanet.model.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GameController는 게임 진행에 필요한 엔드포인트를 제공합니다.
 * - /api/game/start: 게임 시작 시, 초기 게임 상태와 프롤로그를 반환
 * - /api/game/next: 상시 이벤트(일반 이벤트)를 랜덤으로 조회하여 반환
 * - /api/game/choice: 선택지를 처리하여 게임 상태를 업데이트한 후, 업데이트된 상태와 nextEvent 값을 반환
 * - /api/game/ending: 저장된 엔딩 중 하나를 랜덤으로 조회하여 반환
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private EndingDao endingDao;

    @GetMapping("/start")
    public ResponseEntity<StartGameResponse> startGame() {
        StartGameResponse response = gameService.startNewGame();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/next")
    public ResponseEntity<NextEventResponse> getNextEvent(@RequestParam("userId") Long userId) {
        NextEventResponse response = gameService.getNextEvent(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/choice")
    public ResponseEntity<GameUpdateResponse> processChoice(@RequestBody ChoiceRequest request) {
        GameUpdateResponse response = gameService.processChoice(request.getUserStatusId(), request.getChoiceId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ending")
    public ResponseEntity<Ending> getEnding(@RequestParam("endingId") int endingId) {
        // EndingDao를 이용해 해당 endingId를 가진 엔딩 정보를 DB에서 조회
        Ending ending = endingDao.findById((long) endingId)
                .orElseThrow(() -> new RuntimeException("Ending not found with id: " + endingId));
        return ResponseEntity.ok(ending);
    }
}