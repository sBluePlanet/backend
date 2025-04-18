package com.klpj.blueplanet.model.services;

import com.klpj.blueplanet.model.dao.EventDao;
import com.klpj.blueplanet.model.dto.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventDao eventDao;

    // ID로 이벤트 조회
    public Event getEventById(Long id) {
        return eventDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    // 모든 이벤트 조회
    public List<Event> getAllEvents() {
        return eventDao.findAll();
    }
}