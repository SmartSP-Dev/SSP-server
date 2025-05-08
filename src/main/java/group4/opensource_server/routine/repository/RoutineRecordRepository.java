package group4.opensource_server.routine.repository;

import group4.opensource_server.routine.domain.Routine;
import group4.opensource_server.routine.domain.RoutineRecord;
import group4.opensource_server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutineRecordRepository extends JpaRepository<RoutineRecord, Long> {

    // 해당 루틴의 특정 날짜 기록 조회
    Optional<RoutineRecord> findByRoutineAndDate(Routine routine, LocalDate date);

    // 특정 유저의 특정 날짜 전체 기록 조회
    List<RoutineRecord> findByUserAndDate(User user, LocalDate date);

    // 루틴 아이디 + 날짜로 존재 확인
    boolean existsByRoutineAndDate(Routine routine, LocalDate date);

    // 월별 기록 조회 (달성률 계산용)
    List<RoutineRecord> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    // 특정 날짜의 루틴 체크 기록 조회
    Optional<RoutineRecord> findByUserAndRoutineAndDate(User user, Routine routine, LocalDate date);
}