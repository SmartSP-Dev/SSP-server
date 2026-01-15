package group4.opensource_server.quiz.exception;

public class UnauthorizedQuizAccessException extends RuntimeException {
    public UnauthorizedQuizAccessException(String message) {
        super(message);
    }
}
