package com.klpj.blueplanet.controller;

import com.klpj.blueplanet.model.responses.*;
import com.klpj.blueplanet.model.services.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private DataService dataService;

    /**
     * /data/tooltips 엔드포인트는 모든 툴팁 정보를 TooltipResponse DTO 목록으로 반환합니다.
     */
    @GetMapping("/tooltips")
    public ResponseEntity<List<TooltipResponse>> getTooltips() {
        return ResponseEntity.ok(dataService.getAllTooltips());
    }

    /**
     * /data/emailList 엔드포인트는 지정된 userId에 대한 이메일(이벤트) 목록 요약 정보를 반환합니다.
     */
    @GetMapping("/emailList")
    public ResponseEntity<List<EmailSummaryResponse>> getEmailList(@RequestParam("userId") Long userId) {
        List<EmailSummaryResponse> emailList = dataService.getEmailList(userId);
        return ResponseEntity.ok(emailList);
    }

    /**
     * /data/emailDetail 엔드포인트는 userId와 eventId를 받아 해당 이벤트의 상세 정보와
     * 사용자가 선택한 선택지를 포함한 이메일 상세 정보를 반환합니다.
     */
    @GetMapping("/emailDetail")
    public ResponseEntity<EmailDetailResponse> getEmailDetail(
            @RequestParam("userId") Long userId,
            @RequestParam("eventId") Long id) {
        EmailDetailResponse response = dataService.getEmailDetail(userId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * /data/newsList 엔드포인트는 사용자(userId)가 수신한 특수 이벤트(뉴스) 목록의 요약 정보를 반환합니다.
     */
    @GetMapping("/newsList")
    public ResponseEntity<List<NewsSummaryResponse>> getNewsList(@RequestParam("userId") Long userId) {
        List<NewsSummaryResponse> newsList = dataService.getNewsList(userId);
        return ResponseEntity.ok(newsList);
    }

    /**
     * /data/newsDetail 엔드포인트는 특정 특수 이벤트(specialEventId)의 상세 정보를 반환합니다.
     */
    @GetMapping("/newsDetail")
    public ResponseEntity<NewsDetailResponse> getNewsDetail(@RequestParam("specialEventId") Long specialEventId) {
        NewsDetailResponse response = dataService.getNewsDetail(specialEventId);
        return ResponseEntity.ok(response);
    }
}