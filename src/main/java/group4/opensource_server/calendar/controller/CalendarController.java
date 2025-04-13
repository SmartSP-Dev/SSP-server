package group4.opensource_server.calendar.controller;

import group4.opensource_server.calendar.domain.CalendarTimetableService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarTimetableService calendarTimetableService;

    public CalendarController(CalendarTimetableService calendarTimetableService) {
        this.calendarTimetableService = calendarTimetableService;
    }

    @GetMapping("/timetable")
    public Map<String, Object> getSchedule(@RequestParam String url) {
        return calendarTimetableService.crawlSchedule(url);
    }
}