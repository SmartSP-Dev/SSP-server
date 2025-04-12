package group4.opensource_server.study.exception;

import group4.opensource_server.common.exception.NotFoundException;

public class StudyNotFoundException extends NotFoundException {
    public StudyNotFoundException(String message) {super(message);}
}
