package group4.opensource_server.study.dto;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
@Getter
public class StudyRecordRequestDto {
    private Integer studyId;
    private LocalDate date;
    private Integer time;
}