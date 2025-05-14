package group4.opensource_server.quiz.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "question_results")
public class QuestionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 시도에 속했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    private QuizAttempt quizAttempt;

    // 어떤 퀴즈에 속한 문제인지 (추적용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // 어떤 문제인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_questions_id", nullable = false)
    private QuizQuestion quizQuestion;

    // 사용자의 답
    @Lob
    private String userAnswer;

    // 정답 여부
    private boolean isCorrect;
}