package com.klpj.blueplanet.model.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsDetailResponse {
    private Long specialEventId;
    private String title;
    private String content;
    private String imgUrl;
    // 필요시 영향치(airImpact, waterImpact 등)를 포함할 수 있습니다.
}
