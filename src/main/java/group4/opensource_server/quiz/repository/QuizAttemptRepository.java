package group4.opensource_server.quiz.repository;

import group4.opensource_server.quiz.domain.Quiz;
import group4.opensource_server.quiz.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findTopByQuizIdAndUserIdOrderByAttemptTimeDesc(Long quizId, Integer userId);

    List<QuizAttempt> findByQuiz(Quiz quiz);

    void deleteByQuiz(Quiz quiz);
}