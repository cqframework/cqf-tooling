package org.opencds.cqf.igtools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import org.hl7.fhir.dstu3.model.*;
import org.opencds.cqf.Operation;
import org.opencds.cqf.library.STU3MultiLibraryGenerator;
import org.opencds.cqf.terminology.VSACBatchValueSetGenerator;

import ca.uhn.fhir.context.FhirContext;

public class IgRefresher extends Operation {
    private FhirContext fhirContext;
    
    private String pathToIg; // -pathtoig | -ptig
    private String encoding = "json"; // -encoding (-e)

    private File igDir;
    private File cqlDir;
    private File resourcesDir;

    private File[] cqlFiles;
    private File[] igFiles;
    
    //Map igFiles = igDir.listFiles(); I want to create a map of the directories and files to use throughout in the future

    @Override
    public void execute(String[] args) {
        parseArgs(args);
        ensureIsIg();
        setCqlFiles();
        refreshLibraries(cqlFiles);
        refreshValueSets();
        //refreshBundles();  //not yet ready

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

    private void ensureIsIg() {

        igDir = new File(pathToIg);
        if (!igDir.isDirectory()) {
            throw new IllegalArgumentException("The specified path to IG is not a directory");
        }
        cqlDir = new File(pathToIg + "/cql");
        if (!cqlDir.isDirectory()) {
            throw new IllegalArgumentException("IG must include a cql directory");
        }
        resourcesDir = new File(pathToIg + "/resources");
        if (!resourcesDir.isDirectory()) {
            throw new IllegalArgumentException("IG must include a resources directory");
        }
        
    }

    private void setCqlFiles() {
        cqlFiles = cqlDir.listFiles();
        if (cqlFiles == null) {
            return;
        }
        else if (cqlFiles.length == 0) {
            return;
        }
    }

    private void refreshLibraries(File[] cqlFiles) {
        for (File cqlFile : cqlFiles) {
            String cqlFilePath = cqlFile.getPath();
            STU3MultiLibraryGenerator STU3MultiLibraryGenerator = new STU3MultiLibraryGenerator();
            try {
                STU3MultiLibraryGenerator.execute(buildRefreshLibraryArgs(cqlFilePath));
            }
            catch(Exception e) {
                System.out.println("Error while refreshing " + cqlFile.getName() + ":");
                System.out.println(e.getMessage());
            }
        }
    }

    private String[] buildRefreshLibraryArgs(String cqlFilePath) {
        return new String[] {
            "-ptcql=" + cqlFilePath,
            "-e=" + encoding,
            "-op=" + pathToIg + "/resources/library"  //might be just resourcesDir + "/library"
        };
    }

    private void refreshValueSets() {
        String pathToValueSetSpreadsheetDirectory = pathToIg + "/resources/valuesets/spreadsheets";  //think about possibly including this as an argument because it is not defined by the IG
        VSACBatchValueSetGenerator VSACBatchValueSetGenerator = new VSACBatchValueSetGenerator();
        VSACBatchValueSetGenerator.execute(buildRefreshValueSetArgs(pathToValueSetSpreadsheetDirectory));
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
        File[] bundleDirs = igDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.equals("bundles") && !dir.getAbsolutePath().equals(pathToIg + "/bundles"); //make sure to not include base bundles dir
            }
        });
        if (bundleDirs == null) {
            return;
        }
        else if (bundleDirs.length == 0) {
            return;
        }
        buildMasterBundle(bundleDirs);
    }

    private void buildMasterBundle(File[] bundleDirs) {
        Bundle masterBundle = new Bundle();
        for (File bundleDir : bundleDirs) {
            File[] bundleFiles = bundleDir.listFiles();
            if (bundleFiles == null) {
                return;
            }
            else if (bundleFiles.length == 0) {
                return;
            }
            buildDirectorySpecificBundle(bundleDir, bundleFiles);
        }
        //create File masterBundle = new File(master-bundle.json) //should probably have parameter for encoding
        //write masterBundle to pathToIg + "/bundles/" + masterBundle.getName()


        //I want to walk through every directory and check for a bundles dir,
        //if there is one, bundle all the bundles and call it all-{parentDirName}-bundle.json
        //and copy all into a new directory igDirPath/bundles/parentDirName
    }

    private void buildDirectorySpecificBundle(File bundleDir, File[] bundleFiles) {
        File baseParentDirBundlesDir = new File(pathToIg + "/bundles/" + bundleDir.getName());
        if (! baseParentDirBundlesDir.exists()){
            baseParentDirBundlesDir.mkdir();
        }
        Bundle allBundles = new Bundle();
        for (File bundleFile : bundleFiles) {
            buildBundle(baseParentDirBundlesDir, bundleFile);

        }
        //create File allBundlesFile = new File(all-bundleFile.getParentPath-bundle.json) //should probably have parameter for encoding
        //write allBundlesBundle to baseParentDirBundlesDir.getPath() + "/" + allBundlesFile.getName()
        //add allBundlesBundle to master bundle
    }

    private void buildBundle(File baseParentDirBundlesDir, File bundleFile) {
        Bundle bundle = new Bundle();
        //parse json files for bundle resources
        //run through each bundle resource and add it to a bundle as an entry              
        writeBundleToFile(baseParentDirBundlesDir, bundleFile, bundle);
         // masterBundle.addEntry().setResource(bundle).setRequest(new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.PUT).setUrl("Library/" + entry.getValue().getId()));
        //add bundle to allBundlesBundle
        //add bundle to master bundle
    }

    private void writeBundleToFile(File baseParentDirBundlesDir, File bundleFile, Bundle bundle) {
        //write bundle to baseParentDirBundlesDir.getPath() + "/" + bundleFile.getName()
        if (bundle != null) {
            try (FileOutputStream writer = new FileOutputStream(baseParentDirBundlesDir.getPath() + "/" + bundleFile.getName())) {
                writer.write(
                        encoding.equals("json")
                                ? fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle).getBytes()
                                : fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle).getBytes()
                );
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Error outputting library bundle");
            }
        }
    }



}
