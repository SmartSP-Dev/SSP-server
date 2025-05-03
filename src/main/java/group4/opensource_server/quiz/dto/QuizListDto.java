package group4.opensource_server.quiz.dto;


import group4.opensource_server.quiz.domain.Quiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QuizListDto {
    private Long id;
    private String title;
    private String keyword;

    public static QuizListDto fromEntity(Quiz quiz) {
        return QuizListDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .keyword(quiz.getKeywords())
                .build();
    }
}
