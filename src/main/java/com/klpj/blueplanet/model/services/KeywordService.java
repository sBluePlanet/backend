package com.klpj.blueplanet.model.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KeywordService는 스토리 텍스트 내에서 ^로 감싼 키워드를 추출하여,
 * 해당 키워드에 대응하는 툴팁(설명) 정보를 반환합니다.
 */
@Service
public class KeywordService {

    private final Map<String, String> tooltipDictionary = new HashMap<>();

    public KeywordService() {
        tooltipDictionary.put("^오존층^", "지구의 대기 중 해로운 자외선을 차단하는 중요한 층입니다.");
        tooltipDictionary.put("^대기오염^", "대기 중 유해물질 농도가 증가하여 건강에 악영향을 끼칠 수 있습니다.");
        tooltipDictionary.put("^PM2.5^", "PM2.5는 지름 2.5μm 이하의 초미세먼지로, 인체에 치명적인 영향을 줄 수 있습니다.");
    }

    /**
     * 스토리 텍스트에서 ^로 감싼 키워드를 정규 표현식을 통해 추출하고,
     * 해당 키워드가 사전에 존재하면 그 툴팁 설명을 결과 Map에 추가합니다.
     *
     * @param storyContent 스토리 텍스트
     * @return 키워드와 툴팁 설명의 매핑 (예: {"^PM2.5^": "설명..."})
     */
    public Map<String, String> extractTooltips(String storyContent) {
        Map<String, String> foundTooltips = new HashMap<>();
        if (storyContent == null) {
            return foundTooltips;
        }

        // 정규 표현식: \^(.*?)\^
        Pattern pattern = Pattern.compile("\\^(.*?)\\^");
        Matcher matcher = pattern.matcher(storyContent);
        while (matcher.find()) {
            // matcher.group(0)는 ^와 함께 전체 매칭된 부분을 반환합니다.
            String extractedKeyword = matcher.group(0);
            // 사전에서 키워드가 존재하는 경우 결과에 추가
            if (tooltipDictionary.containsKey(extractedKeyword)) {
                foundTooltips.put(extractedKeyword, tooltipDictionary.get(extractedKeyword));
            }
        }
        return foundTooltips;
    }
}