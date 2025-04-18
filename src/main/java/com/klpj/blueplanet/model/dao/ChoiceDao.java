package com.klpj.blueplanet.model.dao;


import com.klpj.blueplanet.model.dto.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ChoiceDao extends JpaRepository<Choice, Long> {
}