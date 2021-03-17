package org.opencds.cqf.tooling.operation;

import org.junit.Test;

public class VmrToFhirOperationTest {
    private String vmrFilePath = "C:/Users/jreys/Documents/src/cqf-tooling/src/test/resources/org/opencds/cqf/tooling/operation/VmrToFhir";

    @Test
    public void vmrOperationTest() {
        String operation = "VmrToFhir";
        String outputPath = "C:/Users/jreys/Documents/src/cqf-tooling/src/test/resources/org/opencds/cqf/tooling/operation/VmrToFhir/vMROutput.xml";
        String encoding = "xml";
        String[] args = { "-" + operation, "-ifp=" + vmrFilePath, "-op=" + outputPath, "-e=" + encoding };
        VmrToFhirOperation vmr = new VmrToFhirOperation();
        vmr.execute(args);
    }
}
