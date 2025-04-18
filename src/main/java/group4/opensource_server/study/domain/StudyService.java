package group4.opensource_server.study.domain;

import group4.opensource_server.study.dto.*;
import group4.opensource_server.study.exception.StudyNotFoundException;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import group4.opensource_server.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final UserRepository userRepository;

    @Transactional
    public Study createStudy(String email, StudyRequestDto studyRequestDto) {
        User user=userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("없는 유저입니다."));
        return studyRepository.save(Study.builder()
                .subject(studyRequestDto.getSubject())
                .user(user)
                .build());
    }

    public Study getStudy(Integer id) {
        return studyRepository.findById(id)
                .orElseThrow(() -> new StudyNotFoundException("존재하지 않는 스터디입니다."));
    }

    public List<Study> getAllStudies() {
        return studyRepository.findAll();
    }

    public List<StudyDataResponseDto> getStudyData(Integer studyId, String range) {
        LocalDate fromDate = switch (range.toLowerCase()) {
            case "week" -> LocalDate.now().minusDays(6);
            case "month" -> LocalDate.now().minusDays(29);
            case "all" -> LocalDate.of(2000, 1, 1);
            default ->  throw new IllegalArgumentException("invalid range: " + range);
        };

        return studyRecordRepository.getStudyData(studyId, fromDate).stream()
                .map(s -> new StudyDataResponseDto(s.getDate(), s.getTotalTime()))
                .toList();
    }

    public List<Study> searchBySubject(String subject) {
        return studyRepository.findBySubjectContaining(subject);
    }
    @Transactional
    public Study updateStudy(Integer id, StudyUpdateRequestDto studyUpdateRequestDto) {
        Study study = getStudy(id);
        study.setSubject(studyUpdateRequestDto.getSubject());
        return study;
    }

    @Transactional
    public void deleteStudy(Integer id) {
        studyRepository.deleteById(id);
    }

    @Transactional
    public StudyDataResponseDto createStudyRecord(StudyRecordRequestDto studyRecordRequestDto) {
        Study study = studyRepository.findById(studyRecordRequestDto.getStudyId())
                .orElseThrow(() -> new StudyNotFoundException("존재하지 않는 스터디입니다."));

        StudyRecord studyRecord=studyRecordRepository.save(StudyRecord.builder()
                .study(study)
                .date(studyRecordRequestDto.getDate())
                .time(studyRecordRequestDto.getTime())
                .build());
        return StudyDataResponseDto.builder()
                .date(studyRecord.getDate())
                .time(studyRecord.getTime().longValue())
                .build();
    }
}
