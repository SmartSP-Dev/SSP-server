package group4.opensource_server.calendar.exception;

public class TimetableParsingException extends RuntimeException {
    public TimetableParsingException(String message) {
        super(message);
    }

    public TimetableParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
