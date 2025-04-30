package group4.opensource_server.quiz.repository;

import group4.opensource_server.quiz.domain.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = {"questions"})
    List<Quiz> findByUserId(Integer userId);
}