package com.klpj.blueplanet.model.responses;


import com.klpj.blueplanet.model.dto.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameUpdateResponse {
    private UserStatus userStatus;
    private int endingId; // 0이면 정상 진행, 1~10이면 엔딩 조건 충족

    public GameUpdateResponse() {}

    public GameUpdateResponse(UserStatus userStatus, int endingId) {
        this.userStatus = userStatus;
        this.endingId = endingId;
    }

}