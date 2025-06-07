package group4.opensource_server.routine.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RoutineCheckRequestDto {
    private Long routineId;
    private LocalDate date;
    private Boolean completed;
}