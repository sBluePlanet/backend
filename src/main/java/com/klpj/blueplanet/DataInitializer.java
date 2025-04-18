package com.klpj.blueplanet;


import com.klpj.blueplanet.model.dao.EventDao;
import com.klpj.blueplanet.model.dto.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration 
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(EventDao eventDao) {
        return args -> {
            // 엑셀 파일을 클래스패스(예: src/main/resources)에 위치시킵니다.
            try (InputStream is = getClass().getResourceAsStream("/events/data/EventData.xlsx");
                 Workbook workbook = new XSSFWorkbook(Objects.requireNonNull(is))) { // XSSFWorkbook: .xlsx 파일 형식을 처리합니다.

                Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트를 사용합니다.
                boolean firstRow = true;  // 첫 번째 행은 헤더라고 가정
                // 이벤트 제목을 키로 하여, 같은 제목의 이벤트를 재활용할 수 있도록 Map으로 관리합니다.
                Map<String, Event> eventMap = new HashMap<>();

                // 시트의 모든 행(row)을 순회합니다.
                for (Row row : sheet) {
                    // 첫 번째 행(헤더 행)은 건너뜁니다.
                    if (firstRow) {
                        firstRow = false;
                        continue;
                    }

                    // 각 셀의 데이터를 읽습니다.
                    // 컬럼 순서: 0: Event Title, 1: Event Content, 2: Choice Text,
                    // 3: Air Impact, 4: Water Impact, 5: Biology Impact, 6: Popularity Impact
                    Cell eventTitleCell = row.getCell(0);
                    Cell eventContentCell = row.getCell(1);
                    Cell choiceTextCell = row.getCell(2);
                    Cell airImpactCell = row.getCell(3);
                    Cell waterImpactCell = row.getCell(4);
                    Cell biologyImpactCell = row.getCell(5);
                    Cell popularityImpactCell = row.getCell(6);

                    // String 형태의 값 읽기 (null 체크는 필요 시 추가)
                    String eventTitle = eventTitleCell.getStringCellValue();
                    String eventContent = eventContentCell.getStringCellValue();
                    String choiceText = choiceTextCell.getStringCellValue();

                    // 수치 데이터는 Numeric 형으로 읽은 후 int로 변환합니다.
                    int airImpact = (int) airImpactCell.getNumericCellValue();
                    int waterImpact = (int) waterImpactCell.getNumericCellValue();
                    int biologyImpact = (int) biologyImpactCell.getNumericCellValue();
                    int popularityImpact = (int) popularityImpactCell.getNumericCellValue();

                    // 동일한 이벤트 제목으로 생성된 이벤트가 존재하는지 확인합니다.
                    Event event = eventMap.get(eventTitle);
                    if (event == null) {
                        // 존재하지 않으면 새 이벤트 객체를 생성합니다.
                        event = new Event();
                        event.setTitle(eventTitle);
                        event.setContent(eventContent);
                        event.setChoices(new ArrayList<>()); // 빈 선택지 리스트 생성
                        eventMap.put(eventTitle, event);
                    }

                    // 새로운 선택지 객체 생성
                    Choice choice = new Choice();
                    choice.setText(choiceText);
                    choice.setAirImpact(airImpact);
                    choice.setWaterImpact(waterImpact);
                    choice.setBiologyImpact(biologyImpact);
                    choice.setPopularityImpact(popularityImpact);
                    choice.setEvent(event); // 선택지에 이벤트 객체 연결

                    // 이벤트의 선택지 리스트에 선택지를 추가합니다.
                    event.getChoices().add(choice);
                }

                // 생성된 모든 이벤트(및 연관된 선택지들)를 DB에 저장합니다.
                // Cascade 옵션에 따라 선택지도 함께 저장됩니다.
                eventDao.saveAll(new ArrayList<>(eventMap.values()));
                System.out.println("엑셀 파일에서 초기 데이터가 DB에 추가되었습니다.");

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
