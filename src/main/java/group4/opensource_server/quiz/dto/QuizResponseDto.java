package group4.opensource_server.quiz.dto;

import group4.opensource_server.quiz.domain.Quiz;
import group4.opensource_server.quiz.dto.QuizQuestionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class QuizResponseDto {
    private Long id;
    private String title;
    private String summary;
    private String keyword;
    private List<QuizQuestionDto> questions;

    public static QuizResponseDto fromEntity(Quiz quiz) {
        return QuizResponseDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .keyword(quiz.getKeywords())
                .questions(
                    quiz.getQuestions().stream()
                        .map(q -> QuizQuestionDto.builder()
                            .id(q.getId())
                            .questionTitle(q.getQuestionTitle())
                            .questionContent(q.getQuestionContent())
                            .quizNumber(q.getQuizNumber())
                            .correctAnswer(q.getCorrectAnswer())
                            .incorrectAnswers(new java.util.ArrayList<>(q.getIncorrectAnswers()))
                            .questionType(q.getQuestionType())
                            .build()
                        )
                        .collect(Collectors.toList())
                )
                .build();
    }
}
