package org.opencds.cqf.tooling.test.exception;
/**
 * @author Adam Stevenson
 */
@SuppressWarnings("serial")
public class InvalidTestCaseException extends Exception {
    public InvalidTestCaseException(String errorMessage) {
        super(errorMessage);
    }
}