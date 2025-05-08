package group4.opensource_server.routine.service;

import group4.opensource_server.routine.domain.Routine;
import group4.opensource_server.routine.domain.RoutineRecord;
import group4.opensource_server.routine.dto.RoutineCheckRequestDto;
import group4.opensource_server.routine.dto.RoutineCreateRequestDto;
import group4.opensource_server.routine.dto.RoutineCreateResponseDto;
import group4.opensource_server.routine.repository.RoutineRepository;
import group4.opensource_server.routine.repository.RoutineRecordRepository;
import group4.opensource_server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

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
}