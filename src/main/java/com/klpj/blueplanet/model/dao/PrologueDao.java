package com.klpj.blueplanet.model.dao;

import com.klpj.blueplanet.model.dto.Prologue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrologueDao extends JpaRepository<Prologue, Long> {
}