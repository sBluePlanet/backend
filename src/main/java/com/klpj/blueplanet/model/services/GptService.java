package com.klpj.blueplanet.model.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class GptService {

    @Value("${openai.api-key:}")
    private String apiKey;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("❌ GPT API 키가 설정되지 않았습니다.");
        } else {
            System.out.println("✅ GPT API Key: " + apiKey);
        }
    }

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String getSummaryFromGpt(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity("https://api.openai.com/v1/chat/completions", request, Map.class);

        Map message = (Map) ((Map) ((List<Object>) response.getBody().get("choices")).get(0)).get("message");
        return message.get("content").toString();
    }


}
