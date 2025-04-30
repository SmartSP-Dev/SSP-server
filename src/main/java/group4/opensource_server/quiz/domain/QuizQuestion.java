package group4.opensource_server.quiz.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Builder; // Lombok의 @Builder를 사용하려면 이 import가 필요합니다.
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    private String questionTitle;
    private String questionContent;
    private int quizNumber;
    private String correctAnswer;

    @Convert(converter = StringListConverter.class)
    @Column(name = "incorrect_answer", columnDefinition = "json")
    private List<String> incorrectAnswers;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;


    @Builder
    public QuizQuestion(Quiz quiz, String questionTitle, String questionContent, int quizNumber, String correctAnswer, List<String> incorrectAnswers, QuestionType questionType) {
        this.quiz = quiz;
        this.questionTitle = questionTitle;
        this.questionContent = questionContent;
        this.quizNumber = quizNumber;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
        this.questionType = questionType;
    }
}