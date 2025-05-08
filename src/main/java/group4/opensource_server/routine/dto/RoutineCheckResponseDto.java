package group4.opensource_server.routine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RoutineCheckResponseDto {
    private Long routineId;
    private LocalDate date;
    private String status; // "checked" 또는 "unchecked"
}