package org.opencds.cqf.quick;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

public class QuickAtlas {

    // Container for the QiCore-defined profiles (StructureDefinition.type -> StructureDefinition)
    private Map<String, StructureDefinition> qicoreProfiles = new TreeMap<>();
    public Map<String, StructureDefinition> getQicoreProfiles() {
        return qicoreProfiles;
    }

    // Container for the QiCore-defined extensions (StructureDefinition.url -> StructureDefinition)
    private Map<String, StructureDefinition> qicoreExtensions = new TreeMap<>();
    public Map<String, StructureDefinition> getQicoreExtensions() {
        return qicoreExtensions;
    }

    // Container for the QiCore-defined extensions (StructureDefinition.url -> StructureDefinition.type)
    private Map<String, String> qicoreUrlToType = new HashMap<>();
    public Map<String, String> getQicoreUrlToType() {
        return qicoreUrlToType;
    }

    // Container for the FHIR types (StructureDefinition.type -> StructureDefinition)
    private Map<String, StructureDefinition> fhirTypes = new TreeMap<>();
    public Map<String, StructureDefinition> getFhirTypes() {
        return fhirTypes;
    }

    // Container for the FHIR profiles (StructureDefinition.type -> StructureDefinition)
    private Map<String, StructureDefinition> fhirProfiles = new HashMap<>();
    public Map<String, StructureDefinition> getFhirProfiles() {
        return fhirProfiles;
    }

    // Container for the FHIR extensions (StructureDefinition.id -> StructureDefinition)
    private Map<String, StructureDefinition> fhirExtensions = new HashMap<>();
    public Map<String, StructureDefinition> getFhirExtensions() {
        return fhirExtensions;
    }

    // Container for links (StructureDefinition.type -> html file name)
    private Map<String, String> linkMap = new HashMap<>();
    public Map<String, String> getLinkMap() {
        return linkMap;
    }

    // Container for profile links (StructureDefinition.type -> html file name)
    private Map<String, String> profileMap = new TreeMap<>();
    public Map<String, String> getProfileMap() {
        return profileMap;
    }

    // Container for primitive type links (type -> html file name)
    private Map<String, String> primitiveMap = new TreeMap<>();
    public Map<String, String> getPrimitiveMap() {
        return primitiveMap;
    }

    // Container for complex type links (type -> html file name)
    private Map<String, String> complexMap = new TreeMap<>();
    public Map<String, String> getComplexMap() {
        return complexMap;
    }

    private final String cqlBooleanUrl = "http://cql.hl7.org/02-authorsguide.html#boolean";
    public String getCqlBooleanUrl() {
        return cqlBooleanUrl;
    }

    private final String cqlCodeUrl = "http://cql.hl7.org/02-authorsguide.html#code";
    public String getCqlCodeUrl() {
        return cqlCodeUrl;
    }

    private final String cqlConceptUrl = "http://cql.hl7.org/02-authorsguide.html#concept";
    public String getCqlConceptUrl() {
        return cqlConceptUrl;
    }

    private final String cqlDateTimeAndTimeUrl = "http://cql.hl7.org/02-authorsguide.html#date-datetime-and-time";
    public String getCqlDateTimeAndTimeUrl() {
        return cqlDateTimeAndTimeUrl;
    }

    private final String cqlDecimalUrl = "http://cql.hl7.org/02-authorsguide.html#decimal";
    public String getCqlDecimalUrl() {
        return cqlDecimalUrl;
    }

    private final String cqlIntegerUrl = "http://cql.hl7.org/02-authorsguide.html#integer";
    public String getCqlIntegerUrl() {
        return cqlIntegerUrl;
    }

    private final String cqlIntervalUrl = "http://cql.hl7.org/02-authorsguide.html#interval-values";
    public String getCqlIntervalUrl() {
        return cqlIntervalUrl;
    }

    private final String cqlQuantityUrl = "http://cql.hl7.org/02-authorsguide.html#quantities";
    public String getCqlQuantityUrl() {
        return cqlQuantityUrl;
    }

    private final String cqlStringUrl = "http://cql.hl7.org/02-authorsguide.html#string";
    public String getCqlStringUrl() {
        return cqlStringUrl;
    }

    private String qicoreDirPath;
    private FhirContext context;

    public QuickAtlas(String qicoreDirPath, FhirContext context) throws FileNotFoundException {
        this.qicoreDirPath = qicoreDirPath;
        this.context = context;

        // first step is to read in and store all the StructureDefinitions defined by the QiCore IG
        resolveQiCoreProfiles();
        // second step is to read in and store all the FHIR types
        resolveFhirTypes();
        // third step is to read in and store all the FHIR profiles
        resolveFhirProfiles();
        // fourth step is to read in and store all the FHIR extensions
        resolveFhirExtensions();
        // fifth step is to resolve the FHIR and CQL types in the link map
        resolveLinkMaps();
    }

