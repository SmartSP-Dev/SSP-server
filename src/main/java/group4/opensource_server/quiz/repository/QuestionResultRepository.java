package group4.opensource_server.quiz.repository;

import group4.opensource_server.quiz.domain.QuestionResult;
import group4.opensource_server.quiz.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionResultRepository extends JpaRepository<QuestionResult, Long> {
    // 특정 퀴즈 시도(attempt)에 대한 모든 문항 결과를 불러오기
    List<QuestionResult> findByQuizAttemptId(Long quizAttemptId);

    void deleteByQuizAttempt(QuizAttempt attempt);
}