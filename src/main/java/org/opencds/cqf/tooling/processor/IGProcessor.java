package org.opencds.cqf.tooling.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;

import ca.uhn.fhir.context.FhirContext;

public class IGProcessor {
    public enum IGVersion {
        FHIR3("fhir3"), FHIR4("fhir4");

        private String string;

        public String toString() {
            return this.string;
        }

        private IGVersion(String string) {
            this.string = string;
        }

        public static IGVersion parse(String value) {
            switch (value) {
            case "fhir3":
                return FHIR3;
            case "fhir4":
                return FHIR4;
            default:
                throw new RuntimeException("Unable to parse IG version value:" + value);
            }
        }
    }

    //mega ig method
    public static void publishIG(RefreshIGParameters params) {
        String igPath = params.igPath;
        String igResourcePath = params.igResourcePath;
        IGVersion igVersion = params.igVersion;
        Encoding encoding = params.outputEncoding;
        Boolean includeELM = params.includeELM;
        Boolean includeDependencies = params.includeDependencies;
        Boolean includeTerminology = params.includeTerminology;
        Boolean includePatientScenarios = params.includePatientScenarios;
        Boolean versioned = params.versioned;
        Boolean cdsHooksIg = params.cdsHooksIg;
        String fhirUri = params.fhirUri;
        String measureToRefreshPath = params.measureToRefreshPath;
        ArrayList<String> resourceDirs = params.resourceDirs;

        IOUtils.resourceDirectories.addAll(resourceDirs);

        FhirContext fhirContext = IGProcessor.getIgFhirContext(igVersion);

        igPath = Paths.get(igPath).toAbsolutePath().toString();

        //Use case 1
        //scafold basic templating for the type of content, Measure, PlanDefinition, or Questionnaire
        //Give it a name and generate all the files, cql, library, measure... should be very basic template
        //ScaffoldProcessor.scaffold(ScaffoldParameters);

        //Use case 2 while developing in Atom refresh content and run tests for either entire IG or targeted Artifact
        //refreshcontent
        LogUtils.info("IGProcessor.publishIG - refreshIG");
        IGRefreshProcessor.refreshIG(params);
        //validate
        //ValidateProcessor.validate(ValidateParameters);
        //run all tests
        //IGTestProcessor.testIg(IGTestParameters);

        //Use case 3
        //package everything
        LogUtils.info("IGProcessor.publishIG - bundleIg");
        IGBundleProcessor.bundleIg(IGRefreshProcessor.refreshedResourcesNames, igPath, encoding, includeELM, includeDependencies, includeTerminology, includePatientScenarios,
        versioned, cdsHooksIg, fhirContext, fhirUri);
        //test everything
        //IGTestProcessor.testIg(IGTestParameters);
        //Publish?
    }

    
    public static FhirContext getIgFhirContext(IGVersion igVersion)
    {
        switch (igVersion) {
            case FHIR3:
                return FhirContext.forDstu3();
            case FHIR4:
                return FhirContext.forR4();
            default:
                throw new IllegalArgumentException("Unknown IG version: " + igVersion);
        }     
    }
    
    public static IGVersion getIgVersion(String igPath){
        IGVersion igVersion = null;
        List<File> igPathFiles = IOUtils.getFilePaths(igPath, false).stream()
            .map(path -> new File(path))
            .collect(Collectors.toList());
        for (File file : igPathFiles) {
            if (FilenameUtils.getExtension(file.getName()).equals("ini")) {
                igVersion = tryToReadIni(file);
            }
        }
        if (igVersion == null) {
            throw new IllegalArgumentException("IG version must be configured in ig.ini or provided as an argument.");
        }
        else return igVersion;
    }

