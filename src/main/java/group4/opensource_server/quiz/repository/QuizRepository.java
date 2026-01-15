package group4.opensource_server.quiz.repository;

import group4.opensource_server.quiz.domain.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = {"questions"})
    List<Quiz> findByUserId(Integer userId);

    // Bulk update: 복습 완료 상태(3)에서 하루 지난 퀴즈들을 복습 필요 상태(2)로 변경
    @Modifying
    @Query("UPDATE Quiz q SET q.status = 2 " +
           "WHERE q.status = 3 " +
           "AND q.lastReviewedAt IS NOT NULL " +
           "AND q.lastReviewedAt < :today")
    int updateExpiredQuizStatuses(@Param("today") LocalDate today);
}