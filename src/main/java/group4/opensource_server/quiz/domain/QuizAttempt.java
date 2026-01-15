package group4.opensource_server.quiz.domain;

import group4.opensource_server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 퀴즈를 풀었는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // 누가 풀었는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 점수 (정답 수 기준)
    private int score;

    private int reviewCount;

    private LocalDateTime attemptTime;

    // 결과 리스트 (1:N)
    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionResult> questionResults;

    // 비즈니스 메서드: 점수 업데이트
    public void updateScore(int score) {
        this.score = score;
    }
}