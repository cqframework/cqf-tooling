package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.VmrToFhirParameters;
import org.opencds.cqf.tooling.processor.VmrToFhirProcessor;
import org.opencds.cqf.tooling.processor.argument.VmrToFhirArgumentProcessor;
/**
 * @author Joshua Reynolds
 */
public class VmrToFhirOperation extends Operation {

    public VmrToFhirOperation() {
    }

    @Override
    public void execute(String[] args) {

        VmrToFhirParameters params = null;
        try {
            params = new VmrToFhirArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        VmrToFhirProcessor.transform(params);
    }
}