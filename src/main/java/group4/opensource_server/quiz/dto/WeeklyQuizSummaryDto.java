package group4.opensource_server.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeeklyQuizSummaryDto {
    private long total;
    private long reviewed;
    private long notReviewed;
}