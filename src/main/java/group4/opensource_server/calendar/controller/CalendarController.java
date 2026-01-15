package group4.opensource_server.calendar.controller;

import group4.opensource_server.calendar.dto.TimetableResponseDto;
import group4.opensource_server.calendar.service.CalendarTimetableService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarTimetableService calendarTimetableService;
    private final UserRepository userRepository;

    @Operation(summary = "에브리타임 시간표 크롤링 및 저장",
            description = "에브리타임 URL을 입력받아 시간표를 크롤링하고 해당 유저에게 저장합니다.")
    @PostMapping("/timetable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TimetableResponseDto> crawlAndSaveTimetable(@RequestParam String url) {
        User user = getCurrentUser();
        TimetableResponseDto response = calendarTimetableService.crawlScheduleAndSave(user, url);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "저장된 시간표 조회",
            description = "로그인한 사용자의 저장된 시간표를 반환합니다.")
    @GetMapping("/timetable/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TimetableResponseDto> getSavedTimetable() {
        User user = getCurrentUser();
        TimetableResponseDto response = calendarTimetableService.getSavedTimetable(user);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 사용자를 조회합니다.
     *
     * @return 현재 사용자
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다: " + email));
    }
}