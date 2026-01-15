package group4.opensource_server.quiz.dto;

import group4.opensource_server.quiz.domain.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "퀴즈 생성 요청")
public class QuizGenerateRequestDto {

    @NotBlank(message = "제목은 필수입니다")
    @Schema(description = "퀴즈 제목", example = "자바 기초 퀴즈")
    private String title;

    @NotBlank(message = "키워드는 필수입니다")
    @Schema(description = "퀴즈 키워드", example = "자바")
    private String keyword;

    @NotNull(message = "문제 유형은 필수입니다")
    @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE")
    private QuestionType questionType;
}
