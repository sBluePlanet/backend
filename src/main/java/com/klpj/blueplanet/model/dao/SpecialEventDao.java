package com.klpj.blueplanet.model.dao;

import com.klpj.blueplanet.model.dto.SpecialEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialEventDao extends JpaRepository<SpecialEvent, Long> {
}
