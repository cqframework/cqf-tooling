package org.opencds.cqf.tooling.operation;

import java.net.URISyntaxException;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.cql.VmrToFhir;

public class VmrToFhirOperationTest {
    private String vmrFilePath = "vMROutput.xml";

    // @Test
    public void vmrOperationTest() throws URISyntaxException {
        String inputFilePath =
                VmrToFhirOperationTest.class.getResource(vmrFilePath).toURI().getPath();
        String operation = "VmrToFhir";
        String outputPath = "target/test-output/vmr-to-fhir";
        String encoding = "xml";
        String[] args = {"-" + operation, "-ifp=" + inputFilePath, "-op=" + outputPath, "-e=" + encoding};
        Operation vmr = new ExecutableOperationAdapter(new VmrToFhir());
        vmr.execute(args);
    }
}
