package group4.opensource_server.quiz.domain;

public class QuizConstants {
    // 텍스트 길이 임계값
    public static final int SMALL_TEXT_THRESHOLD = 10_000;
    public static final int MEDIUM_TEXT_THRESHOLD = 20_000;
    public static final int LARGE_TEXT_MAX = 30_000;

    // 문제 개수
    public static final int SMALL_TEXT_QUESTIONS = 10;
    public static final int MEDIUM_TEXT_QUESTIONS_PER_PART = 5;
    public static final int LARGE_TEXT_QUESTIONS_PART1 = 3;
    public static final int LARGE_TEXT_QUESTIONS_PART2 = 3;
    public static final int LARGE_TEXT_QUESTIONS_PART3 = 4;

    // 복습 간격 (일)
    public static final int REVIEW_INTERVAL_DAYS = 1;

    private QuizConstants() {
        throw new IllegalStateException("Utility class");
    }
}
