package group4.opensource_server.routine.service;

import group4.opensource_server.routine.domain.Routine;
import group4.opensource_server.routine.domain.RoutineRecord;
import group4.opensource_server.routine.dto.*;
import group4.opensource_server.routine.repository.RoutineRepository;
import group4.opensource_server.routine.repository.RoutineRecordRepository;
import group4.opensource_server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final RoutineRecordRepository routineRecordRepository;

    public RoutineCreateResponseDto createRoutine(User user, RoutineCreateRequestDto request) {
        Routine routine = Routine.builder()
                .user(user)
                .title(request.getTitle())
                .isActive(true)
                .createdAt(LocalDate.now())
                .startedAt(LocalDate.now())
                .deletedAt(null)
                .build();

        Routine saved = routineRepository.save(routine);
        return new RoutineCreateResponseDto(saved.getId(), saved.getTitle());
    }

    public void deleteRoutine(User user, Long routineId) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("루틴이 존재하지 않습니다."));

        // 소유자 확인
        if (!routine.getUser().getId().equals(user.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        // soft delete 처리
        routine.setDeletedAt(LocalDate.now());
        routine.setIsActive(false);

        routineRepository.save(routine);
    }

    public void checkRoutine(User user, RoutineCheckRequestDto request) {
        Routine routine = routineRepository.findById(request.getRoutineId())
                .orElseThrow(() -> new IllegalArgumentException("루틴이 존재하지 않습니다."));

        if (!routine.getUser().getId().equals(user.getId())) {
            throw new SecurityException("해당 루틴에 대한 권한이 없습니다.");
        }

        // 이미 해당 날짜에 기록이 있는지 확인
        Optional<RoutineRecord> recordOpt = routineRecordRepository
                .findByUserAndRoutineAndDate(user, routine, request.getDate());

        if (recordOpt.isPresent()) {
            // 체크 상태만 수정
            RoutineRecord record = recordOpt.get();
            record.setCompleted(request.getCompleted());
            routineRecordRepository.save(record);
        } else {
            // 처음 체크 -> 체크 기록 새로 추가
            RoutineRecord newRecord = RoutineRecord.builder()
                    .user(user)
                    .routine(routine)
                    .date(request.getDate())
                    .completed(request.getCompleted())
                    .build();
            routineRecordRepository.save(newRecord);
        }
    }

    public List<RoutineDayResponseDto> getRoutinesByDate(User user, LocalDate date) {
        // 삭제되지 않고, 시작일 이전이며, 활성 상태인 루틴 조회
        List<Routine> routines = routineRepository.findAllByUserAndIsActiveTrueAndStartedAtLessThanEqualAndDeletedAtIsNull(user, date);

        return routines.stream().map(routine -> {
            boolean isChecked = routineRecordRepository
                    .findByUserAndRoutineAndDate(user, routine, date)
                    .map(RoutineRecord::getCompleted)
                    .orElse(false);

            return new RoutineDayResponseDto(routine.getId(), routine.getTitle(), isChecked);
        }).toList();
    }

    public List<RoutineMonthlySummaryDto> getMonthlySummaryForUser(User user) {
        // 1. 루틴 기록 전체 기간 조회
        Optional<LocalDate> optStart = routineRecordRepository.findEarliestDateByUser(user);
        Optional<LocalDate> optEnd = routineRecordRepository.findLatestDateByUser(user);

        if (optStart.isEmpty() || optEnd.isEmpty()) return List.of(); // 기록이 없음

        LocalDate start = optStart.get();
        LocalDate end = optEnd.get();

        // 2. 해당 기간의 모든 기록 조회
        List<RoutineRecord> allRecords = routineRecordRepository.findByUserAndDateBetween(user, start, end);

        // 3. 날짜별 그룹핑
        Map<LocalDate, List<RoutineRecord>> recordsByDate = allRecords.stream()
                .collect(Collectors.groupingBy(RoutineRecord::getDate));

        List<RoutineMonthlySummaryDto> result = new ArrayList<>();

        for (LocalDate date : recordsByDate.keySet()) {
            List<RoutineRecord> records = recordsByDate.get(date);

            long completedCount = records.stream()
                    .filter(RoutineRecord::getCompleted)
                    .count();

            // 루틴 개수는 Routine 테이블 기준으로 계산 (startedAt / deletedAt 조건 포함)
            long totalCount = routineRepository.countByUserAndDate(user, date);

            boolean achieved = totalCount > 0 && ((double) completedCount / totalCount) >= 0.8;

            result.add(new RoutineMonthlySummaryDto(date, achieved));
        }

        return result;
    }
}