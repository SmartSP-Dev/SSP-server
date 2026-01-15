package group4.opensource_server.calendar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 특정 요일의 시간표 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DayScheduleDto {
    /**
     * 요일 ("월", "화", "수", "목", "금")
     */
    @JsonProperty("time_point")
    private String timePoint;

    /**
     * 해당 요일의 강의 목록
     */
    private List<SubjectDto> subjects;
}
