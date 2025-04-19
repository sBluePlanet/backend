package com.klpj.blueplanet.model.dao;

import com.klpj.blueplanet.model.dto.SpecialEventCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialEventConditionDao extends JpaRepository<SpecialEventCondition, Long> {
}