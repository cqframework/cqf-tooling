package org.opencds.cqf.tooling.test.exception;

@SuppressWarnings("serial")
public class InvalidTestCaseException extends Exception {
    public InvalidTestCaseException(String errorMessage) {
        super(errorMessage);
    }
}