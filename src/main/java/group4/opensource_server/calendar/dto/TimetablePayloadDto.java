package group4.opensource_server.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 시간표 응답의 payload DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TimetablePayloadDto {
    /**
     * 요일별 시간표 목록
     */
    private List<DayScheduleDto> schedules;
}
