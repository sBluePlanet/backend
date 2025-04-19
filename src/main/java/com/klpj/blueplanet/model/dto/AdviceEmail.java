package com.klpj.blueplanet.model.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Table(name="advice_emails")
@Data
@Entity
public class AdviceEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 조언 요청한 유저 ID
    private Long eventId; // 어떤 이벤트에 대한 조언인지
    private String title; // 조언 제목 (이벤트 제목)

    @Column(columnDefinition = "TEXT")
    private String content; // GPT가 준 조언 내용

    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 시각 자동 기록
}
