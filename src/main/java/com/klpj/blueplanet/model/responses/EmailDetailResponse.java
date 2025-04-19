package com.klpj.blueplanet.model.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 이메일(이벤트) 상세 정보를 나타내는 DTO.
 * 선택지 목록과 함께, 사용자가 선택했던 선택지(selectedChoice)가 포함됩니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDetailResponse {
    private Long eventId;
    private String title;
    private String writer;
    private String content;
    private List<ChoiceSimpleResponse> choices;
    private ChoiceSimpleResponse selectedChoice; // 사용자가 선택한 선택지가 없으면 null
}
