package com.klpj.blueplanet.model.responses;


import com.klpj.blueplanet.model.dto.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartGameResponse {
    private UserStatus userStatus; // 초기 게임 상태 (수치, 턴 등)
    private int nextEvent; // 다음 이벤트 요청 타입 (프로롤로그에서는 1)

    public StartGameResponse() {
    }

    public StartGameResponse(UserStatus userStatus, int nextEvent) {
        this.userStatus = userStatus;
        this.nextEvent = nextEvent;
    }
}