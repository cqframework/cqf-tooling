package org.opencds.cqf.tooling.acceleratorkit.util;

import java.io.File;
import java.util.Collection;

public class FilesBase {
    private final Collection<File> inputFiles;
    private final Collection<File> compareFiles;

    public FilesBase(Collection<File> inputFiles, Collection<File> compareFiles) {
        this.inputFiles = inputFiles;
        this.compareFiles = compareFiles;
    }

    public void listFilesAndCompare() {
        inputFiles.stream()
                .forEach(file -> {
                    String inputFileName = file.getName();
                    compareFiles.stream()
                            .filter(compareFile -> compareFile.isFile() && compareFile.getName().equalsIgnoreCase(inputFileName))
                            .findFirst()
                            .ifPresent(cf -> compareFilesAndAssertIfNotEqual(file, cf));
                });
    }

    protected void compareFilesAndAssertIfNotEqual(File inputFile, File compareFile){
        FilesComparator comparator = new StringFileComparator();
        if(inputFile.getName().endsWith("json")){
            comparator = new JsonFileComparator();
        }
        comparator.compareFilesAndAssertIfNotEqual(inputFile, compareFile);
    }

    public Collection<File> getInputFiles() {
        return inputFiles;
    }

    public Collection<File> getCompareFiles() {
        return compareFiles;
    }
}
