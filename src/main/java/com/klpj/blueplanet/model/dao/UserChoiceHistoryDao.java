package com.klpj.blueplanet.model.dao;

import com.klpj.blueplanet.model.dto.UserChoiceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserChoiceHistoryDao extends JpaRepository<UserChoiceHistory, Long> {
    List<UserChoiceHistory> findByUserStatusIdAndEventId(Long userStatusId, Long eventId);

    // 데이터를 선택 시간 순으로 정렬
    List<UserChoiceHistory> findByUserStatusIdOrderByChosenAtAsc(Long userStatusId);

    List<UserChoiceHistory> findByUserStatusId(Long userId);
}