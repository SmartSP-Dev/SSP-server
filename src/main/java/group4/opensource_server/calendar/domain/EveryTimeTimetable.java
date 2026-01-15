package group4.opensource_server.calendar.domain;

import group4.opensource_server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "everytime_timetables")
public class EveryTimeTimetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String timetableJson;

    /**
     * 시간표 JSON을 업데이트합니다.
     *
     * @param timetableJson 새로운 시간표 JSON 문자열
     */
    public void updateTimetableJson(String timetableJson) {
        this.timetableJson = timetableJson;
    }
}