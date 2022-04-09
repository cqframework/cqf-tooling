package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.XmlParser;
import com.google.gson.JsonObject;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.File;
import java.util.Locale;


public class RollTestsDataDatesOperation extends Operation {
    private FhirContext fhirContext;
    public static final String separator = System.getProperty("file.separator");

    @Override
    public void execute(String[] args) {
        String inputPath = "";
        String fhirVersion = "";
        setOutputPath("src" + separator + "main" + separator + "resources" + separator + "org.opencds.cqf" + separator + "tooling");
        for (String arg : args) {
            if (arg.equals("-RollTestsDataDates")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "inputpath":
                case "ip":
                    inputPath = value;
                    break;
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break; // -outputpath (-op)
                case "version":
                case "v":
                    fhirVersion = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
        if (inputPath.length() < 1) {
            throw new IllegalArgumentException("The directory to files to change dates is required as -ip=");
        }

        if (fhirVersion.length() < 1){
            throw new IllegalArgumentException("The FHIR version(-v) must be specified and must be the version number such as 3.0.0 or 4.0.1");
        }
        fhirContext = IGProcessor.getIgFhirContext(fhirVersion);

        rollAllDates(inputPath, fhirVersion);
    }

    private void rollAllDates(String inputPath, String fhirVersion) {
        File file = inputPath != null ? new File(inputPath) : null;
        if (file == null || !file.exists() || !file.isDirectory()) {
            throw new IllegalArgumentException("inputPath " + inputPath + " is not a directory");
        }
        processFiles(file, inputPath);
    }

    /***
     * Go through the directory;
     *  if it is a folder go through it (probably recursive)
     *  if it  is a file, fix it, keep going
     * @param file
     */

    private void processFiles(File file, String currentPath) {
        if (file.isDirectory()) {
            for (File nextFile : file.listFiles()) {
                if (nextFile.isDirectory()) {
                    processFiles(nextFile, currentPath);
                }
                if ((!nextFile.getName().toLowerCase(Locale.ROOT).contains("xml")) &&
                        (!nextFile.getName().toLowerCase(Locale.ROOT).contains("json"))){
                    continue;
                }
                rollDatesInFile(nextFile);
            }
//            if (IOUtils.isXMLOrJson(libraryPath, libraryFile.getName())) {
//                loadLibrary(fileMap, libraries, libraryFile);
//            }
        }
    }

    private void rollDatesInFile(File file){
        IOUtils.Encoding fileEncoding = IOUtils.getEncoding(file.getName());

        String fileContents = IOUtils.getFileContent(file);
        if (fileContents.contains("hookInstance")){
            if (fileEncoding.equals(IOUtils.Encoding.XML)) {
                XMLParser xmlParser = new XmlParser();
                JsonObject hook =
            }else if(fileEncoding.equals(IOUtils.Encoding.JSON)){

            }
        }
        else {
            IParser fileParser = getParser(fileEncoding);
            IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), fhirContext);
            if (null != resource) {
                String resourceType = resource.fhirType();
                System.out.println(resourceType);
            }
        }
    }

    private IParser getParser(IOUtils.Encoding encoding)
    {
        switch (encoding) {
            case XML:
                return fhirContext.newXmlParser();
            case JSON:
                return fhirContext.newJsonParser();
            default:
                throw new RuntimeException("Unknown encoding type: " + encoding.toString());
        }
    }
}