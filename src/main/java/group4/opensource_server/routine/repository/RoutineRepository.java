package group4.opensource_server.routine.repository;

import group4.opensource_server.routine.domain.Routine;
import group4.opensource_server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    // 특정 유저의 전체 루틴 목록
    List<Routine> findByUser(User user);

    // 특정 날짜 기준으로 보여줄 루틴 리스트
    List<Routine> findByUserAndIsActiveTrueAndStartedAtLessThanEqualAndDeletedAtIsNullOrDeletedAtGreaterThan(
            User user, LocalDate date1, LocalDate date2
    );
}