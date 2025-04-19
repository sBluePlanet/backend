package com.klpj.blueplanet.model.responses;


import com.klpj.blueplanet.model.dto.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameUpdateResponse {
    private UserStatus userStatus;
    private String result; // 선택지의 결과 (한줄 요약??)
    private int nextEvent; // 1:상시, 2:특별, 3:엔딩

    public GameUpdateResponse() {}

    public GameUpdateResponse(UserStatus userStatus, String result, int nextEvent) {
        this.userStatus = userStatus;
        this.result = result;
        this.nextEvent = nextEvent;
    }

}