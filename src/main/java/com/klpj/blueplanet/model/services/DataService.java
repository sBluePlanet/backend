package com.klpj.blueplanet.model.services;

import com.klpj.blueplanet.model.dao.*;
import com.klpj.blueplanet.model.dto.*;
import com.klpj.blueplanet.model.responses.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DataService {

    @Autowired
    private TooltipDao tooltipDao;

    @Autowired
    private UserStatusDao userStatusDao;

    @Autowired
    private UserChoiceHistoryDao userChoiceHistoryDao;

    @Autowired
    private ChoiceDao choiceDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private SpecialEventDao specialEventDao;

    /**
     * 모든 툴팁 정보를 TooltipResponse DTO 형태로 반환합니다.
     */
    public List<TooltipResponse> getAllTooltips() {
        return tooltipDao.findAll().stream()
                .map(t -> new TooltipResponse(t.getKeyword(), t.getContent()))
                .collect(Collectors.toList());
    }

    /**
     * 사용자(userId)가 이미 수신한 이메일 목록(이벤트 요약: eventId, title)을 EmailSummaryResponse 형태로 반환합니다.
     */
    public List<EmailSummaryResponse> getEmailList(Long userId) {
        UserStatus userStatus = userStatusDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Set<Long> usedEventIds = userStatus.getUsedEventIds();
        List<Event> events = eventDao.findAllById(usedEventIds);
        return events.stream()
                .map(event -> new EmailSummaryResponse(event.getId(), event.getTitle()))
                .collect(Collectors.toList());
    }

    /**
     * 사용자(userId)와 이벤트(eventId)를 받아 해당 이메일(이벤트) 상세 정보를 반환합니다.
     * 추가로, 사용자가 해당 이벤트에서 선택했던 선택지 정보가 있으면 함께 포함합니다.
     */
    public EmailDetailResponse getEmailDetail(Long userId, Long eventId) {
        // 1. 이벤트 조회
        Event event = eventDao.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // 2. 원래 선택지 목록 (간략 버전) 생성
        List<ChoiceSimpleResponse> choices = event.getChoices()
                .stream()
                .map(c -> new ChoiceSimpleResponse(c.getId(), c.getContent()))
                .collect(Collectors.toList());

        // 3. 유저의 선택 이력 조회 (해당 사용자(userId)와 이벤트(eventId)에 대한 기록)
        List<UserChoiceHistory> histories = userChoiceHistoryDao.findByUserStatusIdAndEventId(userId, eventId);

        // 일반적으로 이벤트당 한 번의 선택만 기록하므로 첫 번째 기록을 사용합니다.
        ChoiceSimpleResponse selectedChoice = null;
        if (!histories.isEmpty()) {
            UserChoiceHistory history = histories.get(0);
            Choice choice = choiceDao.findById(history.getChoiceId())
                    .orElse(null);
            if (choice != null) {
                selectedChoice = new ChoiceSimpleResponse(choice.getId(), choice.getContent());
            }
        }

        // 4. EmailDetailResponse DTO 생성 (선택 이력이 없으면 selectedChoice는 null)
        return new EmailDetailResponse(
                event.getId(),
                event.getTitle(),
                event.getWriter(),
                event.getContent(),
                choices,
                selectedChoice
        );
    }

    /**
     * 사용자(userId)가 수신한 특수 이벤트(뉴스) 목록을 NewsSummaryResponse 형태로 반환합니다.
     */
    public List<NewsSummaryResponse> getNewsList(Long userId) {
        UserStatus userStatus = userStatusDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Set<Long> usedSpecialEventIds = userStatus.getUsedSpecialEventIds();
        List<SpecialEvent> specialEvents = specialEventDao.findAllById(usedSpecialEventIds);

        return specialEvents.stream()
                .map(se -> new NewsSummaryResponse(se.getId(), se.getTitle()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 뉴스(특수 이벤트) ID를 받아, 상세 정보를 NewsDetailResponse 형태로 반환합니다.
     */
    public NewsDetailResponse getNewsDetail(Long specialEventId) {
        SpecialEvent specialEvent = specialEventDao.findById(specialEventId)
                .orElseThrow(() -> new RuntimeException("Special event not found with id: " + specialEventId));
        return new NewsDetailResponse(
                specialEvent.getId(),
                specialEvent.getTitle(),
                specialEvent.getContent(),
                specialEvent.getImgUrl()
        );
    }
}
