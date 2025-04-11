package group4.opensource_server.user.exception;

import group4.opensource_server.common.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }
}