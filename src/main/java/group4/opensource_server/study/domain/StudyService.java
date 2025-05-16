package group4.opensource_server.study.domain;

import group4.opensource_server.study.dto.*;
import group4.opensource_server.study.exception.StudyNotFoundException;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import group4.opensource_server.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final UserRepository userRepository;

    @Transactional
    public Study createStudy(String email, StudyRequestDto studyRequestDto) {
        User user=userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("없는 유저입니다."));
        return studyRepository.save(Study.builder()
                .subject(studyRequestDto.getSubject())
                .user(user)
                .build());
    }

    public Study getStudy(Integer id) {
        return studyRepository.findById(id)
                .orElseThrow(() -> new StudyNotFoundException("존재하지 않는 스터디입니다."));
    }

    public MonthlyStudyStatsResponseDto getMonthlyStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("없는 유저입니다."));

        Integer userId = user.getId();
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        List<StudyRecord> records = studyRecordRepository.findByStudyUserIdAndDateBetween(userId, startOfMonth, endOfMonth);

        Map<LocalDate, Long> dailyTotals = new HashMap<>();
        long totalTime = 0;

        for (StudyRecord record : records) {
            LocalDate date = record.getDate();
            long time = record.getTime();

            dailyTotals.put(date, dailyTotals.getOrDefault(date, 0L) + time);
            totalTime += time;
        }

        int studyDay = 0;
        long maxTime = 0;
        LocalDate maxDay = null;

        for (Map.Entry<LocalDate, Long> entry : dailyTotals.entrySet()) {
            long daily = entry.getValue();
            if (daily >= 1) studyDay++;
            if (daily > maxTime) {
                maxTime = daily;
                maxDay = entry.getKey();
            }
        }

        long average = studyDay == 0 ? 0 : totalTime / studyDay;

        return new MonthlyStudyStatsResponseDto(studyDay, totalTime, average, maxTime, maxDay);
    }

    public List<Study> getAllStudies(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("없는 유저입니다."));
        Integer userId = user.getId();
        return studyRepository.findByUserId(userId);
    }

    public List<SubjectStatsResponseDto> getSubjectStats(String email, String range) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("없는 유저입니다."));
        Integer userId = user.getId();

        LocalDate startDate;
        LocalDate endDate = LocalDate.now();

        if ("week".equalsIgnoreCase(range)) {
            startDate = endDate.with(java.time.DayOfWeek.MONDAY);
        } else {
            startDate = LocalDate.of(2000, 1, 1); // 충분히 과거부터 시작 (전체)
        }

        List<Study> studies = studyRepository.findByUserId(userId);
        List<StudyRecord> records = studyRecordRepository.findByStudyUserIdAndDateBetween(userId, startDate, endDate);

        Map<Integer, Long> timeMap = new HashMap<>();
        for (StudyRecord record : records) {
            Integer studyId = record.getStudy().getId();
            long time = record.getTime();
            timeMap.put(studyId, timeMap.getOrDefault(studyId, 0L) + time);
        }

        List<SubjectStatsResponseDto> result = new ArrayList<>();
        for (Study study : studies) {
            long time = timeMap.getOrDefault(study.getId(), 0L);
            result.add(new SubjectStatsResponseDto(study.getId(), study.getSubject(), time));
        }

        return result;
    }

    public List<StudyDataResponseDto> getStudyData(Integer studyId, String range) {
        LocalDate fromDate = switch (range.toLowerCase()) {
            case "week" -> LocalDate.now().minusDays(6);
            case "month" -> LocalDate.now().minusDays(29);
            case "all" -> LocalDate.of(2000, 1, 1);
            default ->  throw new IllegalArgumentException("invalid range: " + range);
        };

        return studyRecordRepository.getStudyData(studyId, fromDate).stream()
                .map(s -> new StudyDataResponseDto(s.getDate(), s.getTotalTime()))
                .toList();
    }

    public List<Study> searchBySubject(String subject) {
        return studyRepository.findBySubjectContaining(subject);
    }
    @Transactional
    public Study updateStudy(Integer id, StudyUpdateRequestDto studyUpdateRequestDto) {
        Study study = getStudy(id);
        study.setSubject(studyUpdateRequestDto.getSubject());
        return study;
    }

    @Transactional
    public void deleteStudy(Integer id) {
        studyRepository.deleteById(id);
    }

    @Transactional
    public StudyDataResponseDto createStudyRecord(String email, StudyRecordRequestDto studyRecordRequestDto) {
        Study study = studyRepository.findById(studyRecordRequestDto.getStudyId())
                .orElseThrow(() -> new StudyNotFoundException("존재하지 않는 스터디입니다."));
        if(study.getUser().getEmail().equals(email)) {
            StudyRecord studyRecord = studyRecordRepository.save(StudyRecord.builder()
                    .study(study)
                    .date(studyRecordRequestDto.getDate())
                    .time(studyRecordRequestDto.getTime())
                    .build());
            return StudyDataResponseDto.builder()
                    .date(studyRecord.getDate())
                    .time(studyRecord.getTime().longValue())
                    .build();
        }
        else {
            return StudyDataResponseDto.builder().build();
        }
    }
}
