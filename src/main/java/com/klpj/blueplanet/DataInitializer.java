package com.klpj.blueplanet;


import com.klpj.blueplanet.model.dao.*;
import com.klpj.blueplanet.model.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final EventDao eventDao;
    private final ChoiceDao choiceDao;
    private final EndingDao endingDao;
    private final TooltipDao tooltipDao;
    private final SpecialEventDao specialEventDao;
    private final SpecialEventConditionDao specialEventConditionDao;
    private final PrologueDao prologueDao; // 추가된 PrologueDao

    @PostConstruct
    public void init() {
        if (eventDao.count() == 0) {
            // Prologue 데이터 초기화 추가
            initPrologue();
            initEvents();
            initChoices();
            initEndings();
            initTooltips();
            initSpecialEvents();
            initSpecialEventConditions();
            System.out.println("불변 데이터 삽입 완료!");
        } else {
            System.out.println("데이터 이미 존재 — 초기화 생략됨");
        }
    }

    // Prologue 초기화 (하드코딩 방식)
    private void initPrologue() {
        Prologue prologue = new Prologue();
        prologue.setTitle("게임 시작");
        prologue.setContent("환경 문제에 대한 여정을 시작합니다. 여러 이벤트를 통해 환경에 긍정적인 변화를 가져오는 선택을 하게 될 것입니다.");
        prologueDao.save(prologue);
        System.out.println("✅ Prologue 성공");
    }

    private void initEvents() {
        try (InputStream is = getClass().getResourceAsStream("/test.data/Event_test.xlsx");
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                Event e = new Event();
                e.setTitle(row.getCell(1).getStringCellValue());
                e.setWriter(row.getCell(2).getStringCellValue());
                e.setContent(row.getCell(3).getStringCellValue());
                eventDao.save(e);
                System.out.println("✅ Event 성공");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initChoices() {
        try (InputStream is = getClass().getResourceAsStream("/test.data/Choice_test.xlsx");
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                long eventId = (long) row.getCell(1).getNumericCellValue();
                Event event = eventDao.findById(eventId).orElse(null);
                if (event == null) continue;

                Choice c = new Choice();
                c.setEvent(event);
                c.setAirImpact((int) row.getCell(2).getNumericCellValue());
                c.setWaterImpact((int) row.getCell(3).getNumericCellValue());
                c.setBiologyImpact((int) row.getCell(4).getNumericCellValue());
                c.setPopularityImpact((int) row.getCell(5).getNumericCellValue());
                c.setResult(row.getCell(6).getStringCellValue());
                c.setContent(row.getCell(7).getStringCellValue());
                choiceDao.save(c);
                System.out.println("✅ Choice 성공");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initEndings() {
        try (InputStream is = getClass().getResourceAsStream("/test.data/Ending_test.xlsx");
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                Ending e = new Ending();
                e.setTitle(row.getCell(1).getStringCellValue());
                e.setContent(row.getCell(2).getStringCellValue());
                endingDao.save(e);
                System.out.println("✅ Ending 성공");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTooltips() {
        try (InputStream is = getClass().getResourceAsStream("/test.data/Tooltip_test.xlsx");
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Tooltip tooltip = new Tooltip();
                tooltip.setKeyword(row.getCell(1).getStringCellValue());
                tooltip.setContent(row.getCell(2).getStringCellValue());

                tooltipDao.save(tooltip);
                System.out.println("✅ Tooltip 성공");
            }
            System.out.println("Tooltip 초기화 완료");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSpecialEvents() {
        try (InputStream is = getClass().getResourceAsStream("/test.data/SpecialEvent_test.xlsx");
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 헤더 스킵

                SpecialEvent s = new SpecialEvent();

                s.setTitle(row.getCell(1).getStringCellValue());
                s.setContent(row.getCell(2).getStringCellValue());
                s.setImgUrl(row.getCell(3).getStringCellValue());
                s.setAirImpact((int) row.getCell(4).getNumericCellValue());
                s.setWaterImpact((int) row.getCell(5).getNumericCellValue());
                s.setEcologyImpact((int) row.getCell(6).getNumericCellValue());
                s.setPopularityImpact((int) row.getCell(7).getNumericCellValue());

                specialEventDao.save(s);
                System.out.println("✅ SpecialEvent 성공");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSpecialEventConditions() {
        try (InputStream is = getClass().getResourceAsStream("/test.data/SpecialEventCondition_test.xlsx");
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Long seId = (long) row.getCell(1).getNumericCellValue();
                SpecialEvent se = specialEventDao.findById(seId).orElse(null);
                if (se == null) {
                    System.out.println("SpecialEvent not found for id: " + seId);
                    continue;
                }

                SpecialEventCondition cond = new SpecialEventCondition();
                cond.setSpecialEvent(se);
                cond.setStatusType(row.getCell(2).getStringCellValue());
                cond.setOperator(row.getCell(3).getStringCellValue());
                cond.setVariation((int) row.getCell(4).getNumericCellValue());
                cond.setPriority((int) row.getCell(5).getNumericCellValue());

                specialEventConditionDao.save(cond);
                System.out.println("✅ SpecialEventCondition 성공");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}