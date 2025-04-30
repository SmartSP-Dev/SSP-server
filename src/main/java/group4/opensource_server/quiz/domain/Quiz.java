package group4.opensource_server.quiz.domain;

import group4.opensource_server.user.domain.User;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


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

    @Lob
    private String summary;

    // QuizQuestion 리스트
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizQuestion> questions;

    @Builder
    public Quiz(User user, String title, String keywords, QuestionType questionType, String summary) {
        this.user = user;
        this.title = title;
        this.keywords = keywords;
        this.questionType = questionType;
        this.summary = summary;
    }
}