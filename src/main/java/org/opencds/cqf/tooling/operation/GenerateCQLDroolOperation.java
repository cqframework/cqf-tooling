package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.GenerateCQLDroolParameters;
import org.opencds.cqf.tooling.processor.GenerateCQLDroolProcessor;
import org.opencds.cqf.tooling.processor.argument.GenerateCQLDroolArgumentProcessor;

public class GenerateCQLDroolOperation extends Operation {

    public GenerateCQLDroolOperation() {
    }

    @Override
    public void execute(String[] args) {

        GenerateCQLDroolParameters params = null;
        try {
            params = new GenerateCQLDroolArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        GenerateCQLDroolProcessor.generate(params);
    }
}