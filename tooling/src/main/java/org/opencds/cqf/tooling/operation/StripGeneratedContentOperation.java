package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operations.stripcontent.StripContentParams;
import org.opencds.cqf.tooling.operations.stripcontent.StripContentExecutor;

public class StripGeneratedContentOperation extends Operation {
    @Override
    public void execute(String[] args) {
        var params = new StripContentParams();
        for (String arg : args) {
            if (arg.equals("-StripGeneratedContent")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1].replace("\"", ""); // Strip quotes

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath":
                case "op":
                    params.outputDirectory(value);
                    break;
                case "pathtores":
                case "ptr":
                    params.inputDirectory(value);
                    break;
                case "version": case "v":
                    params.fhirVersion(value);
                    break;

                case "cql":
                    params.cqlExportDirectory(value);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
       
        new StripContentExecutor(params).execute();
    }
}
