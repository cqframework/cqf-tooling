package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.Bundle;
import org.hl7.fhir.BundleEntry;
import org.hl7.fhir.ResourceContainer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class RollTestsDataDatesOperation extends Operation {
    private FhirContext fhirContext;
    public static final String separator = System.getProperty("file.separator");

    /**
     *
     * open directory;
     * Get object
     *      if directory goto open directory
     *      else
     *          if file type fhir resource
     *              if bundle use bundleutils.getR4ResourcesFromBundle to get resources
     *                  for each resource look for date items and adjust
     *              else
     *                  look for date items and adjust
     *          else if cdsHook
     *              find all resources, including the draftOrder one and adjust

     */
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
//            BundleUtils.extractR4Resources();
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
//                XMLParser xmlParser = new XmlParser();
//                JsonObject hook =
            }else if(fileEncoding.equals(IOUtils.Encoding.JSON)){

            }
        }
        else {
            IParser fileParser = getParser(fileEncoding);
            IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), fhirContext);
            if (null != resource) {
                String resourceType = resource.fhirType();
                if (resourceType.equals("Bundle")){
                    rollBundleDates(resource);
                }
                else{

                }
            }
        }
    }

    private void rollBundleDates(IBaseResource iBaseResource){
        switch (fhirContext.getVersion().getVersion().name()) {
            case "R4":
                ArrayList<org.hl7.fhir.r4.model.Resource> r4ResourceArrayList = BundleUtils.getR4ResourcesFromBundle((org.hl7.fhir.r4.model.Bundle)iBaseResource);
                r4ResourceArrayList.forEach(resource -> {
                    rollDatesInR4Resource(resource);
                });
                break;
            case "Stu3":
                ArrayList<org.hl7.fhir.dstu3.model.Resource> stu3resourceArrayList = BundleUtils.getStu3ResourcesFromBundle((org.hl7.fhir.dstu3.model.Bundle)iBaseResource);
                stu3resourceArrayList.forEach(resource -> {
                    rollDatesInStu3Resource(resource);
                });                break;
        }
    }

    private void rollDatesInR4Resource(org.hl7.fhir.r4.model.Resource resource){
        switch (resource.getResourceType().name()){
            case "Patient":
            case "MedicationRequest":
            case "Encounter":
            case "Observation":
                break;
        }
    }

    private void rollDatesInStu3Resource(org.hl7.fhir.dstu3.model.Resource resource){}

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