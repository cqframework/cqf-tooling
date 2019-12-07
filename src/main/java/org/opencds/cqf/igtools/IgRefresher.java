package org.opencds.cqf.igtools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.*;
import org.opencds.cqf.Operation;
import org.opencds.cqf.bundler.BundleResources;
import org.opencds.cqf.library.stu3.LibraryGenerator;
import org.opencds.cqf.library.stu3.LibraryRefresher;
import org.opencds.cqf.bundler.BundleTestCasesOperation;
import org.opencds.cqf.terminology.VSACBatchValueSetGenerator;

import ca.uhn.fhir.context.FhirContext;

public class IgRefresher extends Operation {
    private FhirContext fhirContext;
    
    private String pathToIg; // -pathtoig | -ptig
    private String encoding = "json"; // -encoding (-e)

    private File igDir;
    private File cqlDir;
    private File resourcesDir;
    private File baseBundleDir;

    private ArrayList<File> cqlFiles;

    private File testsDir;

    @Override
    public void execute(String[] args) {
        parseArgs(args);
        ensureIgHasRequiredDirectories();
        setCqlFiles();
        refreshLibraries();
        bundleTests();
        //bundleValueSets(); //not yet ready
        //refreshBundles();  //not yet ready
        //bundleTests();  //not yet ready

    }

    private void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.equals("-RefreshIg")) continue;

            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "pathtoig": case "ptig": pathToIg = value; break;
                case "encoding": case "e": encoding = value.toLowerCase(); break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }

        }
        if (pathToIg == null) {
            throw new IllegalArgumentException("The path to the IG is required");
        }
    }

    private void ensureIgHasRequiredDirectories() {

        igDir = new File(pathToIg);
        if (!igDir.isDirectory()) {
            throw new IllegalArgumentException("The specified path to IG is not a directory");
        }
        cqlDir = new File(pathToIg + "/cql");
        if (!cqlDir.isDirectory()) {
            throw new IllegalArgumentException("IG must include a cql directory"); //need to change these to just continue if not there
        }
        resourcesDir = new File(pathToIg + "/resources");
        if (!resourcesDir.isDirectory()) {
            throw new IllegalArgumentException("IG must include a resources directory"); //need to change these to just continue if not there
        }

        testsDir = new File(pathToIg + "/tests");
        if (!testsDir.isDirectory()) {
            throw new IllegalArgumentException("IG must include a tests directory"); //need to change these to just continue if not there
        }

        baseBundleDir = new File(pathToIg + "/bundles");
        if (! baseBundleDir.exists()){
            baseBundleDir.mkdir();
        }
        
    }

    private void setCqlFiles() {
        cqlFiles = new ArrayList<File>(Arrays.asList(cqlDir.listFiles()));
        if (cqlFiles == null) {
            return;
        }
        else if (cqlFiles.isEmpty()) {
            return;
        }
    }

    private void refreshLibraries() {
        for (File cqlFile : cqlFiles) {
            String cqlFilePath = cqlFile.getPath();
            File libraryDir = new File(resourcesDir + "/library");

            ArrayList<File> libraryFiles = new ArrayList<File>(Arrays.asList(libraryDir.listFiles()));
            List<File> matchingLibrariesCollection = libraryFiles.stream()
            .filter(library -> containsAllChars(library.getName(), cqlFile.getName()))
            .collect(Collectors.toList());

            if(!matchingLibrariesCollection.isEmpty()) {
                File libraryFile = matchingLibrariesCollection.get(0);
                if(libraryFile != null) {
                    LibraryRefresher STU3LibraryRefresher = new LibraryRefresher();
                    try {
                        STU3LibraryRefresher.execute(buildRefreshLibraryArgs(cqlFilePath, libraryFile.getPath().toString()));
                    }
                    catch(Exception e) {
                        System.out.println("Error while refreshing " + cqlFile.getName() + ":");
                        System.out.println(e.getMessage());
                    }
                }
            }
            else {
                LibraryGenerator STU3LibraryGenerator = new LibraryGenerator();
                try {
                    STU3LibraryGenerator.execute(buildGenerateLibraryArgs(cqlFilePath));
                }
                catch(Exception e) {
                    System.out.println("Error while refreshing " + cqlFile.getName() + ":");
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private String[] buildGenerateLibraryArgs(String cqlFilePath) {
        return new String[] {
            "-ptcql=" + cqlFilePath,
            "-e=" + encoding,
            "-op=" + pathToIg + "/resources/library"  //might be just resourcesDir + "/library"
        };
    }

    private String[] buildRefreshLibraryArgs(String cqlFilePath, String pathToLibrary) {
        return new String[] {
            "-ptcql=" + cqlFilePath,
            "-ptl=" + pathToLibrary,
            "-e=" + encoding,
            "-op=" + pathToIg + "/resources/library"  //might be just resourcesDir + "/library"
        };
    }

    private void bundleTests() {
        BundleTestCasesOperation stu3TestBundler = new BundleTestCasesOperation();
        try {
            stu3TestBundler.execute(buildSTU3TestsBundlerArgs());
        } catch (Exception e) {
            System.out.println("error bundling tests: ");
            System.out.println(e.getMessage());
        }
    }

    private String[] buildSTU3TestsBundlerArgs() {
        return new String[] {
            "-bundleTests",
            "-pttd=" + testsDir.getPath().toString(),
            "-e=" + encoding
        };
    }

    private Set<Character> stringToCharacterSet(String s) {
        Set<Character> set = new HashSet<>();
        for (char c : s.toCharArray()) {
            set.add(c);
        }
        return set;
    }
    
    private boolean containsAllChars
        (String container, String containee) {
        return stringToCharacterSet(container).containsAll
                   (stringToCharacterSet(containee));
    }



}
