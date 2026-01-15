package group4.opensource_server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyQuizSummaryDto {
    private long total;
    private long reviewed;
    private long notReviewed;
}