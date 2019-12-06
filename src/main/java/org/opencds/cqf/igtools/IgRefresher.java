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
    private ArrayList<File> bundleDirs = new ArrayList<>();
    private ArrayList<File> igFiles;

    private File testsDir;

    @Override
    public void execute(String[] args) {
        parseArgs(args);
        ensureIgHasRequiredDirectories();
        setCqlFiles();
        refreshLibraries();
        refreshValueSets();
        refreshBundles();  //not yet ready
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

    private void refreshValueSets() {
        String pathToValueSetSpreadsheetDirectory = pathToIg + "/resources/valuesets/spreadsheets";  //think about possibly including this as an argument because it is not defined by the IG
        VSACBatchValueSetGenerator VSACBatchValueSetGenerator = new VSACBatchValueSetGenerator();
        try {
            VSACBatchValueSetGenerator.execute(buildRefreshValueSetArgs(pathToValueSetSpreadsheetDirectory));
            //add a if there is no bundles dir create one and
            
        } catch (Exception e) {
            System.out.println("error refreshing valuesets");
            System.out.println(e.getMessage());
        }
    }

    private String[] buildRefreshValueSetArgs(String pathToSpreadsheetDirectory) {
        return new String[] {
            "-VsacXlsxToValueSetBatch",
            "-ptsd=" + pathToSpreadsheetDirectory,
            "-op=" + pathToIg + "/resources/valuesets",  //might be just resourcesDir + "/valuesets"
            "-vssrc=cms"
        };
    }

    private void refreshBundles() {
        
        try (Stream<Path> bundlePaths = Files.find(
            igDir.toPath(), Integer.MAX_VALUE,
            (path, basicAttributes) ->  path.getFileName().toFile().getName().equals("bundles") && basicAttributes.isDirectory())
            ) {
                bundlePaths.forEach(path -> bundleDirs.add(path.toFile()));
            } 
            catch (Exception e) {
                System.out.println("could not refresh bundles due to an error");
                System.out.println(e.getMessage());
            }
            
        if (bundleDirs == null) {
            return;
        }
        else if (bundleDirs.isEmpty()) {
            return;
        }
        bundleDirs.remove(baseBundleDir);
        if (bundleDirs == null) {
            return;
        }
        else if (bundleDirs.isEmpty()) {
            return;
        }
        buildMasterBundle(bundleDirs);
    }

    private void buildMasterBundle(ArrayList<File> bundleDirs) {
        //walk through ig dir and if path starts ends with bundles but isnt ig/bundles (base bundles path) then bundleResources and copy all files to ig/bundles/name/
        Bundle masterBundle = new Bundle(); //still needs to be implemented
        for (File bundleDir : bundleDirs) {
            File[] bundleFiles = bundleDir.listFiles();
            if (bundleFiles == null) {
                return;
            }
            else if (bundleFiles.length == 0) {
                return;
            }
            buildDirectorySpecificBundleAndCopy(bundleDir, bundleFiles);
        }
        //create File masterBundle = new File(master-bundle.json) //should probably have parameter for encoding
        //write masterBundle to pathToIg + "/bundles/" + masterBundle.getName()


        //I want to walk through every directory and check for a bundles dir,
        //if there is one, bundle all the bundles and call it all-{parentDirName}-bundle.json
        //and copy all into a new directory igDirPath/bundles/parentDirName
    }

    private void buildDirectorySpecificBundleAndCopy(File bundleDir, File[] bundleFiles) {
        File baseParentDirBundlesDir = new File(pathToIg + "/bundles/" + bundleDir.getParentFile().getName());
        if (! baseParentDirBundlesDir.exists()){
            baseParentDirBundlesDir.mkdir();
        }
        //bundleResources(bundleDir.getPath());  //not yet working
        try {
            FileUtils.copyDirectory(bundleDir, baseParentDirBundlesDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //create File allBundlesFile = new File(all-bundleFile.getParentPath-bundle.json) //should probably have parameter for encoding
        //write allBundlesBundle to baseParentDirBundlesDir.getPath() + "/" + allBundlesFile.getName()
        //add allBundlesBundle to master bundle
    }

    private void bundleResources(String path) {
        String pathToResourceDir = path + "/";
        BundleResources BundleResources = new BundleResources();
        BundleResources.execute(buildBundleResourcesArgs(pathToResourceDir));
    }

    private String[] buildBundleResourcesArgs(String pathToResourceDir) {
        return new String[] {
            "-BundleResources",
            "-ptd=" + pathToResourceDir,
            "-op=" + pathToResourceDir,  //might be just resourcesDir + "/valuesets"
            "-v=stu3"
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
