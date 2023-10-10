package org.opencds.cqf.tooling.exception;

public class InvalidIdException extends RuntimeException {

    static final long serialVersionUid = 1l;

    public InvalidIdException() {super();}

    public InvalidIdException(String message) {super(message);}

    public InvalidIdException(String message, Throwable cause) {super(message, cause);}

    public InvalidIdException(Throwable cause) {super(cause);}
}
