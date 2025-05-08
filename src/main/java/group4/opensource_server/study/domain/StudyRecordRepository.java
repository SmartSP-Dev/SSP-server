package group4.opensource_server.study.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Integer> {
    @Query("SELECT sr.date AS date, SUM(sr.time) AS totalTime " +
            "FROM StudyRecord sr " +
            "WHERE sr.study.id = :studyId AND sr.date >= :fromDate " +
            "GROUP BY sr.date " +
            "ORDER BY sr.date ASC")
    List<StudyRecordDataProjection> getStudyData(@Param("studyId") Integer studyId,
                                                  @Param("fromDate") LocalDate fromDate);

    public interface StudyRecordDataProjection {
        LocalDate getDate();
        Long getTotalTime();
    }
    List<StudyRecord> findByStudyUserIdAndDateBetween(Integer userId, LocalDate startDate, LocalDate endDate);
}