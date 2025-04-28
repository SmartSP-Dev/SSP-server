package group4.opensource_server.quiz.repository;

import group4.opensource_server.quiz.domain.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    // 특정 퀴즈에 속한 문제 목록을 조회
    List<QuizQuestion> findByQuizId(Long quizId);
}