package com.klpj.blueplanet.model.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * KeywordService는 스토리 텍스트 내에서 미리 정의된 키워드를 검색하여,
 * 해당 키워드에 대응하는 툴팁(설명) 정보를 반환합니다.
 * 현재는 하드코딩된 키워드–툴팁 매핑 정보를 사용하지만, 필요에 따라 DB나 설정 파일에서 로딩하도록 확장할 수 있습니다.
 */
@Service
public class KeywordService {

    // 미리 정의된 키워드와 툴팁 설명 매핑 정보를 저장하는 사전
    private final Map<String, String> tooltipDictionary = new HashMap<>();

    public KeywordService() {
        // 하드코딩 예시 (필요에 따라 키워드 추가)
        tooltipDictionary.put("오존층", "지구의 대기 중 해로운 자외선을 차단하는 중요한 층입니다.");
        tooltipDictionary.put("대기오염", "대기 중 유해물질 농도가 증가하여 건강에 악영향을 끼칠 수 있습니다.");
        // 예시: 추가 키워드와 설명
        tooltipDictionary.put("수질", "물의 오염 정도를 나타내며, 깨끗하지 않으면 생태계에 문제를 일으킵니다.");
        tooltipDictionary.put("생물", "환경 속 생물 다양성을 의미하며, 특정 수치가 변화하면 생태계에 큰 영향을 줍니다.");
    }

    /**
     * 주어진 스토리 텍스트에서 미리 정의된 키워드가 포함되어 있는지 검사하고,
     * 포함된 키워드와 그에 대응하는 툴팁 설명을 Map 형태로 반환합니다.
     *
     * @param storyContent 스토리 내용
     * @return 스토리에서 발견된 키워드와 해당 툴팁 설명의 매핑
     */
    public Map<String, String> extractTooltips(String storyContent) {
        Map<String, String> foundTooltips = new HashMap<>();
        // 사전에 등록된 각 키워드에 대해 스토리 내용에 해당 키워드가 포함되어 있는지 검사합니다.
        for (Map.Entry<String, String> entry : tooltipDictionary.entrySet()) {
            String keyword = entry.getKey();
            if (storyContent != null && storyContent.contains(keyword)) {
                foundTooltips.put(keyword, entry.getValue());
            }
        }
        return foundTooltips;
    }
}