package group4.opensource_server.routine.repository;

import group4.opensource_server.routine.domain.Routine;
import group4.opensource_server.routine.domain.RoutineRecord;
import group4.opensource_server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutineRecordRepository extends JpaRepository<RoutineRecord, Long> {
    // 특정 날짜의 루틴 체크 기록 조회
    Optional<RoutineRecord> findByUserAndRoutineAndDate(User user, Routine routine, LocalDate date);

    @Query("SELECT MIN(r.date) FROM RoutineRecord r WHERE r.user = :user")
    Optional<LocalDate> findEarliestDateByUser(User user);

    @Query("SELECT MAX(r.date) FROM RoutineRecord r WHERE r.user = :user")
    Optional<LocalDate> findLatestDateByUser(User user);

    List<RoutineRecord> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
}