package group4.opensource_server.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MonthlyStudyStatsResponseDto {
    private int studyDay;
    private long studyTime;
    private long averageStudyTime;
    private long maxStudyTime;
    private LocalDate maxStudyDay;
}