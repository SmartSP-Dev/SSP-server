package group4.opensource_server.routine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RoutineMonthlySummaryDto {
    private LocalDate date;     // 일자
    private boolean achieved;   // true: 달성률 80% 이상
}
