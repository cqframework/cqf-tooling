package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.common.ThreadUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

public class BundleToResources extends Operation {

    private String encoding; // -encoding (-e)
    private String path; // -path (-p)
    private String version; // -version (-v) Can be dstu2, stu3, or r4
    private FhirContext context;


    private final List<Callable<Void>> outputTasks = new CopyOnWriteArrayList<>();
    private final List<Callable<Void>> discoverBundleTasks = new CopyOnWriteArrayList<>();
    private final List<StringBuilder> outputReportList = new CopyOnWriteArrayList<>();
    private final List<File> bundleFiles = new CopyOnWriteArrayList<>();

    private int totalBundleCount = 0;
    private int processedBundleCount = 0;

    private void increaseBundleDiscoveredProgress() {
        totalBundleCount++;
        System.out.print("\rBundles discovered: " + totalBundleCount);
    }

    private void reportProgress() {
        processedBundleCount++;
        double percentage = (double) processedBundleCount / totalBundleCount * 100;
        System.out.print("\r" + String.format("%.2f%%", percentage) + " processed.");
    }

    @Override
    public void execute(String[] args) {

        String outputPath = null;

        boolean deleteBundles = false;

        for (String arg : args) {
            if (arg.equals("-BundleToResources")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];


            switch (flag.replace("-", "").toLowerCase()) {
                case "encoding":
                case "e":
                    encoding = value.toLowerCase();
                    break;
                case "outputpath":
                case "op":
                    value = value.replace("/", File.separator).replace("\\", File.separator);
                    outputPath = value;
                    break; // -outputpath (-op)
                case "path":
                case "p":
                    value = value.replace("/", File.separator).replace("\\", File.separator);

                    File pathFile = new File(value);
                    if (!pathFile.exists()) {
                        throw new RuntimeException("path set to invalid location: " + value);
                    }

                    path = pathFile.getAbsolutePath();
                    break;
                case "version":
                case "v":
                    version = value;
                    break;
                case "db":
                    if (value.equalsIgnoreCase("true")) {
                        deleteBundles = true;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (path == null) {
            throw new IllegalArgumentException("The path to a Bundle or directory of resources is required");
        }

        File file = new File(path);
        File[] bundles = null;
        if (file.isDirectory()) {
            bundles = file.listFiles();
        } else {
            bundles = new File[]{file};
        }

        if (encoding == null) {
            encoding = "json";
        }

        if (version == null) {
            context = FhirContext.forR4Cached();
        } else {
            switch (version.toLowerCase()) {
                case "dstu2":
                    context = FhirContext.forDstu2Cached();
                    break;
                case "stu3":
                    context = FhirContext.forDstu3Cached();
                    break;
                case "r4":
                    context = FhirContext.forR4Cached();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown fhir version: " + version);
            }
        }


        StringBuilder deleteBundlesLog = new StringBuilder();
        if (bundles != null) {
            if (outputPath == null) {
                outputPath = "src/main/resources/org/opencds/cqf/tooling/bundle/output";

            }
            setOutputPath(outputPath);
            String outputPathLocation = new File(outputPath).getAbsolutePath();

            discoverBundles(bundles, outputPathLocation);


            if (!outputTasks.isEmpty()) {
                System.out.println("\n\rExtracting resources from bundles...");

                //outputTasks has been built up by discoverBundles
                ThreadUtils.executeTasks(outputTasks);


                if (deleteBundles) {

                    for (File bundleFile : bundleFiles) {
                        if (deleteBundlesLog.length() == 0) {
                            deleteBundlesLog.append("\n\rResult of deleting bundles (-db=true): ");
                        }
                        try {
                            if (bundleFile.delete()) {
                                deleteBundlesLog.append("\n\rDeleted: ")
                                        .append(bundleFile.getAbsolutePath());
                            } else {
                                deleteBundlesLog.append("\n\rFailed to delete: ")
                                        .append(bundleFile.getAbsolutePath());
                            }
                        } catch (SecurityException se) {
                            deleteBundlesLog.append("\n\rPermission denied to delete: ")
                                    .append(bundleFile.getAbsolutePath()).append("\n\r")
                                    .append(se.getMessage());
                        } catch (Exception e) {
                            deleteBundlesLog.append("\n\rError occurred while deleting: ")
                                    .append(bundleFile.getAbsolutePath()).append("\n\r")
                                    .append(e.getMessage());
                        }
                    }
                }
            } else {
                System.out.println("\n\rNo files to extract.");
            }
        }


        //Organize and report final status:
        outputReportList.sort((sb1, sb2) -> {
            String fileLocation1 = getFileLocation(sb1);
            String fileLocation2 = getFileLocation(sb2);
            return fileLocation1.compareTo(fileLocation2);
        });

        // Print the sorted list
        StringBuilder outputReportSB = new StringBuilder();
        for (StringBuilder sb : outputReportList) {
            outputReportSB.append(sb);
        }

        if (deleteBundlesLog.length() > 0) {
            outputReportSB.append("\n\r").append(deleteBundlesLog);
        }

        System.out.println("\n\r" + outputReportSB);
        System.out.println("\n\rProcess complete.");

        // TODO: add DSTU2
    }

    private static String getFileLocation(StringBuilder sb) {
        String[] parts = sb.toString().split(": ");
        return parts.length > 1 ? parts[1] : "";
    }

    public void discoverBundles(File[] resources, String outputPathLocation) {
        List<Pair<IBaseResource, File>> bundleResourceList = new CopyOnWriteArrayList<>();
        discoverBundlesRecursively(resources, outputPathLocation, bundleResourceList);
        ThreadUtils.executeTasks(discoverBundleTasks);
    }

    private void discoverBundlesRecursively(File[] resources, String outputPathLocation, List<Pair<IBaseResource, File>> bundleResourceList) {
        for (File resourceFile : resources) {
            if (resourceFile.getAbsolutePath().equals(outputPathLocation)) {
                continue;
            }

            if (resourceFile.isDirectory()) {
                File[] nestedFiles = resourceFile.listFiles();
                if (nestedFiles != null) {
                    //File is actually a directory, recursively call this method and add all files in THAT
                    //folder to the discoverBundleTasks list.
                    discoverBundlesRecursively(nestedFiles, outputPathLocation, bundleResourceList);
                }
                continue;
            }

            // Skip unsupported files
            if (!resourceFile.getAbsolutePath().endsWith(".json") && !resourceFile.getAbsolutePath().endsWith(".xml")) {
                continue;
            }

            // Submit file processing as a task
            discoverBundleTasks.add(() -> {
                try (FileReader reader = new FileReader(resourceFile)) {
                    IBaseResource parsedResource;
                    if (resourceFile.getPath().endsWith(".xml")) {
                        parsedResource = context.newXmlParser().parseResource(reader);
                    } else if (resourceFile.getPath().endsWith(".json")) {
                        parsedResource = context.newJsonParser().parseResource(reader);
                    } else {
                        parsedResource = null;
                    }

                    if (parsedResource instanceof Bundle || parsedResource instanceof org.hl7.fhir.r4.model.Bundle) {
                        increaseBundleDiscoveredProgress();
                        outputTasks.add(() -> {
                            outputFiles(parsedResource, resourceFile);
                            return null;
                        });
                    }
                } catch (Exception e) {
                    return null;
                }
                return null;
            });
        }
    }


    private void outputFiles(IBaseResource bundleResource, File bundleResourceFile) {

        List<IBaseResource> listOfResources = new ArrayList<>();

        if (context.getVersion().getVersion() == FhirVersionEnum.DSTU3) {
            // foreach resource, if it's a bundle, output all the resources it contains
            Bundle bundle = (Bundle) bundleResource;
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() != null) {
                    listOfResources.add(entry.getResource());
                }
            }
        } else if (context.getVersion().getVersion() == FhirVersionEnum.R4) {
            org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) bundleResource;
            for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() != null) {
                    listOfResources.add(entry.getResource());
                }
            }
        }

        if (!listOfResources.isEmpty()) {
            String directoryName = bundleResourceFile.getAbsolutePath().replace(path, "").replace(bundleResourceFile.getName(), "");
            int extractionCount = 0;
            for (IBaseResource thisResource : listOfResources) {
                if (output(thisResource, context, directoryName) != null) {
                    extractionCount++;
                }
            }
            //give user information on resources extracted
            synchronized (outputReportList) {
                String extractionCountStr = "" + extractionCount;

                //try to format to the thousandth
                if (extractionCountStr.length() == 1) {
                    extractionCountStr = "   " + extractionCountStr;
                } else if (extractionCountStr.length() == 2) {
                    extractionCountStr = "  " + extractionCountStr;
                } else if (extractionCountStr.length() == 3) {
                    extractionCountStr = " " + extractionCountStr;
                }

                outputReportList.add(new StringBuilder("\n\r").append(extractionCountStr)
                        .append(" resources extracted from: ")
                        .append(bundleResourceFile.getAbsolutePath().replace(path, "")));
                bundleFiles.add(bundleResourceFile);
                reportProgress();
            }

        }
    }

    public String output(IBaseResource resource, FhirContext context, String folderName) {
        String outputPath = getOutputPath();
        File outputDirectory = new File(outputPath, folderName);
        if (!outputDirectory.exists()) {
            try {
                outputDirectory.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        String resourceType = resource.getIdElement().getResourceType();
        String resourceId = resource.getIdElement().getIdPart();
        String fileName = String.format("%s-%s.%s", resourceType, resourceId, encoding);
        File outputFile = new File(outputDirectory, fileName);

        try (FileOutputStream writer = new FileOutputStream(outputFile)) {
            String encodedResource = encoding.equals("json")
                    ? context.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource)
                    : context.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);

            writer.write(encodedResource.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        return outputFile.getAbsolutePath();
    }

}
