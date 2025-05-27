package group4.opensource_server.group.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TimeTable {
    private List<TimeBlock> timeBlocks;

    public TimeTable() {
        this.timeBlocks = setTimeTable(); // 30 * 7 = 210개
    }

    public static TimeTable createTimeTable() {
        return new TimeTable();
    }

    private List<TimeBlock> setTimeTable() {
        List<TimeBlock> blocks = new ArrayList<>();
        String[] days = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

        for (int h = 8; h < 23; h++) {
            for (int m = 0; m <= 30; m += 30) {
                LocalTime time = LocalTime.of(h, m);

                for (String day : days) {
                    blocks.add(new TimeBlock(day, time, 0, new ArrayList<>()));
                }
            }
        }

        return blocks;
    }

    public List<TimeBlock> getTimeBlocks() {
        return this.timeBlocks;
    }
}