    private static IGVersion tryToReadIni(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            String igIniContent = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
            String[] contentLines = igIniContent.split("\n");
            inputStream.close();
            return parseVersion(contentLines);
        } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
    }

    static final String STU3SPECIFIER = "stu3";
    static final String DSTU3SPECIFIER = "dstu3";
    static final String R4SPECIFIER = "r4";
    private static IGVersion parseVersion(String[] contentLines) {
        for (String line : contentLines) {
            if (line.toLowerCase().startsWith("fhirspec"))
            {
                if (line.toLowerCase().contains(R4SPECIFIER)){
                    return IGVersion.FHIR4;
                }
                else if (line.toLowerCase().contains(STU3SPECIFIER) || line.toLowerCase().contains(DSTU3SPECIFIER)) {
                    return IGVersion.FHIR3;
                }
            }
        }
        return null;
    }

    public static final String bundlePathElement = "bundles/";
    public static String getBundlesPath(String igPath) {
        return FilenameUtils.concat(igPath, bundlePathElement);
    }
    public static final String cqlLibraryPathElement = "input/pagecontent/cql/";
    public static final String libraryPathElement = "input/resources/library/";
    public static final String measurePathElement = "input/resources/measure/";
    public static final String valuesetsPathElement = "input/vocabulary/valueset/";
    public static final String testCasePathElement = "input/tests/";
    
    public static void ensure(String igPath, Boolean includePatientScenarios, Boolean includeTerminology, ArrayList<String> resourcePaths) {                
        File directory = new File(getBundlesPath(igPath));
        if (!directory.exists()) {
            directory.mkdir();
        }        
        if (resourcePaths.isEmpty()) {
            ensureDirectory(igPath, IGProcessor.cqlLibraryPathElement);
            ensureDirectory(igPath, IGProcessor.libraryPathElement);
            ensureDirectory(igPath, IGProcessor.measurePathElement);
            ensureDirectory(igPath, IGProcessor.valuesetsPathElement);
            ensureDirectory(igPath, IGProcessor.testCasePathElement);
        }
        else {
            checkForDirectory(igPath, IGProcessor.cqlLibraryPathElement);
            checkForDirectory(igPath, IGProcessor.libraryPathElement);
            checkForDirectory(igPath, IGProcessor.measurePathElement);
            checkForDirectory(igPath, IGProcessor.valuesetsPathElement);
            checkForDirectory(igPath, IGProcessor.testCasePathElement);
        }
        HashSet<String> cqlContentPaths = IOUtils.getCqlLibraryPaths();
        for (String cqlContentPath : cqlContentPaths) {
            String cqlLibraryContent = IOUtils.getCqlString(cqlContentPath);
            if (!cqlLibraryContent.startsWith("library ")) {
                throw new RuntimeException("Unable to refresh IG.  All Libraries must begin with \"library \": " + cqlContentPath);
            }
            String strippedLibraryName = FilenameUtils.getBaseName(cqlContentPath);
            for (IGProcessor.IGVersion igVersion : IGVersion.values()) {                
                String igVersionToken = "_" + igVersion.toString().toUpperCase();
               
                if (strippedLibraryName.contains(igVersionToken)) {
                    strippedLibraryName = strippedLibraryName.replace(igVersionToken, "");
                    if (strippedLibraryName.contains("_")) {
                        throw new RuntimeException("Convention only allows a single \"_\" and it must be preceeding the IG Version: " + cqlContentPath);
                    }
                }    
            }
            if (strippedLibraryName.contains("_")) {
                throw new RuntimeException("Convention only allows a single \"_\" and it must be preceeding the IG Version: " + cqlContentPath);
            }
        }
    }

    private static void ensureDirectory(String igPath, String pathElement) {
        File directory = new File(FilenameUtils.concat(igPath, pathElement));
        if (!directory.exists()) {
            throw new RuntimeException("Convention requires the following directory:" + pathElement);
        }
        // TODO: This is a concept different from "resource directories". It is expected elsewhere (e.g., IOUtils.setupActivityDefinitionPaths)
        // that resourceDirectories contains a set of proper "resource" directories. Adding non-resource directories
        // leads to surprising results when bundling like picking up resources from the /tests directory.
        IOUtils.resourceDirectories.add(FilenameUtils.concat(igPath, pathElement));
    }

    private static void checkForDirectory(String igPath, String pathElement) {
        File directory = new File(FilenameUtils.concat(igPath, pathElement));
        if (!directory.exists()) {
            System.out.println("No directory found by convention for: " + directory.getName());
        }
        else {
            // TODO: This is a concept different from "resource directories". It is expected elsewhere (e.g., IOUtils.setupActivityDefinitionPaths)
            // that resourceDirectories contains a set of proper "resource" directories. Adding non-resource directories
            // leads to surprising results when bundling like picking up resources from the /tests directory.
            IOUtils.resourceDirectories.add(FilenameUtils.concat(igPath, pathElement));
        }
    }
}