    /**
     * Read and store all the QiCore profiles from the provided directory. Expects profile file names to start with "StructureDefinition-".
     * @throws FileNotFoundException - this Exception will not be raised as written, but must be accounted for when using FileReader
     */
    private void resolveQiCoreProfiles() throws FileNotFoundException {
        File qicoreOutput = new File(qicoreDirPath);
        if (!qicoreOutput.exists()) {
            throw new IllegalArgumentException("The provided qicore output directory doesn't exist.");
        }
        if (!qicoreOutput.isDirectory()) {
            throw new IllegalArgumentException("The provided qicore output directory path doesn't point to a directory");
        }
        File[] qicoreSds = qicoreOutput.listFiles(
                pathname -> pathname.getName().startsWith("StructureDefinition-") && pathname.getName().endsWith(".xml")
        );
        if (qicoreSds != null) {
            for (File file : qicoreSds) {
                StructureDefinition sd = context.newXmlParser().parseResource(StructureDefinition.class, new FileReader(file));
                if (sd.getType().equals("Extension")) {
                    qicoreExtensions.put(sd.getUrl(), sd);
                }
                else {
                    qicoreProfiles.put(sd.getType(), sd);
                    qicoreUrlToType.put(sd.getUrl(), sd.getType());
                }
            }
        }
        else {
            throw new IllegalArgumentException("No profiles were found in the provided QiCore directory.");
        }
    }

    /**
     * Read and store all the FHIR types provided in the file "profile-types.xml" (obtained from http://hl7.org/fhir/definitions.xml.zip).
     * This file MUST be stored in the src/main/resources/org/opencds/cqf/quick directory in this project.
     * Excludes Element and BackboneElement types.
     */
    private void resolveFhirTypes() {
        InputStream is = this.getClass().getResourceAsStream("profiles-types.xml");
        Bundle typeBundle = context.newXmlParser().parseResource(Bundle.class, new InputStreamReader(is));
        if (typeBundle.hasEntry()) {
            for (Bundle.BundleEntryComponent entry : typeBundle.getEntry()) {
                if (entry.hasResource() && entry.getResource() instanceof StructureDefinition) {
                    StructureDefinition sd = (StructureDefinition) entry.getResource();
                    if (sd.getType().equals("Element") || sd.getType().equals("BackboneElement")
                            || sd.getType().equals("Extension")) {
                        continue;
                    }

                    fhirTypes.put(sd.getType(), sd);
                }
            }
        }
        else {
            throw new IllegalArgumentException("Provided FHIR type Bundle entry is empty");
        }
    }

    /**
     * Read and store all the FHIR profiles provided in the file "profile-resources.xml" (obtained from http://hl7.org/fhir/definitions.xml.zip).
     * This file MUST be stored in the src/main/resources/org/opencds/cqf/quick directory in this project.
     * Excludes Resource profile.
     */
    private void resolveFhirProfiles() {
        InputStream is = this.getClass().getResourceAsStream("profiles-resources.xml");
        Bundle typeBundle = context.newXmlParser().parseResource(Bundle.class, new InputStreamReader(is));
        if (typeBundle.hasEntry()) {
            for (Bundle.BundleEntryComponent entry : typeBundle.getEntry()) {
                if (entry.hasResource() && entry.getResource() instanceof StructureDefinition) {
                    StructureDefinition sd = (StructureDefinition) entry.getResource();
                    if (sd.getType().equals("Resource")) {
                        continue;
                    }

                    fhirProfiles.put(sd.getType(), sd);
                }
            }
        }
        else {
            throw new IllegalArgumentException("Provided FHIR profile Bundle entry is empty");
        }
    }

    /**
     * Read and store all the FHIR extensions provided in the file "extension-definitions.xml" (obtained from http://hl7.org/fhir/definitions.xml.zip).
     * This file MUST be stored in the src/main/resources/org/opencds/cqf/quick directory in this project.
     */
    private void resolveFhirExtensions() {
        InputStream is = this.getClass().getResourceAsStream("extension-definitions.xml");
        Bundle extensionsBundle = context.newXmlParser().parseResource(Bundle.class, new InputStreamReader(is));
        if (extensionsBundle.hasEntry()) {
            for (Bundle.BundleEntryComponent entry : extensionsBundle.getEntry()) {
                if (entry.hasResource() && entry.getResource() instanceof StructureDefinition) {
                    StructureDefinition sd = (StructureDefinition) entry.getResource();
                    fhirExtensions.put(sd.getId(), sd);
                }
            }
        }
        else {
            throw new IllegalArgumentException("Provided FHIR extension Bundle entry is empty");
        }
    }

    /**
     * Resolve links to CQL types (point to the online spec) and for the complex FHIR types not covered by CQL.
     */
    private void resolveLinkMaps() {
        linkMap.put("Boolean", cqlBooleanUrl);
        primitiveMap.put("Boolean", cqlBooleanUrl);
        linkMap.put("Code", cqlCodeUrl);
        complexMap.put("Code", cqlCodeUrl);
        linkMap.put("Concept", cqlConceptUrl);
        complexMap.put("Concept", cqlConceptUrl);
        linkMap.put("DateTime", cqlDateTimeAndTimeUrl);
        primitiveMap.put("DateTime", cqlDateTimeAndTimeUrl);
        linkMap.put("Decimal", cqlDecimalUrl);
        primitiveMap.put("Decimal", cqlDecimalUrl);
        linkMap.put("Integer", cqlIntegerUrl);
        primitiveMap.put("Integer", cqlIntegerUrl);
        linkMap.put("Quantity", cqlQuantityUrl);
        complexMap.put("Quantity", cqlQuantityUrl);
        linkMap.put("String", cqlStringUrl);
        primitiveMap.put("String", cqlStringUrl);
        linkMap.put("Time", cqlDateTimeAndTimeUrl);
        primitiveMap.put("Time", cqlDateTimeAndTimeUrl);

        for (Map.Entry<String, StructureDefinition> entry : fhirTypes.entrySet()) {
            // skip FHIR Quantity in favor of CQL Quantity
            if (entry.getKey().equals("Quantity")) {
                continue;
            }
            linkMap.put(entry.getKey(), "QUICK-" + entry.getKey() + ".html");
        }
    }
}
