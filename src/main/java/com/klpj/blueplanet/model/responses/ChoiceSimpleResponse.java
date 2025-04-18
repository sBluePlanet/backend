package com.klpj.blueplanet.model.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChoiceSimpleResponse {
    private Long id;
    private String content;

    public ChoiceSimpleResponse() {}

    public ChoiceSimpleResponse(Long id, String content) {
        this.id = id;
        this.content = content;
    }

}