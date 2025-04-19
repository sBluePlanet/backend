package com.klpj.blueplanet.model.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String title;

    private String content;

    @Column(name = "img_url", length = 500)
    private String imgUrl;

    @Column(name = "air_impact")
    private int airImpact;

    @Column(name = "water_impact")
    private int waterImpact;

    @Column(name = "ecology_impact")
    private int ecologyImpact;

    @Column(name = "popularity_impact")
    private int popularityImpact;
}
