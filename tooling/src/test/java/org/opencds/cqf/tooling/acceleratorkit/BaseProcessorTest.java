package org.opencds.cqf.tooling.acceleratorkit;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BaseProcessorTest {
    protected void countFiles(String filePath, int expectedCount) {
        List<File> files = readFiles(filePath);
        assertEquals(files.size(), expectedCount, String.format("expected filePath %s to contain %d files. Found %d files instead.", filePath, expectedCount, files.size()));
    }

    protected List<File> readFiles(String path){
        if (!Files.exists(Paths.get(path))) {
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(r -> new File(r.toUri()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
