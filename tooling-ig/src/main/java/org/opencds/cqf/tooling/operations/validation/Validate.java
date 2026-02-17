package org.opencds.cqf.tooling.operations.validation;

import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.parameter.ValidateParameters;
import org.opencds.cqf.tooling.processor.ValidateProcessor;

@Operation(name = "Validate")
public class Validate implements ExecutableOperation {

    @Override
    public void execute() {
        ValidateParameters params = new ValidateParameters();
        ValidateProcessor.validate(params);
    }
}
