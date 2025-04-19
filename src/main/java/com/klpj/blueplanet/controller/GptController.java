package com.klpj.blueplanet.controller;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.klpj.blueplanet.model.services.GameService;
import com.klpj.blueplanet.model.services.GptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gpt")
public class GptController {
    @Autowired
    private GptService gptService;

    @Autowired
    private GameService gameService;

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

            // 결과를 JSON 으로 반환
            Map<String, String> response = Map.of("title", reTitle, "content", advice);
            return ResponseEntity.ok(response);
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("content", "‼️조언 요청 중 오류 발생"));
        }
    }

}
