package com.klpj.blueplanet.model.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChoiceResponse {
    // Getters and setters ...
    private Long id;
    private String text;
    private int airImpact;
    private int waterImpact;
    private int biologyImpact;
    private int popularityImpact;

    public ChoiceResponse() {
    }

    public ChoiceResponse(Long id, String text, int airImpact, int waterImpact, int biologyImpact, int popularityImpact) {
        this.id = id;
        this.text = text;
        this.airImpact = airImpact;
        this.waterImpact = waterImpact;
        this.biologyImpact = biologyImpact;
        this.popularityImpact = popularityImpact;
    }

}