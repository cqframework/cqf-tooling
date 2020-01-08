package org.opencds.cqf.igtools;

import org.opencds.cqf.Operation;
import org.opencds.cqf.igtools.IGProcessor;

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
        IGProcessor.refreshIG(params);
    }   
}

