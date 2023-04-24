package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class StripGeneratedContentOperation extends Operation {

    private String pathToResource;

    private boolean isDirectory;

    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals("-StripGeneratedContent")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1].replace("\"", ""); // Strip quotes

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break;
                case "pathtores":
                case "ptr":
                    pathToResource = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
        File resource  = validateDirectory(pathToResource);
        List<File> files = getListOfActionableFiles(resource);

        for(File file : files) {

        }

    }

    private File validateDirectory(String pathToDir) {
        if (pathToDir == null) {
            throw new IllegalArgumentException("The path to the resource is required");
        }

        File bundleDirectory = new File(pathToDir);
        if (bundleDirectory.isDirectory()) {
            isDirectory = true;
        }
        return bundleDirectory;
    }

    private List<File> getListOfActionableFiles(File file) {
        if (!isDirectory) {
            return List.of(file);
        }
        List<File> files = Arrays.asList(file.listFiles(f -> !f.isDirectory()));
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("The specified path to resource is empty");
        }
        return files;
    }
}
