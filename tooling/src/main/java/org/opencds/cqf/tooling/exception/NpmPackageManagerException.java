package org.opencds.cqf.tooling.exception;

/**
 * This exception is thrown whenever there is an issue with the NpmPackageManager.
 */
public class NpmPackageManagerException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public NpmPackageManagerException() {
        super();
    }

    public NpmPackageManagerException(String message) {
        super(message);
    }

    public NpmPackageManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NpmPackageManagerException(Throwable cause) {
        super(cause);
    }
}
