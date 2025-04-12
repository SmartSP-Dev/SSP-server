package group4.opensource_server.study.dto;

import group4.opensource_server.study.domain.Study;
import lombok.Getter;


@Getter
public class StudyResponseDto {
    private Integer id;
    private String subject;

    public StudyResponseDto(Study study) {
        this.id = study.getId();
        this.subject = study.getSubject();
    }
}
