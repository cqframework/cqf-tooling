package org.opencds.cqf.tooling.cql.exception;

import org.cqframework.cql.cql2elm.CqlCompilerException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom exception to pass the list of errors returned by the translator to calling methods.
 */
public class CqlTranslatorException extends Exception implements Serializable {
    private static final long serialVersionUID = 20600L;

    /**
     * Using Set to avoid duplicate entries.
     */
    private final transient List<CqlCompilerException>  errors = new ArrayList<>();

    public CqlTranslatorException(Exception e) {
        super("CQL Translation Error(s): " + e.getMessage());
    }

    public CqlTranslatorException(List<CqlCompilerException> errors) {
        super("CQL Translation Error(s)");
        this.errors.addAll(errors);
    }

    public CqlTranslatorException(List<String> errorsInput, CqlCompilerException.ErrorSeverity errorSeverity) {
        super("CQL Translation Error(s)");
        for (String error : errorsInput){
            errors.add(new CqlCompilerException(error, errorSeverity));
        }
    }

    public CqlTranslatorException(String message) {
        super("CQL Translation Error(s): " + message);
    }

    public List<CqlCompilerException> getErrors() {
        if (errors.isEmpty()) {
            errors.add(new CqlCompilerException(this.getMessage()));
        }
        return errors;
    }
}
