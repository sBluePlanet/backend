package com.klpj.blueplanet.model.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class NextEventResponse {
    private EventResponse event;         // 이벤트 정보 (eventId, title, content, writer)
    private List<TooltipResponse> tooltips; // 툴팁 정보 목록 (키워드, 설명)
    private List<ChoiceSimpleResponse> choices; // 선택지 목록 (id, content)

    public NextEventResponse() {}

    public NextEventResponse(EventResponse event, List<TooltipResponse> tooltips, List<ChoiceSimpleResponse> choices) {
        this.event = event;
        this.tooltips = tooltips;
        this.choices = choices;
    }

}