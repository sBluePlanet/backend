package com.klpj.blueplanet.model.responses;


import com.klpj.blueplanet.model.dto.SpecialEvent;
import com.klpj.blueplanet.model.dto.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialEventResponse {
    private Long id;
    private String title;
    private String content;
    private String imgUrl;

    private UserStatus userStatus;
    private int nextEvent;

    public SpecialEventResponse(SpecialEvent event, UserStatus userStatus, int nextEvent) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.content = event.getContent();
        this.imgUrl = event.getImgUrl();
        this.userStatus = userStatus;
        this.nextEvent = nextEvent;
    }

    // Getters & Setters (or @Data if using Lombok)
}
