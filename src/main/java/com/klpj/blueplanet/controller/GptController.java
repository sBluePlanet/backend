package com.klpj.blueplanet.controller;

import com.klpj.blueplanet.model.dao.*;
import com.klpj.blueplanet.model.dto.*;
import com.klpj.blueplanet.model.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gpt")
public class GptController {
    @Autowired
    private GptService gptService;

    @Autowired
    private GameService gameService;

    @Autowired
    private AdviceEmailDao adviceEmailDao;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, String>> summarizeUserFlow(@RequestParam Long userId){

        try {
            // 선택 흐름 요약 텍스트 생성
            String userFlow = gameService.summarizeUserFlow(userId);

            // GPT API 호출하여 요약 요청
            String gptSummary = gptService.sendPrompt(userFlow);

            // 결과를 JSON 형태로 반환
            Map<String, String> response = new HashMap<>();
            response.put("content", gptSummary);

            return ResponseEntity.ok(response);
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("content", "요약 중 오류 발생"));
        }

    }

    @GetMapping("/advice")
    public ResponseEntity<Map<String, String>> askAdvice(
            @RequestParam Long userId,
            @RequestParam Long eventId,
            @RequestParam String title,
            @RequestParam String content) {

        try{
            //GPT에게 조언 요청
            String advice = gptService.askAdvice(userId, eventId, title, content);

            String reTitle = "RE:" + title;

            // 응답 저장
            AdviceEmail adviceEmail = new AdviceEmail();
            adviceEmail.setUserId(userId);
            adviceEmail.setEventId(eventId);
            adviceEmail.setTitle(reTitle);
            adviceEmail.setContent(advice);
            adviceEmailDao.save(adviceEmail);

            // 결과를 JSON 으로 반환
            Map<String, String> response = Map.of("title", reTitle, "content", advice);
            return ResponseEntity.ok(response);

        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("content", "‼️조언 요청 중 오류 발생"));
        }
    }

}
