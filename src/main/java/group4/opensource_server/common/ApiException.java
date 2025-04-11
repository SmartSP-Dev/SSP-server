package group4.opensource_server.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class ApiException extends ResponseStatusException {
    public ApiException(final String message, final HttpStatus status) {
        super(status, message);
    }
}