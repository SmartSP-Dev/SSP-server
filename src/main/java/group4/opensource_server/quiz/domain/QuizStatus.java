package group4.opensource_server.quiz.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuizStatus {
    CREATED(1, "생성됨"),
    NEED_REVIEW(2, "복습 필요"),
    REVIEWED(3, "복습 완료");

    private final int code;
    private final String description;

    public static QuizStatus fromCode(int code) {
        for (QuizStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid quiz status code: " + code);
    }
}
