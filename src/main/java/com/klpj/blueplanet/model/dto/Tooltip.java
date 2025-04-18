package com.klpj.blueplanet.model.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tooltips")
public class Tooltip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 키워드 (예: "오존층") – 유일하도록 관리
    @Column(length = 100, unique = true)
    private String keyword;

    // 툴팁에 표시할 설명 혹은 관련 과학 정보
    @Column(length = 1000)
    private String content;
}
