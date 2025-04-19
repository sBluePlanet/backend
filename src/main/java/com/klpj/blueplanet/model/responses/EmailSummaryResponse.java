package com.klpj.blueplanet.model.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이메일 목록에 표시할 항목으로,
 * 사용자가 이미 수신한 이벤트의 eventId와 title을 담는 DTO입니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailSummaryResponse {
    private Long id;
    private String title;
    private String type; // "event" or "advice"
    private LocalDateTime createdAt;

}