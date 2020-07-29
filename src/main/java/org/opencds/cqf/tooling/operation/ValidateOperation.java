package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.ValidateParameters;
import org.opencds.cqf.tooling.processor.ValidateProcessor;
import org.opencds.cqf.tooling.processor.argument.ValidateArgumentProcessor;

public class ValidateOperation extends Operation {

    public ValidateOperation() {
    }

    @Override
    public void execute(String[] args) {

        ValidateParameters params = null;
        try {
            params = new ValidateArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        ValidateProcessor.validate(params);
    }
}