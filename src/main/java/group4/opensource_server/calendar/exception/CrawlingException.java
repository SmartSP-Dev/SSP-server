package group4.opensource_server.calendar.exception;

public class CrawlingException extends RuntimeException {
    public CrawlingException(String message) {
        super(message);
    }

    public CrawlingException(String message, Throwable cause) {
        super(message, cause);
    }
}
