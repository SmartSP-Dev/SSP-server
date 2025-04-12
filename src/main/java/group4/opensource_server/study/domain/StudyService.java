package group4.opensource_server.study.domain;

import group4.opensource_server.study.dto.StudyRecordRequestDto;
import group4.opensource_server.study.dto.StudyRequestDto;
import group4.opensource_server.study.dto.StudyUpdateRequestDto;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import group4.opensource_server.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));
    }

    public List<Study> getAllStudies() {
        return studyRepository.findAll();
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
    public StudyRecord createStudyRecord(StudyRecordRequestDto studyRecordRequestDto) {
        Study study = studyRepository.findById(studyRecordRequestDto.getStudyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스터디입니다."));

        return studyRecordRepository.save(StudyRecord.builder()
                .study(study)
                .date(studyRecordRequestDto.getDate())
                .time(studyRecordRequestDto.getTime())
                .build());
    }
}
