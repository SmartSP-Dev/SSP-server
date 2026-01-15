package group4.opensource_server.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시간표 API 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TimetableResponseDto {
    /**
     * 시간표 데이터를 포함하는 payload
     */
    private TimetablePayloadDto payload;

    /**
     * Map 형태의 시간표 데이터를 DTO로 변환하는 팩토리 메소드
     *
     * @param schedules 요일별 시간표 목록
     * @return TimetableResponseDto 인스턴스
     */
    public static TimetableResponseDto of(java.util.List<DayScheduleDto> schedules) {
        return new TimetableResponseDto(new TimetablePayloadDto(schedules));
    }
}
