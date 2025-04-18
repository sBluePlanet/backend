package com.klpj.blueplanet.model.dao;

import com.klpj.blueplanet.model.dto.Ending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndingDao extends JpaRepository<Ending, Long> {
}