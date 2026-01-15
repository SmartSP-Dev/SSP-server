package group4.opensource_server.quiz.exception;

public class QuizGenerationException extends RuntimeException {
    public QuizGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuizGenerationException(String message) {
        super(message);
    }
}
