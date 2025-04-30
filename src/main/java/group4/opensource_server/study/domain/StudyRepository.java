package group4.opensource_server.study.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudyRepository extends JpaRepository<Study, Integer> {
    List<Study> findBySubjectContaining(String subject);
    List<Study> findByUserId(Integer userId);
}