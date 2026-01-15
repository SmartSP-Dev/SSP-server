package group4.opensource_server.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "퀴즈 생성 응답")
public class QuizGenerateResponseDto {

    @Schema(description = "생성된 퀴즈 ID")
    private Long quizId;

    @Schema(description = "퀴즈 제목")
    private String title;

    @Schema(description = "생성된 문제 수")
    private int questionCount;

    @Schema(description = "상태 메시지")
    private String message;
}
