package group4.opensource_server.routine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoutineDayResponseDto {
    private Long routineId;
    private String title;
    private Boolean completed; // true/false
}
