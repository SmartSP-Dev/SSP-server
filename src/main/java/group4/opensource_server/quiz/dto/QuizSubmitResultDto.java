package group4.opensource_server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class QuizSubmitResultDto {

    private int totalQuestions;
    private int correctAnswers;

    private List<QuestionResult> questionResults;

    @Data
    @AllArgsConstructor
    public static class QuestionResult {
        private int quizNumber;         // 변경: quizNumber 사용
        private String questionTitle;
        private String userAnswer;
        private String correctAnswer;
        private boolean isCorrect;
    }
}