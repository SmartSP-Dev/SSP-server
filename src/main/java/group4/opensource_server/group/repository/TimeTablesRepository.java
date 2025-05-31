package group4.opensource_server.group.repository;

import group4.opensource_server.group.domain.DayOfWeekEnum;
import group4.opensource_server.group.domain.TimeTables;
import group4.opensource_server.group.domain.TimeTablesId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

public interface TimeTablesRepository extends JpaRepository<TimeTables, TimeTablesId> {
    List<TimeTables> findByGroupId(int groupId);

    List<TimeTables> findByGroupIdAndMemberId(int groupId, int memberId);

    List<TimeTables> findByGroupIdAndDayOfWeekAndTimeBlock(int groupId, DayOfWeekEnum dayOfWeek, LocalTime timeBlock);

    void deleteByGroupIdAndMemberId(int groupId, int memberId);
}
