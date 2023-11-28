package org.opencds.cqf.tooling.dateroller;

import ca.uhn.fhir.context.FhirContext;
import com.google.gson.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;


public class DataDateRollerOperation extends Operation {
    private FhirContext fhirContext;
    public String fhirVersion;
    private IOUtils.Encoding fileEncoding;
    private static final Logger logger = LoggerFactory.getLogger(DataDateRollerOperation.class);

    @Override
    public void execute(String[] args) {
        String inputPath = "";
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
        if (fhirVersion.length() < 1) {
            throw new IllegalArgumentException("The FHIR version(-v) must be specified and must be the version number such as 3.0.0 or 4.0.1");
        }
        fhirContext = ResourceUtils.getFhirContext(ResourceUtils.FhirVersion.parse(fhirVersion));

        rollAllDates(inputPath);
    }

    private void rollAllDates(String inputPath) {
        File file = inputPath != null ? new File(inputPath) : null;
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("inputPath " + inputPath + " does not exist");
        }
        processFiles(file);
    }

    private void processFiles(File file) {
        if (file.isDirectory()) {
            for (File nextFile : file.listFiles()) {
                if (nextFile.isDirectory()) {
                    processFiles(nextFile);
                }
                if ((!nextFile.getName().toLowerCase(Locale.ROOT).contains("xml")) &&
                        (!nextFile.getName().toLowerCase(Locale.ROOT).contains("json"))) {
                    continue;
                }
                rollDatesInFile(nextFile);
            }
        }
        if (file.isFile()) {
            if ((!file.getName().toLowerCase(Locale.ROOT).contains("xml")) &&
                    (!file.getName().toLowerCase(Locale.ROOT).contains("json"))) {
                return;
            }
            rollDatesInFile(file);
        }
    }

    private void rollDatesInFile(File file) {
        logger.info("Rolling dates for file: {}", file.getAbsolutePath());
        fileEncoding = IOUtils.getEncoding(file.getName());

        String fileContents = IOUtils.getFileContent(file);
        if(null != fileContents && fileContents.length() > 0) {
            if (fileContents.contains("hookInstance")) {
                if (fileEncoding.equals(IOUtils.Encoding.XML)) {
                    logger.error("Current CDS Hooks specification calls for JSON only. (5/2022)");
                    return;
                } else if (fileEncoding.equals(IOUtils.Encoding.JSON)) {
                    HookDataDateRoller hookDataDateRoller = new HookDataDateRoller(fhirContext, fileEncoding);
                    JsonObject hook = hookDataDateRoller.rollJSONHookDates(JsonParser.parseString(fileContents).getAsJsonObject());//this should be the whole hook
                    FileWriter fileWriter = null;
                    try {
                        fileWriter = new FileWriter(file.getAbsolutePath());
                        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
                        gson.toJson(hook, fileWriter);
                        fileWriter.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), fhirContext);
                if (null != resource) {
                    String resourceType = resource.fhirType();
                    if (resourceType.equalsIgnoreCase("Bundle")) {
                        ResourceDataDateRoller.rollBundleDates(fhirContext, resource);
                        IOUtils.writeBundle(resource, file.getAbsolutePath(), fileEncoding, fhirContext);
                    } else {
                        ResourceDataDateRoller.rollResourceDates(fhirContext, resource);
                        IOUtils.writeResource(resource, file.getAbsolutePath(), fileEncoding, fhirContext);
                    }
                }
            }
        }
        else{
            logger.error("The file {} was either empty or came back null.", file.getAbsolutePath());
        }
    }
}