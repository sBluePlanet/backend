package com.klpj.blueplanet.model.dao;

import com.klpj.blueplanet.model.dto.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatusDao extends JpaRepository<UserStatus, Long> {
}