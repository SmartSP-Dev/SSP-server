package group4.opensource_server.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CalendarTimetableDto {
    private List<String> times;
    private String subject;
}
