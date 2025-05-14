package group4.opensource_server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitRequestDto {
    private Long quizId;
    private Integer userId;

    private List<AnswerDto> answers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDto {
        private int quizNumber;
        private String userAnswer;
    }
}