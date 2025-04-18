package com.klpj.blueplanet.model.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"id", "text", "airImpact", "waterImpact", "biologyImpact", "popularityImpact"})
@Data
@NoArgsConstructor
@Entity
@Table(name = "choices")
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    // 각 선택지가 미치는 영향 수치들 (예: -15~+15 범위)
    private int airImpact;         // 대기에 미치는 영향
    private int waterImpact;       // 수질에 미치는 영향
    private int biologyImpact;     // 생물에 미치는 영향
    private int popularityImpact;  // 지지도에 미치는 영향

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event; // 이 선택지가 속한 이벤트
}