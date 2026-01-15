package group4.opensource_server.quiz.dto;

import group4.opensource_server.quiz.domain.Quiz;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizListDto {
    private Long quizId;
    private String title;
    private String keywords;
    private String questionType;
    private LocalDate createdAt;
    private Boolean isReviewed;

    public static QuizListDto fromEntity(Quiz quiz) {
        return new QuizListDto(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getKeywords(),
                quiz.getQuestionType().name(),
                quiz.getCreatedAt(),
                quiz.isReviewed()
        );
    }
}
