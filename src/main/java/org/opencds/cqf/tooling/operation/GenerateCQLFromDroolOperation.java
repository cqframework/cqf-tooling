package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.GenerateCQLFromDroolParameters;
import org.opencds.cqf.tooling.processor.GenerateCQLFromDroolProcessor;
import org.opencds.cqf.tooling.processor.argument.GenerateCQLFromDroolArgumentProcessor;

public class GenerateCQLFromDroolOperation extends Operation {

    public GenerateCQLFromDroolOperation() {
    }

    @Override
    public void execute(String[] args) {

        GenerateCQLFromDroolParameters params = null;
        try {
            params = new GenerateCQLFromDroolArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        GenerateCQLFromDroolProcessor.generate(params);
    }
}