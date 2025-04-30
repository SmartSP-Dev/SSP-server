package group4.opensource_server.study.controller;

import group4.opensource_server.study.domain.Study;
import group4.opensource_server.study.domain.StudyRecord;
import group4.opensource_server.study.domain.StudyRecordRepository;
import group4.opensource_server.study.domain.StudyService;
import group4.opensource_server.study.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @PostMapping
    public ResponseEntity<StudyResponseDto> createStudy(@AuthenticationPrincipal UserDetails userDetails,@RequestBody StudyRequestDto studyRequestDto) {
        Study study = studyService.createStudy(userDetails.getUsername(),studyRequestDto);
        return ResponseEntity.ok(new StudyResponseDto(study));
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<StudyDataResponseDto>> getStudy(@PathVariable Integer id, @RequestParam String range) {
        return ResponseEntity.ok(studyService.getStudyData(id, range));
    }

    @GetMapping
    public ResponseEntity<List<StudyResponseDto>> getAllStudies() {
        List<StudyResponseDto> result = studyService.getAllStudies().stream()
                .map(StudyResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectStatsResponseDto>> getSubjectStats(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "all") String range
    ) {
        List<SubjectStatsResponseDto> stats = studyService.getSubjectStats(userDetails.getUsername(), range);
        return ResponseEntity.ok(stats);
    }


    @GetMapping("/search")
    public ResponseEntity<List<StudyResponseDto>> searchStudies(@RequestParam String subject) {
        List<Study> studies = studyService.searchBySubject(subject);
        List<StudyResponseDto> result = studies.stream()
                .map(StudyResponseDto::new)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats/monthly")
    public ResponseEntity<MonthlyStudyStatsResponseDto> getMonthlyStats(@AuthenticationPrincipal UserDetails userDetails) {
        MonthlyStudyStatsResponseDto monthlyStudyStatsResponseDto = studyService.getMonthlyStats(userDetails.getUsername());
        return ResponseEntity.ok(monthlyStudyStatsResponseDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<StudyResponseDto> updateStudy(@PathVariable Integer id, @RequestBody StudyUpdateRequestDto studyUpdateRequestDto) {
        Study study = studyService.updateStudy(id, studyUpdateRequestDto);
        return ResponseEntity.ok(new StudyResponseDto(study));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudy(@PathVariable Integer id) {
        studyService.deleteStudy(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/records")
    public ResponseEntity<StudyDataResponseDto> createRecord(@RequestBody StudyRecordRequestDto studyRecordRequestDto) {
        return ResponseEntity.ok(studyService.createStudyRecord(studyRecordRequestDto));
    }
}
