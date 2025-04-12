package group4.opensource_server.study.controller;

import group4.opensource_server.study.domain.Study;
import group4.opensource_server.study.domain.StudyRecord;
import group4.opensource_server.study.domain.StudyService;
import group4.opensource_server.study.dto.StudyRecordRequestDto;
import group4.opensource_server.study.dto.StudyRequestDto;
import group4.opensource_server.study.dto.StudyResponseDto;
import group4.opensource_server.study.dto.StudyUpdateRequestDto;
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
    public ResponseEntity<StudyResponseDto> getStudy(@PathVariable Integer id) {
        Study study = studyService.getStudy(id);
        return ResponseEntity.ok(new StudyResponseDto(study));
    }


    @GetMapping
    public ResponseEntity<List<StudyResponseDto>> getAllStudies() {
        List<StudyResponseDto> result = studyService.getAllStudies().stream()
                .map(StudyResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudyResponseDto>> searchStudies(@RequestParam String subject) {
        List<Study> studies = studyService.searchBySubject(subject);
        List<StudyResponseDto> result = studies.stream()
                .map(StudyResponseDto::new)
                .toList();
        return ResponseEntity.ok(result);
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
    public ResponseEntity<StudyRecord> createRecord(@RequestBody StudyRecordRequestDto studyRecordRequestDto) {
        StudyRecord record = studyService.createStudyRecord(studyRecordRequestDto);
        return ResponseEntity.ok(record);
    }
}
