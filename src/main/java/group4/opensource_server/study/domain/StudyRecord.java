package group4.opensource_server.study.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "study_records")
@Getter
@Setter
@NoArgsConstructor
public class StudyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time")
    private Integer time;

    @Builder
    public StudyRecord(Study study, LocalDate date, Integer time) {
        this.study = study;
        this.date = date;
        this.time = time;
    }
}
