package org.opencds.cqf.tooling.terminology;

import java.io.File;
import java.nio.file.Paths;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class CMSFlatMultiValueSetGeneratorIT {

    @Ignore("The test set here is too big. Even with 4Gb of memory it fails on the build server. Need a smaller set.")
    @Test
    public void testValueSetGenerator() {
        VSACBatchValueSetGenerator generator = new VSACBatchValueSetGenerator();
        generator.execute(new String[] {
                "-VsacXlsxToValueSetBatch",
                "-ptsd=src/test/resources/org/opencds/cqf/tooling/terminology",
                "-op=target/test/resources/org/opencds/cqf/tooling/terminology/output",
                "-setname=true",
                "-vssrc=cms"
        });

        File outputPath = Paths.get("target/test/resources/org/opencds/cqf/tooling/terminology/output").toFile();
        int fileCount = 0;
        for (File outputFile : outputPath.listFiles()) {
            fileCount++;
            if (!outputFile.getName().startsWith("valueset-")) {
                throw new IllegalArgumentException("Output file should start with valueset- prefix");
            }
        }
        if (fileCount != 567) {
            throw new IllegalArgumentException("Expected 567 value set files");
        }
    }
}
