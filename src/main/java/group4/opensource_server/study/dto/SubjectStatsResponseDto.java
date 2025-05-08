package group4.opensource_server.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubjectStatsResponseDto  {
    private Integer studyId;
    private String subject;
    private long totalStudyTime;
}