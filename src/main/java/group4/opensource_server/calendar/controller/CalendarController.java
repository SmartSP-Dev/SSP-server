package group4.opensource_server.calendar.controller;

import group4.opensource_server.calendar.domain.CalendarTimetableService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarTimetableService calendarTimetableService;
    private final UserRepository userRepository;

    public CalendarController(CalendarTimetableService calendarTimetableService,
                              UserRepository userRepository) {
        this.calendarTimetableService = calendarTimetableService;
        this.userRepository = userRepository;
    }

    // 크롤링 + 저장
    @Operation(summary = "에브리타임 시간표 크롤링 및 저장",
            description = "에브리타임 URL을 입력받아 시간표를 크롤링하고 해당 유저에게 저장합니다.")
    @PostMapping("/timetable")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> crawlAndSaveTimetable(@RequestParam String url) {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다: " + email));
        return calendarTimetableService.crawlScheduleAndSave(user, url);
    }

    // 저장된 시간표 조회
    @Operation(summary = "저장된 시간표 조회",
            description = "로그인한 사용자의 저장된 시간표를 반환합니다.")
    @GetMapping("/timetable/my")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> getSavedTimetable() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다: " + email));
        return calendarTimetableService.getSavedTimetable(user);
    }

    // 현재 로그인한 유저 이메일 추출
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // UserDetails.getUsername()과 동일
    }
}