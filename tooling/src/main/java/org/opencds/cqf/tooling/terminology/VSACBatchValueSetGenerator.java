package org.opencds.cqf.tooling.terminology;

import java.io.File;

import org.opencds.cqf.tooling.Operation;

public class VSACBatchValueSetGenerator extends Operation {

    private String pathToSpreadsheetDirectory; // -pathtospreadsheetdir (-ptsd)
    private String valueSetSource = "vsac"; //vsac or cms
    private String baseUrl; // -baseurl (-burl)
    private boolean setName; // -setname (-name)

    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals("-VsacXlsxToValueSetBatch")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1].replace("\"", ""); // Strip quotes

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
                case "baseurl":
                case "burl":
                    baseUrl = value;
                    break;
                case "setname":
                case "name":
                    setName = value.toLowerCase().equals("true") ? true : false;
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
            if (baseUrl != null) {
                throw new RuntimeException("baseUrl flag is not valid with valueSetSource flag set to 'cms'");
            }
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
            if (baseUrl == null) {
                baseUrl = VSACValueSetGenerator.VSAC_BASE_URL;
            }
            for (File valueSet : valueSetFiles) {
                if (!valueSet.getPath().endsWith(".xlsx")) continue;
                String[] argsForSpreadsheet = { "-VsacXlsxToValueSet", "-pts=" + valueSet.getAbsolutePath(), "-op=" + getOutputPath(), "-burl=" + baseUrl, "-name=" + (setName ? "true" : "false") };
                generator =  new VSACValueSetGenerator();
                generator.execute(argsForSpreadsheet);
            }
        }
        else if (valueSetSource.equals("hedis")) {
            HEDISValueSetGenerator generator;
            for (File valueSet : valueSetFiles) {
                if (!valueSet.getPath().endsWith(".xlsx")) continue;
                String[] argsForSpreadsheet = { "-HedisXlsxToValueSet", "-pts=" + valueSet.getAbsolutePath(), "-op=" + getOutputPath() };
                generator =  new HEDISValueSetGenerator();
                generator.execute(argsForSpreadsheet);
            }
        }
    }
}
