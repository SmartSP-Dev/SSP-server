package group4.opensource_server.quiz.domain;

import jakarta.persistence.*;
import lombok.Builder; // Lombok의 @Builder를 사용하려면 이 import가 필요합니다.
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private String questionTitle;
    private String questionContent;
    private int quizNumber;
    private String correctAnswer;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    public QuizQuestion() {
    }

    // @Builder 어노테이션 추가
    @Builder
    public QuizQuestion(Quiz quiz, String questionTitle, String questionContent, int quizNumber, String correctAnswer, QuestionType questionType) {
        this.quiz = quiz;
        this.questionTitle = questionTitle;
        this.questionContent = questionContent;
        this.quizNumber = quizNumber;
        this.correctAnswer = correctAnswer;
        this.questionType = questionType;
    }
}