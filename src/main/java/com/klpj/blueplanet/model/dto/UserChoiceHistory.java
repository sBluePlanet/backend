package com.klpj.blueplanet.model.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_choice_history")
public class UserChoiceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 해당 유저의 UserStatus ID
    private Long userStatusId;

    // 사용자가 선택한 이벤트의 ID
    private Long eventId;

    // 선택한 선택지의 ID
    private Long choiceId;

    // 선택한 시각 (자동으로 현재 시각 저장)
    @Temporal(TemporalType.TIMESTAMP)
    private Date chosenAt = new Date();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}