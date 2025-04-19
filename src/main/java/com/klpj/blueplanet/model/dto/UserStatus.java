package com.klpj.blueplanet.model.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_status")
public class UserStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    // 각 수치를 저장하는 필드
    private int air;       // 대기
    private int water;     // 수질
    private int biology;   // 생물
    private int popularity; // 지지도

    // 게임 진행 턴
    @Column(name = "turn_count")
    private int turnCount;

    // 사용된 이벤트 ID를 저장
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_status_used_events", joinColumns = @JoinColumn(name = "user_status_id"))
    @Column(name = "event_id")
    private Set<Long> usedEventIds = new HashSet<>();
}