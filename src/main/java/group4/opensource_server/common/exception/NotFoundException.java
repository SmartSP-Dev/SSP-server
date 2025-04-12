package group4.opensource_server.common.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(NOT_FOUND)
public abstract class NotFoundException extends ApiException {
    public NotFoundException(final String message) {
        super(message, NOT_FOUND);
    }
}