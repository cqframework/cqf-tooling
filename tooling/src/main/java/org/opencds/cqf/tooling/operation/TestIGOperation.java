package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.TestIGParameters;
import org.opencds.cqf.tooling.processor.IGTestProcessor;
import org.opencds.cqf.tooling.processor.argument.TestIGArgumentsProcessor;

public class TestIGOperation extends Operation {
    public TestIGOperation() {
    }

    @Override
    public void execute(String[] args) {
        TestIGParameters params = null;
        try {
            params = new TestIGArgumentsProcessor().parseAndConvert(args);
            new IGTestProcessor().testIg(params);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        TestIGOperation op = new TestIGOperation();

        op.execute(new String[] {
            "-TestIG",
            "-ini",
            "/Users/Adam/Src/DBCG/connectathon/fhir401/ig.ini",
            "-root-dir",
            "/Users/Adam/Src/DBCG/connectathon/fhir401",
//            "-igcb",
//            "http://fhir.org/guides/dbcg/connectathon/ImplementationGuide/fhir.dbcg.connectathon-r4",
            "-fv",
            "4.0.1",
            "-tcp",
            "/Users/Adam/Src/DBCG/connectathon/fhir401/input/tests",
            "-fs",
            //"http://localhost:8080/cqf-ruler-r4/fhir"
            "http://192.168.2.194:8082/cqf-ruler-r4/fhir"
        });
    }
}
