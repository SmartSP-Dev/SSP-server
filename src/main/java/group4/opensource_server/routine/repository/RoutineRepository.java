package group4.opensource_server.routine.repository;

import group4.opensource_server.routine.domain.Routine;
import group4.opensource_server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    // 삭제되지 않고, 시작일 이전이며, 활성 상태인 루틴 조회
    List<Routine> findAllByUserAndIsActiveTrueAndStartedAtLessThanEqualAndDeletedAtIsNull(User user, LocalDate date);

    @Query("""
    SELECT COUNT(r) FROM Routine r
    WHERE r.user = :user
    AND r.isActive = true
    AND r.startedAt <= :date
    AND (r.deletedAt IS NULL OR r.deletedAt > :date)
    """)
    long countByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
}