package org.opencds.cqf.operation;

import org.opencds.cqf.Operation;
import org.opencds.cqf.parameter.RefreshIGParameters;
import org.opencds.cqf.processor.IGProcessor;
import org.opencds.cqf.processor.argument.RefreshIGArgumentProcessor;

public class RefreshIGOperation extends Operation {

    public RefreshIGOperation() {    
    } 

    @Override
    public void execute(String[] args) {
        RefreshIGParameters params = null;
        try {
            params = new RefreshIGArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        IGProcessor.publishIG(params);
    }   
}

