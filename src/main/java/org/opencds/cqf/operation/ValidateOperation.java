package org.opencds.cqf.operation;

import org.opencds.cqf.Operation;
import org.opencds.cqf.parameter.ValidateParameters;
import org.opencds.cqf.processor.ValidateProcessor;
import org.opencds.cqf.processor.argument.ValidateArgumentProcessor;

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