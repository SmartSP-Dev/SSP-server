package group4.opensource_server.calendar.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import group4.opensource_server.calendar.domain.EveryTimeTimetable;
import group4.opensource_server.calendar.domain.EveryTimeTimetableRepository;
import group4.opensource_server.calendar.dto.*;
import group4.opensource_server.calendar.exception.TimetableNotFoundException;
import group4.opensource_server.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 에브리타임 시간표 크롤링 및 저장 서비스
 *
 * <주요 기능>
 * 1. 에브리타임 시간표 크롤링
 * 2. 크롤링 결과를 파싱하여 구조화된 데이터 생성
 * 3. 시간표 데이터베이스 저장/조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarTimetableService {

    private final EverytimeCrawler everytimeCrawler;
    private final TimetableParser timetableParser;
    private final EveryTimeTimetableRepository timetableRepository;
    private final ObjectMapper objectMapper;

    /**
     * 에브리타임 시간표를 크롤링합니다.
     *
     * @param url 에브리타임 시간표 공유 URL
     * @return 구조화된 시간표 응답 DTO
     */
    public TimetableResponseDto crawlSchedule(String url) {
        log.info("시간표 크롤링 시작: {}", url);

        // 1. 크롤링
        EverytimeCrawler.CrawlResult crawlResult = everytimeCrawler.crawl(url);

        // 2. 파싱
        Map<String, List<CalendarTimetableDto>> scheduleMap = timetableParser.parse(crawlResult);

        // 3. DTO 변환
        TimetableResponseDto response = convertToResponseDto(scheduleMap);

        log.info("시간표 크롤링 완료: {}개 요일", scheduleMap.size());
        return response;
    }

    /**
     * 에브리타임 시간표를 크롤링하고 데이터베이스에 저장합니다.
     *
     * @param user 시간표를 저장할 사용자
     * @param url 에브리타임 시간표 공유 URL
     * @return 구조화된 시간표 응답 DTO
     */
    @Transactional
    public TimetableResponseDto crawlScheduleAndSave(User user, String url) {
        log.info("시간표 크롤링 및 저장 시작: userId={}, url={}", user.getId(), url);

        // 1. 크롤링 및 파싱
        TimetableResponseDto response = crawlSchedule(url);

        // 2. JSON 직렬화
        String json = serializePayload(response.getPayload());

        // 3. DB 저장
        saveTimetable(user, json);

        log.info("시간표 저장 완료: userId={}", user.getId());
        return response;
    }

    /**
     * 데이터베이스에 저장된 시간표를 조회합니다.
     *
     * @param user 시간표를 조회할 사용자
     * @return 저장된 시간표 응답 DTO
     * @throws TimetableNotFoundException 저장된 시간표가 없는 경우
     */
    @Transactional(readOnly = true)
    public TimetableResponseDto getSavedTimetable(User user) {
        log.info("저장된 시간표 조회: userId={}", user.getId());

        // 1. DB에서 시간표 조회
        EveryTimeTimetable timetable = timetableRepository.findByUser(user)
                .orElseThrow(() -> new TimetableNotFoundException("저장된 시간표가 없습니다"));

        // 2. JSON 역직렬화
        TimetablePayloadDto payload = deserializePayload(timetable.getTimetableJson());

        log.info("시간표 조회 완료: userId={}", user.getId());
        return new TimetableResponseDto(payload);
    }

    /**
     * 파싱된 시간표 맵을 응답 DTO로 변환합니다.
     *
     * @param scheduleMap 요일별 시간표 맵
     * @return TimetableResponseDto
     */
    private TimetableResponseDto convertToResponseDto(Map<String, List<CalendarTimetableDto>> scheduleMap) {
        List<DayScheduleDto> daySchedules = new ArrayList<>();

        for (var entry : scheduleMap.entrySet()) {
            String day = entry.getKey();
            List<SubjectDto> subjects = entry.getValue().stream()
                    .map(dto -> new SubjectDto(dto.getSubject(), dto.getTimes()))
                    .toList();

            daySchedules.add(new DayScheduleDto(day, subjects));
        }

        return TimetableResponseDto.of(daySchedules);
    }

    /**
     * Payload를 JSON 문자열로 직렬화합니다.
     *
     * @param payload 시간표 payload
     * @return JSON 문자열
     * @throws RuntimeException JSON 직렬화 실패 시
     */
    private String serializePayload(TimetablePayloadDto payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("시간표 JSON 직렬화 실패", e);
            throw new RuntimeException("시간표 저장 실패", e);
        }
    }

    /**
     * JSON 문자열을 Payload로 역직렬화합니다.
     *
     * @param json JSON 문자열
     * @return TimetablePayloadDto
     * @throws RuntimeException JSON 역직렬화 실패 시
     */
    private TimetablePayloadDto deserializePayload(String json) {
        try {
            return objectMapper.readValue(json, TimetablePayloadDto.class);
        } catch (JsonProcessingException e) {
            log.error("시간표 JSON 역직렬화 실패", e);
            throw new RuntimeException("시간표 불러오기 실패", e);
        }
    }

    /**
     * 시간표를 데이터베이스에 저장합니다.
     *
     * @param user 사용자
     * @param json 시간표 JSON 문자열
     */
    private void saveTimetable(User user, String json) {
        EveryTimeTimetable timetable = timetableRepository.findByUser(user)
                .orElse(EveryTimeTimetable.builder().user(user).build());

        timetable.updateTimetableJson(json);
        timetableRepository.save(timetable);
    }
}
