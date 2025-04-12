package group4.opensource_server.study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
@Getter
@AllArgsConstructor
@Builder
public class StudyDataResponseDto {
    private LocalDate date;
    private Long time;
}
