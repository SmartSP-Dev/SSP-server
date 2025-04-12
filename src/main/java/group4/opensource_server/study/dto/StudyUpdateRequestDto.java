package group4.opensource_server.study.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class StudyUpdateRequestDto {
    private String subject;
}