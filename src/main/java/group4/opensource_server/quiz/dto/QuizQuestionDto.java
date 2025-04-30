package group4.opensource_server.quiz.dto;

import group4.opensource_server.quiz.domain.QuestionType;
import group4.opensource_server.quiz.domain.QuizQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionDto {
    private Long id;
    private String questionTitle;
    private String questionContent;
    private int quizNumber;
    private String correctAnswer;
    private List<String> incorrectAnswers;
    private QuestionType questionType;

    public static QuizQuestionDto fromEntity(QuizQuestion question) {
        return QuizQuestionDto.builder()
                .id(question.getId())
                .questionTitle(question.getQuestionTitle())
                .questionContent(question.getQuestionContent())
                .quizNumber(question.getQuizNumber())
                .correctAnswer(question.getCorrectAnswer())
                .incorrectAnswers(question.getIncorrectAnswers())
                .questionType(question.getQuestionType())
                .build();
    }
}
