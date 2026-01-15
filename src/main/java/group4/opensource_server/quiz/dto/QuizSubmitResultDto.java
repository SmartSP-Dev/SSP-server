package group4.opensource_server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class QuizSubmitResultDto {

    private int totalQuestions;
    private int correctAnswers;
    private List<QuestionResult> questionResults;

    @Getter
    @AllArgsConstructor
    public static class QuestionResult {
        private int quizNumber;
        private String questionTitle;
        private String userAnswer;
        private String correctAnswer;
        private boolean isCorrect;
    }
}