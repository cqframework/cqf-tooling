package org.opencds.cqf.operation;

import org.opencds.cqf.Operation;
import org.opencds.cqf.parameter.PostBundlesInDirParameters;
import org.opencds.cqf.processor.PostBundlesInDirProcessor;
import org.opencds.cqf.processor.argument.PostBundlesInDirArgumentProcessor;

public class PostBundlesInDirOperation extends Operation {

    public PostBundlesInDirOperation() {    
    } 

    @Override
    public void execute(String[] args) {
        PostBundlesInDirParameters params = null;
        try {
            params = new PostBundlesInDirArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        PostBundlesInDirProcessor.PostBundlesInDir(params);
    }   
}