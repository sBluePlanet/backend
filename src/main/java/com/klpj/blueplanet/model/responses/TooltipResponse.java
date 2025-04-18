package com.klpj.blueplanet.model.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TooltipResponse {
    private String keyword;
    private String content;

    public TooltipResponse() {}

    public TooltipResponse(String keyword, String content) {
        this.keyword = keyword;
        this.content = content;
    }

}