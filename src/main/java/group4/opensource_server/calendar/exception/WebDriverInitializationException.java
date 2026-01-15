package group4.opensource_server.calendar.exception;

public class WebDriverInitializationException extends RuntimeException {
    public WebDriverInitializationException(String message) {
        super(message);
    }

    public WebDriverInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
