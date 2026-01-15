package group4.opensource_server.quiz.repository;

import group4.opensource_server.quiz.domain.QuestionResult;
import group4.opensource_server.quiz.domain.Quiz;
import group4.opensource_server.quiz.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionResultRepository extends JpaRepository<QuestionResult, Long> {
    // 특정 퀴즈 시도(attempt)에 대한 모든 문항 결과를 불러오기
    List<QuestionResult> findByQuizAttemptId(Long quizAttemptId);

    void deleteByQuizAttempt(QuizAttempt attempt);

    // Bulk delete: 특정 퀴즈에 속한 모든 문항 결과 삭제
    @Modifying
    @Query("DELETE FROM QuestionResult qr WHERE qr.quizAttempt.quiz = :quiz")
    void deleteByQuiz(@Param("quiz") Quiz quiz);
}