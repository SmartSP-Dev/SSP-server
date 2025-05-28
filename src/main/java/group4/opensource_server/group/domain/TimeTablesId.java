package group4.opensource_server.group.domain;

import lombok.*;

import java.io.Serializable;
import java.time.LocalTime;

import group4.opensource_server.group.domain.DayOfWeekEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TimeTablesId implements Serializable {
    private int groupId;
    private int memberId;
    private DayOfWeekEnum dayOfWeek;
    private LocalTime timeBlock;
}
