package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.processor.argument.RefreshIGArgumentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RefreshIGOperation extends Operation {
    private final static Logger logger = LoggerFactory.getLogger(RefreshIGOperation.class);
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

        if (params.verboseMessaging == null || !params.verboseMessaging) {
            logger.info("Re-run with -x to for expanded reporting of errors, warnings, and informational messages.");
        }

        try {
            new IGProcessor().publishIG(params);
        } catch (IOException e) {
            logger.error("Error refreshing IG: ", e);
        }
    }
}

