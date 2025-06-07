package group4.opensource_server.routine.controller;

import group4.opensource_server.routine.dto.*;
import group4.opensource_server.routine.service.RoutineService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Routine", description = "루틴 생성, 조회, 삭제, 체크 기능 API")
public class RoutineController {

    private final RoutineService routineService;
    private final UserRepository userRepository;

    @Operation(
            summary = "루틴 생성",
            description = "루틴 제목과 요일 정보를 받아 새로운 루틴을 생성합니다."
    )
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

    @Operation(
            summary = "루틴 삭제",
            description = "루틴 ID를 통해 특정 루틴을 삭제 처리합니다."
    )
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


    @Operation(
            summary = "루틴 체크/해제",
            description = "특정 날짜에서 사용자가 해당 루틴을 체크 또는 체크해제 합니다."
    )
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


    @Operation(
            summary = "날짜별 루틴 목록 조회",
            description = "특정 날짜에 해당하는 루틴 리스트를 반환합니다."
    )
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

    @Operation(
            summary = "루틴 월간 요약 조회",
            description = "현재 사용자의 월별 루틴 달성 요약을 반환합니다."
    )
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