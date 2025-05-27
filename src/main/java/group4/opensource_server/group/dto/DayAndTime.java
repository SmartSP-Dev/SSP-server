package group4.opensource_server.group.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DayAndTime {
    String dayOfWeek; // "MON", "TUE", ...
    LocalTime time;
}
