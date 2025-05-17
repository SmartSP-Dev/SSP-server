package group4.opensource_server.quiz.repository;

import group4.opensource_server.quiz.domain.QuestionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionResultRepository extends JpaRepository<QuestionResult, Long> {
}