package org.opencds.cqf.tooling.cql.exception;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Custom exception to pass the list of errors returned by the translator to calling methods.
 */
public class CQLTranslatorException extends Exception implements Serializable {
    private static final long serialVersionUID = 20600L;

    /**
     * Using Set to avoid duplicate entries.
     */
    private final Set<String> errors = new HashSet<>();

    public CQLTranslatorException(Exception e) {
        super("CQL Translation Error(s): " + e.getMessage());
    }

    public CQLTranslatorException(List<String> errors) {
        super("CQL Translation Error(s)");
        this.errors.addAll(errors);
    }

    public CQLTranslatorException(String message) {
        super("CQL Translation Error(s): " + message);
    }

    public Set<String> getErrors() {
        if (errors.isEmpty()) {
            errors.add(this.getMessage());
        }
        return errors;
    }
}
