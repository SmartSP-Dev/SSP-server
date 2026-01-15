package group4.opensource_server.quiz.repository;

import group4.opensource_server.quiz.domain.Quiz;
import group4.opensource_server.quiz.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findTopByQuizIdAndUserIdOrderByAttemptTimeDesc(Long quizId, Integer userId);

    List<QuizAttempt> findByQuiz(Quiz quiz);

    // Bulk delete: 특정 퀴즈에 속한 모든 시도 삭제
    @Modifying
    @Query("DELETE FROM QuizAttempt qa WHERE qa.quiz = :quiz")
    void deleteByQuiz(@Param("quiz") Quiz quiz);
}