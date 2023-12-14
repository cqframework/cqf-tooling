package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.processor.argument.RefreshIGArgumentProcessor;

public class RefreshIGOperation extends Operation {

    public RefreshIGOperation() {
    }

    @Override
    public void execute(String[] args) {

        if (args == null) {
            throw new IllegalArgumentException();
        }

        RefreshIGParameters params = null;
        try {
            params = new RefreshIGArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (params.includeErrors == null || !params.includeErrors) {
            System.out.println("\r\nRe-run with -x to for expanded reporting of errors, warnings, and informational messages.\r\n");
        }

        new IGProcessor().publishIG(params);
    }
}

