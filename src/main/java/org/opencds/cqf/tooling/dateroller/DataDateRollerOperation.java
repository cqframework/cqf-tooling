package org.opencds.cqf.tooling.dateroller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;


public class DataDateRollerOperation extends Operation {
    private FhirContext fhirContext;
    public static final String separator = System.getProperty("file.separator");
    public String fhirVersion;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * open directory;
     * Get object
     * if directory goto open directory
     * else
     * if file type fhir resource
     * if bundle use bundleutils.getR4ResourcesFromBundle to get resources
     * for each resource look for date items and adjust
     * else
     * look for date items and adjust
     * else if cdsHook
     * find all resources, including the draftOrder one and adjust
     */
    @Override
    public void execute(String[] args) {
        String inputPath = "";
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
        processFiles(file, inputPath);
    }

    private void processFiles(File file, String currentPath) {
        if (file.isDirectory()) {
            for (File nextFile : file.listFiles()) {
                if (nextFile.isDirectory()) {
                    processFiles(nextFile, currentPath);
                }
                if ((!nextFile.getName().toLowerCase(Locale.ROOT).contains("xml")) &&
                        (!nextFile.getName().toLowerCase(Locale.ROOT).contains("json"))) {
                    continue;
                }
                rollDatesInFile(nextFile);
            }
//            BundleUtils.extractR4Resources();
//            if (IOUtils.isXMLOrJson(libraryPath, libraryFile.getName())) {
//                loadLibrary(fileMap, libraries, libraryFile);
//            }
        }
        if (file.isFile()) {
            if ((!file.getName().toLowerCase(Locale.ROOT).contains("xml")) &&
                    (!file.getName().toLowerCase(Locale.ROOT).contains("json"))) {
                return;
            }
            logger.info("Rolling dates for file: " + file.getAbsolutePath());
            rollDatesInFile(file);
        }
    }

    private void rollDatesInFile(File file) {
        IOUtils.Encoding fileEncoding = IOUtils.getEncoding(file.getName());

        String fileContents = IOUtils.getFileContent(file);
        if (fileContents.contains("hookInstance")) {
            if (fileEncoding.equals(IOUtils.Encoding.XML)) {
                return;
/**
 *          turn xml into json then call rollJSONHookDates
 */
//                XMLParser xmlParser = new XmlParser();
//                JsonObject hook =
            } else if (fileEncoding.equals(IOUtils.Encoding.JSON)) {
                HookDataDateRoller.rollJSONHookDates(JsonParser.parseString(fileContents).getAsJsonObject());//this should be the whole hook
            }
        } else if (fileContents.contains("resourceType")) {
            IParser fileParser = DataDateRollerUtils.getParser(fileEncoding, fhirContext);
            IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), fhirContext);
            if (null != resource) {
                Field[] fields = resource.getClass().getFields();
                String resourceType = resource.fhirType();
                if (resourceType.equals("Bundle")) {
                    ResourceDataDateRoller.rollBundleDates(fhirContext, resource);
                } else {
                    ResourceDataDateRoller.rollDatesInR4Resource(resource);
                }
            }
        }
    }
// resource.getClass().getName() "org.hl7.fhir.r4.model.MedicationRequest"
    //resource.getClass().getDeclaredFields()

    private LocalDate getLastUpdated(JsonArray dataDateRollerExtensions) {
        AtomicReference<LocalDate> lastUpdated = null;
        dataDateRollerExtensions.forEach(extensionMember -> {
            String url = extensionMember.getAsJsonObject().get("url").getAsString();
            if (null != url && url.equalsIgnoreCase("dateLastUpdated")) {
                String dateString = extensionMember.getAsJsonObject().get("valueDateTime").getAsString();
                lastUpdated.set(DataDateRollerUtils.stringToDate(dateString));
            }
        });
        return lastUpdated.get();
    }

    /**
     * if resource has an extension with "url": "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller"
     * then get sub-extension from that with "url": "dateLastUpdated", and grab it's "valueDateTime": "2022-04-22"
     * then get sub-extension "url": "frequency", and grab it's
     * "valueDuration": {
     * "value": 30.0,
     * "unit": "days",
     * "system": "http://unitsofmeasure.org",
     * "code": "d"
     * }
     * //grab current dateTime objects and update them to current dateTime + valueDuration value (in whatever unit is in extension)
     * //    Then set dateLastUpdated to current dateTime
     * <p>
     * newDate = Current date - lastRunDate + frequency + objects dateValue
     */
}