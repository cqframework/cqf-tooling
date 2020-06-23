package org.opencds.cqf.terminology;

import java.io.File;

import org.opencds.cqf.Operation;

public class VSACBatchValueSetGenerator extends Operation {

    private String pathToSpreadsheetDirectory; // -pathtospreadsheetdir (-ptsd)
    private String valueSetSource = "vsac"; //vsac or cms

    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals("-VsacXlsxToValueSetBatch")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break; // -outputpath (-op)
                case "pathtospreadsheetdir":
                case "ptsd":
                    pathToSpreadsheetDirectory = value;
                    break;
                case "valuesetsource":
                case "vssrc":
                    valueSetSource = value;
                    break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
        if (pathToSpreadsheetDirectory == null) {
            throw new IllegalArgumentException("The path to the spreadsheet directory is required");
        }

        File valueSetDirectory = new File(pathToSpreadsheetDirectory);
        if (!valueSetDirectory.isDirectory()) {
            throw new RuntimeException("The specified path to valueset files is not a directory");
        }

        File[] valueSetFiles = valueSetDirectory.listFiles();
        if (valueSetFiles == null) {
            throw new RuntimeException("The specified path to valueset files is empty");
        }
        if (valueSetSource.equals("cms")) {
            CMSFlatMultiValueSetGenerator generator;
            for (File valueSet : valueSetFiles) {
                if (!valueSet.getPath().endsWith(".xlsx")) continue;
                String[] argsForSpreadsheet = { "-pts=" + valueSet.getPath(), "-op=" + getOutputPath() }; //-pts=${valueSetExcelSpreadSheet} -op=${resourcesValueSetDirectory}
                generator =  new CMSFlatMultiValueSetGenerator();
                generator.execute(argsForSpreadsheet);
            }
        }
        else if (valueSetSource.equals("vsac")) {
            VSACValueSetGenerator generator;
            for (File valueSet : valueSetFiles) {
                if (!valueSet.getPath().endsWith(".xlsx")) continue;
                String[] argsForSpreadsheet = { "-VsacXlsxToValueSet", "-pts=" + valueSet.getAbsolutePath(), "-op=" + getOutputPath() };
                generator =  new VSACValueSetGenerator();
                generator.execute(argsForSpreadsheet);
            }
        }
    }
}
