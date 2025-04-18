package com.klpj.blueplanet.model.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EventResponse {
    private Long eventId;
    private String title;
    private String content;
    private String writer;

    public EventResponse() {}

    public EventResponse(Long eventId, String title, String content, String writer) {
        this.eventId = eventId;
        this.title = title;
        this.content = content;
        this.writer = writer;
    }

}