package group4.opensource_server.group.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import group4.opensource_server.group.domain.TimeTablesId;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "timetables")
@IdClass(TimeTablesId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeTables {
    @Id
    @Column(name = "group_id", nullable = false)
    private int groupId;

    @Id
    @Column(name = "member_id", nullable = false)
    private int memberId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 3)
    private DayOfWeekEnum dayOfWeek;

    @Id
    @Column(name = "time_block", nullable = false)
    private LocalTime timeBlock;

    @Column(name = "weight", nullable = false)
    private int weight;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "available", nullable = false)
    private boolean available;

}
