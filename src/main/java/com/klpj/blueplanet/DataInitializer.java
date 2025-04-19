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
import java.util.HashMap;
import java.util.Map;

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
    private final EventDao eventDao;
    private final ChoiceDao choiceDao;
    private final EndingDao endingDao;
    private final TooltipDao tooltipDao;
    private final SpecialEventDao specialEventDao;
    private final SpecialEventConditionDao specialEventConditionDao;

    @PostConstruct
    public void init() {
        // 1) 모든 테이블 truncate 및 identity reset
        String sql = "TRUNCATE TABLE events, choices, endings, tooltips, special_events, special_event_conditions RESTART IDENTITY CASCADE";
        jdbcTemplate.execute(sql);
        logger.info("테이블 초기화 및 시퀀스 리셋 완료");

        // 2) 데이터 로드
        initEvents();
        initChoices();
        initEndings();
        initTooltips();
        initSpecialEvents();
        initSpecialEventConditions();

        logger.info("✅ 데이터 재초기화 완료");
    }

    private void initEvents() {
        InputStream is = getClass().getResourceAsStream("/data/events/Regular_Events.xlsx");
        if (is == null) {
            logger.error("Regular_Events.xlsx not found");
            return;
        }
        try (Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 첫 번째 줄은 헤더니까 건너뜀

                try {
                    String title = getStringSafe(row, 1);
                    String writer = getStringSafe(row, 2);
                    String content = getStringSafe(row, 3);

                    // 필수 값 누락 시 스킵
                    if (title == null || title.isBlank() || content == null || content.isBlank()) {
                        logger.warn("❗ 빈 값이 있는 row({})는 건너뜀", row.getRowNum());
                        continue;
                    }

                    Event e = new Event();
                    e.setTitle(title);
                    e.setWriter(writer);
                    e.setContent(content);

                    eventDao.save(e); // 여기서만 save
                } catch (Exception ex) {
                    logger.warn("❌ row {} 저장 중 오류 발생: {}", row.getRowNum(), ex.getMessage());
                }
            }
            logger.info("✅ Events 초기화 완료");
        } catch (Exception e) {
            logger.error("Events 로딩 실패", e);
        }
    }

    private void initChoices() {
        InputStream is = getClass().getResourceAsStream("/data/events/Regular_Events_Choices.xlsx");
        if (is == null) {
            logger.error("Choice_test.xlsx not found");
            return;
        }
        try (Workbook wb = new XSSFWorkbook(is)) {
            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue;

                Cell eventIdCell = row.getCell(1);
                if (eventIdCell == null || eventIdCell.getCellType() != CellType.NUMERIC) continue;

                long eventId = (long) eventIdCell.getNumericCellValue();

                eventDao.findById(eventId).ifPresent(event -> {
                    try {
                        Choice c = new Choice();
                        c.setEvent(event);
                        c.setAirImpact(getIntCellValue(row.getCell(2)));
                        c.setWaterImpact(getIntCellValue(row.getCell(3)));
                        c.setBiologyImpact(getIntCellValue(row.getCell(4)));
                        c.setPopularityImpact(getIntCellValue(row.getCell(5)));
                        c.setResult(getStringCellValue(row.getCell(6)));
                        c.setContent(getStringCellValue(row.getCell(7)));
                        choiceDao.save(c);
                    } catch (Exception innerEx) {
                        logger.warn("⚠️ 특정 Choice 로딩 중 오류 발생 (eventId={}): {}", eventId, innerEx.getMessage());
                    }
                });
            }
            logger.info("✅ Choices 초기화 완료");
        } catch (Exception e) {
            logger.error("Choices 로딩 실패", e);
        }
    }

    // 안전하게 숫자 셀 값을 가져오는 유틸
    private int getIntCellValue(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.NUMERIC) ? (int) cell.getNumericCellValue() : 0;
    }

    // 안전하게 문자열 셀 값을 가져오는 유틸
    private String getStringCellValue(Cell cell) {
        return (cell != null) ? cell.getStringCellValue() : "";
    }



    private void initEndings() {
        InputStream is = getClass().getResourceAsStream("/data/endings/Endings.xlsx");
        if (is == null) {
            logger.error("Ending_test.xlsx not found");
            return;
        }
        try (Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String title = getStringSafe(row, 0);
                    String content = getStringSafe(row, 1);

                    if (title == null || title.isBlank() || content == null || content.isBlank()) {
                        logger.warn("❗ 빈 값이 있는 row({})는 건너뜀", row.getRowNum());
                        continue;
                    }

                    Ending e = new Ending();
                    e.setTitle(title);
                    e.setContent(content);

                    endingDao.save(e);
                } catch (Exception ex) {
                    logger.warn("❌ row {} 저장 중 오류 발생: {}", row.getRowNum(), ex.getMessage());
                }
            }
            logger.info("✅ Endings 초기화 완료");
        } catch (Exception e) {
            logger.error("Endings 로딩 실패", e);
        }
    }

    private void initTooltips() {
        InputStream is = getClass().getResourceAsStream("/data/events/Regular_Events_Keyword.xlsx");
        if (is == null) {
            logger.error("Tooltip_test.xlsx not found");
            return;
        }
        try (Workbook wb = new XSSFWorkbook(is)) {
            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue; // 헤더 스킵

                Cell keywordCell = row.getCell(1);
                Cell contentCell = row.getCell(2);

                // keyword 셀이 null이거나 빈 문자열이면 무시
                if (keywordCell == null || keywordCell.getStringCellValue().isBlank()) {
                    continue;
                }

                Tooltip t = new Tooltip();
                t.setKeyword(keywordCell.getStringCellValue());
                t.setContent(contentCell != null ? contentCell.getStringCellValue() : "");
                tooltipDao.save(t);
            }
            logger.info("✅ Tooltips 초기화 완료");
        } catch (Exception e) {
            logger.error("Tooltips 로딩 실패", e);
        }
    }

    private final Map<Long, SpecialEvent> seqToEvent = new HashMap<>();

    private void initSpecialEvents() {
        try (InputStream is = getClass().getResourceAsStream("/data/special/SpecialEvent_test.xlsx");
             Workbook wb = (is == null ? null : new XSSFWorkbook(is))) {

            if (wb == null) {
                logger.error("SpecialEvent_test.xlsx not found");
                return;
            }
            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue;
                Cell seqCell = row.getCell(0);
                if (seqCell == null || seqCell.getCellType() != CellType.NUMERIC) continue;

                long seq = (long) seqCell.getNumericCellValue();
                SpecialEvent s = new SpecialEvent();
                s.setTitle(getStringSafe(row, 1));
                s.setContent(getStringSafe(row, 2));
                s.setImgUrl(getStringSafe(row, 3));
                s.setAirImpact((int) getNumericSafe(row, 4));
                s.setWaterImpact((int) getNumericSafe(row, 5));
                s.setBiologyImpact((int) getNumericSafe(row, 6));
                s.setPopularityImpact((int) getNumericSafe(row, 7));
                s.setPriority((int) getNumericSafe(row, 8));
                specialEventDao.save(s);

                seqToEvent.put(seq, s);
            }
            logger.info("✅ SpecialEvents 초기화 완료");
        } catch (Exception e) {
            logger.error("SpecialEvents 로딩 실패", e);
        }
    }

    private void initSpecialEventConditions() {
        try (InputStream is = getClass().getResourceAsStream("/data/special/SpecialEventCondition_test.xlsx");
             Workbook wb = (is == null ? null : new XSSFWorkbook(is))) {

            if (wb == null) {
                logger.error("SpecialEventCondition_test.xlsx not found");
                return;
            }
            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue;
                Cell seqCell = row.getCell(1);
                if (seqCell == null || seqCell.getCellType() != CellType.NUMERIC) continue;

                long seq = (long) seqCell.getNumericCellValue();
                SpecialEvent parent = seqToEvent.get(seq);
                if (parent == null) {
                    logger.warn("SpecialEvent seq={} not found (row {})", seq, row.getRowNum());
                    continue;
                }

                SpecialEventCondition cond = new SpecialEventCondition();
                cond.setSpecialEvent(parent);
                cond.setStatusType(getStringSafe(row, 2));
                cond.setOperator(getStringSafe(row, 3));
                cond.setVariation(getIntCellValue(row.getCell(4)));
                specialEventConditionDao.save(cond);
            }
            logger.info("✅ SpecialEventConditions 초기화 완료");
        } catch (Exception e) {
            logger.error("SpecialEventConditions 로딩 실패", e);
        }
    }

    private String getStringSafe(Row row, int idx) {
        Cell cell = row.getCell(idx);
        return cell == null ? "" : cell.getStringCellValue();
    }

    private double getNumericSafe(Row row, int idx) {
        Cell cell = row.getCell(idx);
        return cell == null ? 0 : cell.getNumericCellValue();
    }
}
