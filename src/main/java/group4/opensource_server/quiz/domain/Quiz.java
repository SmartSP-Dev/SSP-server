package group4.opensource_server.quiz.domain;

import group4.opensource_server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 해당 퀴즈를 만든 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String title;

    @Lob
    private String keywords;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;

    // 복습 상태: 1(처음), 2(복습 필요), 3(복습 완료)
    @Column(nullable = false)
    @Builder.Default
    private int status = QuizStatus.CREATED.getCode();

    // 복습 횟수 (0 → 1 → 2 → 종료)
    @Column(nullable = false)
    @Builder.Default
    private int reviewCount = 0;

    // 마지막 복습 완료 날짜
    private LocalDate lastReviewedAt;

    // 퀴즈 생성일
    @Column(nullable = false, updatable = false)
    private LocalDate createdAt;

    // QuizQuestion 리스트
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizQuestion> questions;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }

    // 비즈니스 메서드: 상태 변경
    public void updateStatus(QuizStatus newStatus) {
        this.status = newStatus.getCode();
    }

    // 비즈니스 메서드: 복습 완료 처리
    public void completeReview() {
        this.status = QuizStatus.REVIEWED.getCode();
        this.lastReviewedAt = LocalDate.now();
        this.reviewCount++;
    }

    // 비즈니스 메서드: 복습 필요 상태로 변경
    public void markAsNeedReview() {
        this.status = QuizStatus.NEED_REVIEW.getCode();
    }

    // 상태 확인 메서드
    public boolean isReviewed() {
        return this.status == QuizStatus.REVIEWED.getCode();
    }

    public QuizStatus getStatusEnum() {
        return QuizStatus.fromCode(this.status);
    }
}