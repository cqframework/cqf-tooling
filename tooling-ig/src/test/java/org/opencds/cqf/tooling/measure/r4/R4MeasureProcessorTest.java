package org.opencds.cqf.tooling.measure.r4;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import java.io.File;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.measure.MeasureProcessorTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class R4MeasureProcessorTest extends MeasureProcessorTest {
    private String resourceDirectory = "r4";

    public R4MeasureProcessorTest() {
        super(new R4MeasureProcessor(), FhirContext.forCached(FhirVersionEnum.R4));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        // IOUtils.resourceDirectories = new ArrayList<String>();
        // IOUtils.clearDevicePaths();
        File dir = Paths.get("target", "refreshMeasures", "r4").toFile();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    @Test
    public void testRefreshMeasures() throws Exception {
        // String targetDirectory = Paths.get("target", "refreshMeasures", this.resourceDirectory).toString();
        // copyResourcesToTargetDir(targetDirectory, this.resourceDirectory);

        // String measurePath = Paths.get("", "input", "resources", "measure",
        //     "measure-EXM124_FHIR4-8.2.000.json").toString();
        // runRefresh(
        //     targetDirectory,
        //     targetDirectory + measurePath,
        //     targetDirectory + Paths.get("", "input", "pagecontent", "cql",
        //         "EXM124_FHIR4-8.2.000.cql").toString(),
        //     false
        // );

        // validateCqfmSofwareSystemExtension(targetDirectory + measurePath);

    }

    @Test
    public void testRefreshMeasureContent() throws Exception {
        // String targetDirectory = Paths.get("target", "refreshMeasures", this.resourceDirectory).toString();
        // copyResourcesToTargetDir(targetDirectory, this.resourceDirectory);

        // String measurePath = Paths.get("", "input", "resources", "measure",
        //     "measure-EXM124_FHIR4-8.2.000.json").toString();
        // runRefresh(
        //     targetDirectory,
        //     targetDirectory + measurePath,
        //     targetDirectory + Paths.get("", "input", "pagecontent", "cql",
        //         "EXM124_FHIR4-8.2.000.cql").toString(),
        //     false
        // );

        // validateCqfmSofwareSystemExtension(targetDirectory + measurePath);

    }
}
