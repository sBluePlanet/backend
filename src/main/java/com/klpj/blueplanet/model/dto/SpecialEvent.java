package com.klpj.blueplanet.model.dto;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "special_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "img_url", length = 500)
    private String imgUrl;

    @Column(name = "air_impact")
    private int airImpact;

    @Column(name = "water_impact")
    private int waterImpact;

    @Column(name = "biology_impact")
    private int biologyImpact;

    @Column(name = "popularity_impact")
    private int popularityImpact;

    @Column(name = "priority")
    private int priority;
}
