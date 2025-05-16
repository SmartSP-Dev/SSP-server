package group4.opensource_server.calendar.domain;

import group4.opensource_server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "everytime_timetables")
public class EveryTimeTimetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 해당 시간표를 등록한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 에브리타임에서 크롤링한 시간표 JSON을 그대로 문자열로 저장
    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String timetableJson;
}