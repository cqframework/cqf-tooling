package org.opencds.cqf.tooling.acceleratorkit;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.acceleratorkit.util.FilesBase;
import org.testng.annotations.AfterClass;

public class BaseProcessorTest {
    static final String separator = System.getProperty("file.separator");
    static final String resourcesPath = "src/test/resources/acceleratorkit";

    @AfterClass
    public void destroy() throws IOException {
        FileUtils.deleteDirectory(new File(resourcesPath + "/out"));
    }

    protected void compareFiles(String inputFilePath, String compareFilePath) {
        List<File> inputFilePaths = readFiles(inputFilePath);
        List<File> compareFilePaths = readFiles(compareFilePath);
        FilesBase filesBase = new FilesBase(inputFilePaths, compareFilePaths);
        //check if the list sizes are equals
        assertEquals(filesBase.getInputFiles().size(), filesBase.getCompareFiles().size());
        filesBase.listFilesAndCompare();
    }

    protected List<File> readFiles(String path){
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
