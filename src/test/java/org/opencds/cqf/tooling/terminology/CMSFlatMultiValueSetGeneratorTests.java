package org.opencds.cqf.tooling.terminology;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CMSFlatMultiValueSetGeneratorTests {

    @Test
    public void testValueSetGenerator() {
        VSACBatchValueSetGenerator generator = new VSACBatchValueSetGenerator();
        generator.execute(new String[] {
                "-VsacXlsxToValueSetBatch",
                "-ptsd=\"src\\test\\resources\\org\\opencds\\cqf\\tooling\\terminology\"",
                "-op=\"src\\test\\resources\\org\\opencds\\cqf\\tooling\\terminology\\output\"",
                "-setname=true",
                "-vssrc=cms"
        });

        File outputPath = Paths.get("src\\test\\resources\\org\\opencds\\cqf\\tooling\\terminology\\output").toFile();
        int fileCount = 0;
        for (File outputFile : outputPath.listFiles()) {
            fileCount++;
            if (!outputFile.getName().startsWith("valueset-")) {
                throw new IllegalArgumentException("Output file should start with valueset- prefix");
            }
        }
        if (fileCount != 567) {
            throw new IllegalArgumentException("Exected 567 value set files");
        }
    }
}
