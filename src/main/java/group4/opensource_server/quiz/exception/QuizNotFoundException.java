package group4.opensource_server.quiz.exception;

public class QuizNotFoundException extends RuntimeException {
    public QuizNotFoundException(Long quizId) {
        super("해당 퀴즈를 찾을 수 없습니다: " + quizId);
    }
}
