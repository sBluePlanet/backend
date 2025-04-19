package com.klpj.blueplanet.model.dto;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "special_event_conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialEventCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "special_event_id")
    private SpecialEvent specialEvent;

    @Column(name = "status_type")
    private String statusType;

    @Column(name = "operator")
    private String operator;

    @Column(name = "variation")
    private int variation;

    @Column(name = "priority")
    private int priority;
}
