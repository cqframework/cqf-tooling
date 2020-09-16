package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.ScaffoldParameters;
import org.opencds.cqf.tooling.processor.ScaffoldProcessor;
import org.opencds.cqf.tooling.processor.argument.ScaffoldArgumentProcessor;


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

        ScaffoldProcessor scaffoldProcessor = new ScaffoldProcessor();
        scaffoldProcessor.scaffold(params);
    }

    public static void main(String[] args) {
        ScaffoldOperation op = new ScaffoldOperation();

        op.execute(new String[] {
            "-ScaffoldIG",
            "-ip",
            "/Users/Adam/Src/scaffolded-ig",
            "-iv",
            "r4",
            "-software",
            "TestTool1=1.2.4",
            "-software",
            "TestTool2=4.2.1",
            "-rn",
            "EXM_133",
            "-rn",
            "EXM_134"
        });
    }
}