package com.klpj.blueplanet.model.requests;

import lombok.Data;

@Data
public class AdviceRequest {
    private Long userId;
    private Long eventId;
    private String title;
    private String content;
}
