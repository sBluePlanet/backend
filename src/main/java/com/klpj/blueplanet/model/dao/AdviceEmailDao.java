package com.klpj.blueplanet.model.dao;

import com.klpj.blueplanet.model.dto.AdviceEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdviceEmailDao extends JpaRepository<AdviceEmail, Long> {
    // 특정 유저가 받은 모든 조언 목록
    List<AdviceEmail> findByUserId(Long userId);

    // 특정 유저가 특정 이벤트에 대해 요청한 모든 조언
    Optional<AdviceEmail> findTopByUserIdAndEventIdOrderByCreatedAtDesc(Long userId, Long eventId);

    // 최신 순으로 정렬
    List<AdviceEmail> findByUserIdOrderByCreatedAtDesc(Long userId);


}
