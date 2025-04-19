package com.klpj.blueplanet.model.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klpj.blueplanet.model.dao.ChoiceDao;
import com.klpj.blueplanet.model.dao.EventDao;
import com.klpj.blueplanet.model.dao.UserStatusDao;
import com.klpj.blueplanet.model.dto.Choice;
import com.klpj.blueplanet.model.dto.Event;
import com.klpj.blueplanet.model.dto.UserStatus;
import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GptService {

    @Autowired
    private UserStatusDao userStatusDao;

    @Autowired
    private ChoiceDao choiceDao;

    @Autowired
    private EventDao eventDao;

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

    public String sendPrompt(String prompt) {
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

    public String askAdvice(Long userId, Long eventId, String title, String content){
        // 사용자 상태 조회
        UserStatus userStatus = userStatusDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 이벤트 정보 조회
        Event event = eventDao.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // 선택지 조회
        List<Choice> choices = choiceDao.findByEventId(eventId);

        // 프롬프트 구성
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 환경 문제에 대한 조언을 해주는 가상의 과학자입니다.\n");
        sb.append("사용자의 현재 상태는 다음과 같습니다.\n")
                .append("대기: ").append(userStatus.getAir()).append(", ")
                .append("수질: ").append(userStatus.getWater()).append(", ")
                .append("생물: ").append(userStatus.getBiology()).append(", ")
                .append("지지도: ").append(userStatus.getPopularity()).append("\n\n");

        sb.append("현재 이벤트는 다음과 같습니다.\n")
                .append("제목: ").append(event.getTitle()).append("\n")
                .append(event.getContent()).append("\n\n");

        sb.append("선택지는 다음과 같습니다:\n");
        int idx = 1;
        for (Choice choice : choices) {
            sb.append(idx++).append(") ").append(choice.getContent()).append("\n")
                    .append(" - 대기: ").append(choice.getAirImpact())
                    .append(", 수질: ").append(choice.getWaterImpact())
                    .append(", 생물: ").append(choice.getBiologyImpact())
                    .append(", 지지도: ").append(choice.getPopularityImpact()).append("\n");
        }

        sb.append("\n")
                .append("질문: ").append(title).append("\n")
                .append(content).append("\n\n");

        sb.append("위 정보를 바탕으로 사용자에게 가장 적절한 선택이 무엇인지 조언해 주세요. 과학적인 근거도 함께 설명해 주세요.");

        // 5. GPT 호출
        return sendPrompt(sb.toString());

    }


}
