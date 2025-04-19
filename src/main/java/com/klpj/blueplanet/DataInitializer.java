package com.klpj.blueplanet;


import com.klpj.blueplanet.model.dao.*;
import com.klpj.blueplanet.model.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 데이터베이스를 매번 초기화하고
 * 최신 XLSX 파일로 덮어씁니다.
 * 모든 테이블을 truncate하여 시퀀스를 리셋하고,
 * 관계형 제약 조건을 CASCADE 처리합니다.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final PrologueDao prologueDao;
    private final EventDao eventDao;
    private final ChoiceDao choiceDao;
    private final EndingDao endingDao;
    private final TooltipDao tooltipDao;
    private final SpecialEventDao specialEventDao;
    private final SpecialEventConditionDao specialEventConditionDao;

    @PostConstruct
    public void init() {
        // 1) 모든 테이블 truncate 및 identity reset
        String sql = "TRUNCATE TABLE prologues, events, choices, endings, tooltips, special_events, special_event_conditions RESTART IDENTITY CASCADE";
        jdbcTemplate.execute(sql);
        logger.info("테이블 초기화 및 시퀀스 리셋 완료");

        // 2) 데이터 로드
        initPrologue();
        initEvents();
        initChoices();
        initEndings();
        initTooltips();
        initSpecialEvents();
        initSpecialEventConditions();

        logger.info("✅ 데이터 재초기화 완료");
    }

    private void initPrologue() {
        try {
            Prologue p = new Prologue();
            p.setTitle("게임 시작");
            p.setContent("환경 문제에 대한 여정을 시작합니다. 여러 이벤트를 통해 환경에 긍정적인 변화를 가져오는 선택을 하게 될 것입니다.");
            prologueDao.save(p);
            logger.info("✅ Prologue 초기화 완료");
        } catch (Exception e) {
            logger.error("Prologue 로딩 실패", e);
        }
    }

    private void initEvents() {
        InputStream is = getClass().getResourceAsStream("/test.data/Event_test.xlsx");
        if (is == null) {
            logger.error("Event_test.xlsx not found");
            return;
        }
        try (Workbook wb = new XSSFWorkbook(is)) {
            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue;
                Event e = new Event();
                e.setTitle(row.getCell(1).getStringCellValue());
                e.setWriter(row.getCell(2).getStringCellValue());
                e.setContent(row.getCell(3).getStringCellValue());
                eventDao.save(e);
            }
            logger.info("✅ Events 초기화 완료");
        } catch (Exception e) {
            logger.error("Events 로딩 실패", e);
        }
    }

    private void initChoices() {
        InputStream is = getClass().getResourceAsStream("/test.data/Choice_test.xlsx");
        if (is == null) {
            logger.error("Choice_test.xlsx not found");
            return;
        }
        try (Workbook wb = new XSSFWorkbook(is)) {
            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue;
                long eventId = (long) row.getCell(1).getNumericCellValue();
                eventDao.findById(eventId).ifPresent(event -> {
                    Choice c = new Choice();
                    c.setEvent(event);
                    c.setAirImpact((int) row.getCell(2).getNumericCellValue());
                    c.setWaterImpact((int) row.getCell(3).getNumericCellValue());
                    c.setBiologyImpact((int) row.getCell(4).getNumericCellValue());
                    c.setPopularityImpact((int) row.getCell(5).getNumericCellValue());
                    c.setResult(row.getCell(6).getStringCellValue());
                    c.setContent(row.getCell(7).getStringCellValue());
                    choiceDao.save(c);
                });
            }
            logger.info("✅ Choices 초기화 완료");
        } catch (Exception e) {
            logger.error("Choices 로딩 실패", e);
        }
    }

    private void initEndings() {
        InputStream is = getClass().getResourceAsStream("/test.data/Ending_test.xlsx");
        if (is == null) {
            logger.error("Ending_test.xlsx not found");
            return;
        }
        try (Workbook wb = new XSSFWorkbook(is)) {
            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue;
                Ending e = new Ending();
                e.setTitle(row.getCell(1).getStringCellValue());
                e.setContent(row.getCell(2).getStringCellValue());
                endingDao.save(e);
            }
            logger.info("✅ Endings 초기화 완료");
        } catch (Exception e) {
            logger.error("Endings 로딩 실패", e);
        }
    }

    private void initTooltips() {
        InputStream is = getClass().getResourceAsStream("/test.data/Tooltip_test.xlsx");
        if (is == null) {
            logger.error("Tooltip_test.xlsx not found");
            return;
        }
        try (Workbook wb = new XSSFWorkbook(is)) {
            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue;
                Tooltip t = new Tooltip();
                t.setKeyword(row.getCell(1).getStringCellValue());
                t.setContent(row.getCell(2).getStringCellValue());
                tooltipDao.save(t);
            }
            logger.info("✅ Tooltips 초기화 완료");
        } catch (Exception e) {
            logger.error("Tooltips 로딩 실패", e);
        }
    }

    private void initSpecialEvents() {
        InputStream is = getClass().getResourceAsStream("/test.data/SpecialEvent_test.xlsx");
        if (is == null) {
            logger.error("SpecialEvent_test.xlsx not found");
            return;
        }
        try (Workbook wb = new XSSFWorkbook(is)) {
            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue;
                SpecialEvent s = new SpecialEvent();
                s.setTitle(row.getCell(1).getStringCellValue());
                s.setContent(row.getCell(2).getStringCellValue());
                s.setImgUrl(row.getCell(3).getStringCellValue());
                s.setAirImpact((int) row.getCell(4).getNumericCellValue());
                s.setWaterImpact((int) row.getCell(5).getNumericCellValue());
                s.setBiologyImpact((int) row.getCell(6).getNumericCellValue());
                s.setPopularityImpact((int) row.getCell(7).getNumericCellValue());
                specialEventDao.save(s);
            }
            logger.info("✅ SpecialEvents 초기화 완료");
        } catch (Exception e) {
            logger.error("SpecialEvents 로딩 실패", e);
        }
    }

    private void initSpecialEventConditions() {
        InputStream is = getClass().getResourceAsStream("/test.data/SpecialEventCondition_test.xlsx");
        if (is == null) {
            logger.error("SpecialEventCondition_test.xlsx not found");
            return;
        }
        try (Workbook wb = new XSSFWorkbook(is)) {
            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue;
                Long seId = (long) row.getCell(1).getNumericCellValue();
                specialEventDao.findById(seId).ifPresent(se -> {
                    SpecialEventCondition cond = new SpecialEventCondition();
                    cond.setSpecialEvent(se);
                    cond.setStatusType(row.getCell(2).getStringCellValue());
                    cond.setOperator(row.getCell(3).getStringCellValue());
                    cond.setVariation((int) row.getCell(4).getNumericCellValue());
                    cond.setPriority((int) row.getCell(5).getNumericCellValue());
                    specialEventConditionDao.save(cond);
                });
            }
            logger.info("✅ SpecialEventConditions 초기화 완료");
        } catch (Exception e) {
            logger.error("SpecialEventConditions 로딩 실패", e);
        }
    }
}