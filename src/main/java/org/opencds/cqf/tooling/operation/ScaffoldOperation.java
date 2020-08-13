package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.ScaffoldParameters;
import org.opencds.cqf.tooling.processor.ScaffoldProcessor;
import org.opencds.cqf.tooling.processor.argument.ScaffoldArgumentProcessor;

public class ScaffoldOperation extends Operation {

    public ScaffoldOperation() {
    }

    @Override
    public void execute(String[] args) {

        ScaffoldParameters params = null;
        try {
            params = new ScaffoldArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        ScaffoldProcessor.scaffold(params);
    }
}