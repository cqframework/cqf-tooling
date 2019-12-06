package org.opencds.cqf.bundler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MeasureReport;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.Operation;
import org.opencds.cqf.utilities.BundleUtils;
import org.opencds.cqf.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class STU3TestsBundler extends Operation {

    private String pathToTestsDir;
    private String encoding;
    private FhirContext fhirContext;

    private Map<File, List<IAnyResource>> testResourcesMap = new HashMap<>();

    public STU3TestsBundler() {
        this.fhirContext = FhirContext.forDstu3();
    }

    @Override
    public void execute(String[] args) {
        parseArgs(args);
        readResources();
        output();
    }

    private void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.equals("-bundleTests")) continue;
    
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];
    
            switch (flag.replace("-", "").toLowerCase()) {
                case "pathtotestsdir": case "pttd": pathToTestsDir = value; break;
                case "encoding": case "e": encoding = value.toLowerCase(); break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
    
        }
        if (pathToTestsDir == null) {
            throw new IllegalArgumentException("The path to the testsDir is required");
        }
    }

    private void readResources() {
        ArrayList<File> allMeasureTestDirs = IOUtils.getFilesFromDir(pathToTestsDir);
        for (File measureTestDir : allMeasureTestDirs) {
            ArrayList<File> testScenarios = IOUtils.getFilesFromDir(measureTestDir.getPath().toString());
            for (File testScenario : testScenarios) {
                if (testScenario.isFile()) {
                    if (!isMeasureReport(testScenario)) continue;
                }
                testResourcesMap.put(
                    testScenario,
                    IOUtils.readResourcesFromDir(testScenario.getPath().toString(), fhirContext, true));
            }
        }
    }

    private boolean isMeasureReport(File file) {
        try {
            MeasureReport measureReport = (MeasureReport) IOUtils.readResource(file.getPath().toString(), fhirContext);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void output() {
        for (Map.Entry<File, List<IAnyResource>> entry : testResourcesMap.entrySet())
        {
            if (entry.getValue() != null) {
                Bundle testScenarioBundle = BundleUtils.bundleStu3Artifacts(entry.getValue(), "bundle-" + entry.getKey().getName());
                IOUtils.writeResource(testScenarioBundle, testScenarioBundle.getId(), entry.getKey().getParent().toString(), encoding, fhirContext);
            }
        }
    }

}

