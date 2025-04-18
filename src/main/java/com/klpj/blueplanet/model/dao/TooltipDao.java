package com.klpj.blueplanet.model.dao;

import com.klpj.blueplanet.model.dto.Tooltip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TooltipDao extends JpaRepository<Tooltip, Long> {
}