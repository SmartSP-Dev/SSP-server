package group4.opensource_server.routine.controller;

import group4.opensource_server.routine.dto.*;
import group4.opensource_server.routine.service.RoutineService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/routines")
public class RoutineController {

    private final RoutineService routineService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoutineCreateResponseDto> createRoutine(
            @RequestBody RoutineCreateRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 현재 로그인 유저 가져오기
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));

        // 루틴 생성 로직 수행
        RoutineCreateResponseDto response = routineService.createRoutine(currentUser, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{routineId}/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteRoutine(
            @PathVariable Long routineId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));

        try {
            routineService.deleteRoutine(currentUser, routineId);
            return ResponseEntity.ok("해당 루틴이 삭제 처리되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body("해당 루틴(ID: " + routineId + ")이 존재하지 않습니다.");
        }
    }

    @PatchMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoutineCheckResponseDto> checkRoutine(
            @RequestBody RoutineCheckRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));

        routineService.checkRoutine(currentUser, request);

        String status = request.getCompleted() ? "checked" : "unchecked";
        RoutineCheckResponseDto response = new RoutineCheckResponseDto(
                request.getRoutineId(),
                request.getDate(),
                status
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoutineDayResponseDto>> getRoutinesByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음"));

        List<RoutineDayResponseDto> routines = routineService.getRoutinesByDate(currentUser, date);
        return ResponseEntity.ok(routines);
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoutineMonthlySummaryDto>> getRoutineSummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 유저 없음"));

        List<RoutineMonthlySummaryDto> result = routineService.getMonthlySummaryForUser(currentUser);
        return ResponseEntity.ok(result);
    }
}