package org.opencds.cqf.tooling.measure.r4;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.measure.MeasureProcessorTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

public class R4MeasureProcessorTest extends MeasureProcessorTest {
    private String resourceDirectory = "r4";
    public R4MeasureProcessorTest() {
        super(new R4MeasureProcessor(), FhirContext.forCached(FhirVersionEnum.R4));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        //IOUtils.resourceDirectories = new ArrayList<String>();
        //IOUtils.clearDevicePaths();
        File dir  = new File("target" + separator + "refreshMeasures" + separator + "r4");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    @Test
    public void testRefreshMeasures() throws Exception {
        // String targetDirectory = "target" + separator + "refreshMeasures" + separator + "" + this.resourceDirectory;
        // copyResourcesToTargetDir(targetDirectory, this.resourceDirectory);
        
        // String measurePath = separator + "input" + separator + "resources" + separator + "measure" + separator + "measure-EXM124_FHIR4-8.2.000.json";
        // runRefresh(
        //     targetDirectory,
        //     targetDirectory + measurePath,
        //     targetDirectory + separator + "input" + separator + "pagecontent" + separator + "cql" + separator + "EXM124_FHIR4-8.2.000.cql",
        //     false
        // );

        // validateCqfmSofwareSystemExtension(targetDirectory + measurePath);

    }

    @Test
    public void testRefreshMeasureContent() throws Exception {
        // String targetDirectory = "target" + separator + "refreshMeasures" + separator + "" + this.resourceDirectory;
        // copyResourcesToTargetDir(targetDirectory, this.resourceDirectory);
        
        // String measurePath = separator + "input" + separator + "resources" + separator + "measure" + separator + "measure-EXM124_FHIR4-8.2.000.json";
        // runRefresh(
        //     targetDirectory,
        //     targetDirectory + measurePath,
        //     targetDirectory + separator + "input" + separator + "pagecontent" + separator + "cql" + separator + "EXM124_FHIR4-8.2.000.cql",
        //     false
        // );

        // validateCqfmSofwareSystemExtension(targetDirectory + measurePath);

    }
    
}
