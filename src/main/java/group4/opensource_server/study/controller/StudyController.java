package group4.opensource_server.study.controller;

import group4.opensource_server.study.domain.Study;
import group4.opensource_server.study.domain.StudyRecord;
import group4.opensource_server.study.domain.StudyRecordRepository;
import group4.opensource_server.study.domain.StudyService;
import group4.opensource_server.study.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Study", description = "스터디 등록, 조회, 기록, 통계 등을 관리하는 API")
@RestController
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @Operation(summary = "스터디 생성", description = "사용자가 입력한 과목명과 색상을 기반으로 새로운 스터디를 생성합니다.")
    @PostMapping
    public ResponseEntity<StudyResponseDto> createStudy(@AuthenticationPrincipal UserDetails userDetails,@RequestBody StudyRequestDto studyRequestDto) {
        Study study = studyService.createStudy(userDetails.getUsername(),studyRequestDto);
        return ResponseEntity.ok(new StudyResponseDto(study));
    }

    @Operation(summary = "스터디 데이터 조회", description = "특정 스터디 ID에 대한 기간별 학습 기록을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<List<StudyDataResponseDto>> getStudy(@PathVariable Integer id, @RequestParam String range) {
        return ResponseEntity.ok(studyService.getStudyData(id, range));
    }

    @Operation(summary = "전체 스터디 조회", description = "현재 로그인한 사용자의 전체 스터디 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<StudyResponseDto>> getAllStudies(@AuthenticationPrincipal UserDetails userDetails) {
        List<StudyResponseDto> result = studyService.getAllStudies(userDetails.getUsername()).stream()
                .map(StudyResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "과목별 통계 조회", description = "사용자의 스터디 과목별 총 학습 시간 통계를 조회합니다.")
    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectStatsResponseDto>> getSubjectStats(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "all") String range
    ) {
        List<SubjectStatsResponseDto> stats = studyService.getSubjectStats(userDetails.getUsername(), range);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "스터디 검색", description = "과목 이름 키워드를 통해 사용자의 스터디를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<StudyResponseDto>> searchStudies(@RequestParam String subject) {
        List<Study> studies = studyService.searchBySubject(subject);
        List<StudyResponseDto> result = studies.stream()
                .map(StudyResponseDto::new)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "월별 학습 통계 조회", description = "월 단위로 사용자의 학습 시간 통계를 제공합니다.")
    @GetMapping("/stats/monthly")
    public ResponseEntity<MonthlyStudyStatsResponseDto> getMonthlyStats(@AuthenticationPrincipal UserDetails userDetails) {
        MonthlyStudyStatsResponseDto monthlyStudyStatsResponseDto = studyService.getMonthlyStats(userDetails.getUsername());
        return ResponseEntity.ok(monthlyStudyStatsResponseDto);
    }

    @Operation(summary = "스터디 수정", description = "특정 ID를 가진 스터디를 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<StudyResponseDto> updateStudy(@PathVariable Integer id, @RequestBody StudyUpdateRequestDto studyUpdateRequestDto) {
        Study study = studyService.updateStudy(id, studyUpdateRequestDto);
        return ResponseEntity.ok(new StudyResponseDto(study));
    }

    @Operation(summary = "스터디 삭제", description = "특정 ID를 가진 스터디를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudy(@PathVariable Integer id) {
        studyService.deleteStudy(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "스터디 학습 기록 추가", description = "기존 스터디에 대한 학습 시간 기록을 추가합니다.")
    @PostMapping("/records")
    public ResponseEntity<StudyDataResponseDto> createRecord(@RequestBody StudyRecordRequestDto studyRecordRequestDto) {
        return ResponseEntity.ok(studyService.createStudyRecord(studyRecordRequestDto));
    }
}