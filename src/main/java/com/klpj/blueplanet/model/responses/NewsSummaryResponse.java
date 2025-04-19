package com.klpj.blueplanet.model.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsSummaryResponse {
    private Long specialEventId;
    private String title;
    // 필요에 따라 내용 미리보기(preview) 필드를 추가할 수 있습니다.
}
