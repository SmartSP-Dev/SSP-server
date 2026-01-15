package group4.opensource_server.calendar.service;

import group4.opensource_server.calendar.domain.TimetableConstants;
import group4.opensource_server.calendar.dto.CalendarTimetableDto;
import group4.opensource_server.calendar.exception.TimetableParsingException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 시간표 파싱 클래스
 *
 * <주요 기능>
 * 1. CSS 스타일(top, height)에서 시간 정보 추출
 * 2. 픽셀 값을 시간으로 변환
 * 3. 요일별로 강의 그룹화
 *
 * <시간 변환 알고리즘>
 * - 에브리타임은 강의 시간을 CSS의 top, height 속성으로 표현
 * - 변환 공식: startMinutes = ((top - BASE_TOP_PX) / PX_PER_SLOT) * MINUTES_PER_SLOT
 * - 기준: top=450px가 09:00, 25px = 30분 단위
 * - 예시: top=550px → ((550-450)/25)*30 = 120분 → 11:00
 */
@Slf4j
@Component
public class TimetableParser {

    private static final Pattern TOP_PATTERN = Pattern.compile("top:\\s*(\\d+)px");
    private static final Pattern HEIGHT_PATTERN = Pattern.compile("height:\\s*(\\d+)px");

    /**
     * 크롤링 결과를 파싱하여 시간표 데이터를 생성합니다.
     *
     * @param crawlResult 크롤링 결과 (요일 목록, 강의 요소 목록)
     * @return 요일별로 그룹화된 시간표 맵 (key: 요일, value: 강의 목록)
     * @throws TimetableParsingException 파싱 실패 시
     */
    public Map<String, List<CalendarTimetableDto>> parse(EverytimeCrawler.CrawlResult crawlResult) {
        try {
            Map<String, List<CalendarTimetableDto>> scheduleMap = new HashMap<>();
            List<String> days = crawlResult.days();
            List<WebElement> subjects = crawlResult.subjects();

            log.info("시간표 파싱 시작: 강의 {}개", subjects.size());

            for (WebElement subject : subjects) {
                parseSubject(subject, days, scheduleMap);
            }

            log.info("시간표 파싱 완료: {}개 요일", scheduleMap.size());
            return scheduleMap;

        } catch (Exception e) {
            log.error("시간표 파싱 실패", e);
            throw new TimetableParsingException("시간표 파싱 실패", e);
        }
    }

    /**
     * 개별 강의 요소를 파싱합니다.
     *
     * @param subject 강의 WebElement
     * @param days 요일 목록
     * @param scheduleMap 결과를 저장할 맵
     */
    private void parseSubject(WebElement subject, List<String> days, Map<String, List<CalendarTimetableDto>> scheduleMap) {
        try {
            // CSS style 속성에서 top, height 추출
            String style = subject.getAttribute("style");
            Matcher topMatcher = TOP_PATTERN.matcher(style);
            Matcher heightMatcher = HEIGHT_PATTERN.matcher(style);

            // top이나 height가 없으면 스킵
            if (!topMatcher.find() || !heightMatcher.find()) {
                log.debug("top 또는 height 없음, 스킵");
                return;
            }

            int top = Integer.parseInt(topMatcher.group(1));
            int height = Integer.parseInt(heightMatcher.group(1));

            // 시간 슬롯 생성
            List<String> timeSlots = calculateTimeSlots(top, height);

            // 요일 인덱스 찾기
            int dayIndex = findDayIndex(subject);

            // 요일 범위 내에 있는 경우만 처리
            if (dayIndex >= 0 && dayIndex < days.size()) {
                String day = days.get(dayIndex);
                String subjectName = subject.getText();

                scheduleMap.computeIfAbsent(day, k -> new ArrayList<>())
                        .add(new CalendarTimetableDto(timeSlots, subjectName));

                log.debug("강의 추가: {} - {} ({} 슬롯)", day, subjectName, timeSlots.size());
            }

        } catch (Exception e) {
            log.warn("강의 파싱 중 오류 (스킵): {}", e.getMessage());
            // 개별 강의 파싱 실패는 전체 파싱을 중단하지 않음
        }
    }

    /**
     * CSS top, height 값을 시간 슬롯 목록으로 변환합니다.
     *
     * <시간 변환 알고리즘>
     * 1. 시작 시간 계산: startMinutes = ((top - BASE_TOP_PX) / PX_PER_SLOT) * MINUTES_PER_SLOT
     * 2. 종료 시간 계산: endMinutes = startMinutes + (height를 30분 단위로 변환)
     * 3. 15분 단위로 시간 슬롯 생성
     *
     * 예시:
     * - top=450, height=50 → 09:00~10:00 → ["09:00", "09:15", "09:30", "09:45"]
     * - top=550, height=75 → 11:00~12:30 → ["11:00", "11:15", "11:30", "11:45", "12:00", "12:15"]
     *
     * @param top CSS top 픽셀 값
     * @param height CSS height 픽셀 값
     * @return 15분 단위 시간 슬롯 목록
     */
    private List<String> calculateTimeSlots(int top, int height) {
        // 시작 시간 계산 (분 단위)
        int startMinutes = ((top - TimetableConstants.BASE_TOP_PX) / TimetableConstants.PX_PER_SLOT)
                * TimetableConstants.MINUTES_PER_SLOT;

        // 종료 시간 계산 (분 단위)
        int endMinutes = startMinutes + (int) Math.ceil((double)(height - 1) / TimetableConstants.PX_PER_SLOT)
                * TimetableConstants.MINUTES_PER_SLOT;

        // 15분 단위로 시간 슬롯 생성
        List<String> timeSlots = new ArrayList<>();
        for (int m = startMinutes; m < endMinutes; m += TimetableConstants.TIME_SLOT_INTERVAL) {
            int hour = TimetableConstants.BASE_HOUR + m / 60;
            int minute = m % 60;
            timeSlots.add(String.format("%02d:%02d", hour, minute));
        }

        return timeSlots;
    }

    /**
     * 강의 요소의 요일 인덱스를 찾습니다.
     * XPath로 부모 요소를 탐색하여 몇 번째 열(요일)인지 확인합니다.
     *
     * @param subject 강의 WebElement
     * @return 요일 인덱스 (0부터 시작)
     */
    private int findDayIndex(WebElement subject) {
        try {
            WebElement parentTd = subject.findElement(By.xpath("./ancestor::td"));
            WebElement parentTr = parentTd.findElement(By.xpath("./ancestor::tr"));
            List<WebElement> allTds = parentTr.findElements(By.tagName("td"));

            return allTds.indexOf(parentTd);
        } catch (Exception e) {
            log.warn("요일 인덱스 찾기 실패: {}", e.getMessage());
            return -1;
        }
    }
}
