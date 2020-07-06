package org.opencds.cqf.operation;

import org.opencds.cqf.Operation;
import org.opencds.cqf.parameter.ScaffoldParameters;
import org.opencds.cqf.processor.ScaffoldProcessor;
import org.opencds.cqf.processor.argument.ScaffoldArgumentProcessor;

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