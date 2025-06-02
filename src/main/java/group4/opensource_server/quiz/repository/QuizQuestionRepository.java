package group4.opensource_server.quiz.repository;

import group4.opensource_server.quiz.domain.Quiz;
import group4.opensource_server.quiz.domain.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    // 특정 퀴즈에 속한 문제 목록을 조회
    List<QuizQuestion> findByQuizId(Long quizId);

    Optional<QuizQuestion> findByQuizAndQuizNumber(Quiz quiz, int quizNumber);

    void deleteByQuiz(Quiz quiz);
}