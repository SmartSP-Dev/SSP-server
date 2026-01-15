package group4.opensource_server.calendar.domain;

import group4.opensource_server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EveryTimeTimetableRepository extends JpaRepository<EveryTimeTimetable, Long> {
    Optional<EveryTimeTimetable> findByUser(User user);
}