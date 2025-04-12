package group4.opensource_server.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
@Getter
@AllArgsConstructor
public class StudyRecordResponseDto {
    private Integer studyId;
    private String subject;
    private LocalDate date;
    private Integer time;
}
