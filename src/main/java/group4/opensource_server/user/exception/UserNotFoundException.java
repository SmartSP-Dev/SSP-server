package group4.opensource_server.user.exception;

import group4.opensource_server.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }
}