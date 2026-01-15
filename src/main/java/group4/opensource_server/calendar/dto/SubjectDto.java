package group4.opensource_server.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 개별 강의 정보 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {
    /**
     * 강의명
     */
    private String subject;

    /**
     * 15분 단위 시간 슬롯 목록
     * 예: ["09:00", "09:15", "09:30", "09:45"]
     */
    private List<String> times;
}
