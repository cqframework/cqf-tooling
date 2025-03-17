package org.opencds.cqf.tooling.measure;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.parameter.RefreshMeasureParameters;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;

public abstract class MeasureProcessorTest extends RefreshTest {

    private MeasureProcessor measureProcessor;
    @BeforeMethod
    public void setUp() throws Exception {
        //IOUtils.resourceDirectories = new ArrayList<String>();
        //IOUtils.clearDevicePaths();
        File dir  = new File("target" + separator + "refreshMeasures");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    public MeasureProcessorTest(MeasureProcessor measureProcessor, FhirContext fhirContext) {
        super(fhirContext);
        this.measureProcessor = measureProcessor;
    }

    protected MeasureProcessor getMeasureProcessor() {
        return measureProcessor;
    }

    protected void runRefresh(String targetDirectory, String measureResourcePath, String cqlResourcePath,
            boolean versioned) throws IOException {
        runRefresh(targetDirectory, measureResourcePath, null, cqlResourcePath, versioned);
    }

    protected void runRefresh(String targetDirectory, String measurePath, String measureOutputDirectory,
            String cqlResourcePath, boolean versioned) throws IOException {
        RefreshMeasureParameters params = new RefreshMeasureParameters();
        params.encoding = Encoding.JSON;
        params.fhirContext = getFhirContext();
        params.measurePath = measurePath;
        params.measureOutputDirectory = measureOutputDirectory;
        params.cqlContentPath = cqlResourcePath;
        params.ini = targetDirectory + separator + "ig.ini";
        params.versioned = versioned;
        getMeasureProcessor().refreshMeasureContent(params);
    }
}
