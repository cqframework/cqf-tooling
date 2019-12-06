package org.opencds.cqf.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class IOUtils 
{    
    public static byte[] parseResource(IAnyResource resource, String encoding, FhirContext fhirContext) 
    {
        IParser parser = getParser(encoding, fhirContext);      
        return parser.setPrettyPrint(true).encodeResourceToString(resource).getBytes();
    }

    public static <T extends IAnyResource> void writeResource(T resource, String baseFileName, String outputPath, String encoding, FhirContext fhirContext) 
    {        
        try (FileOutputStream writer = new FileOutputStream(FilenameUtils.concat(outputPath, baseFileName + "." + encoding)))
        {
            writer.write(parseResource(resource, encoding, fhirContext));
            writer.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error writing Resource to file: " + e.getMessage());
        }
    }

    public static <T extends IAnyResource> void writeResources(Map<String, T> resources, String outputPath, String encoding, FhirContext fhirContext)
    {        
        for (Map.Entry<String, T> set : resources.entrySet())
        {
            writeResource(set.getValue(), set.getValue().getId(), outputPath, encoding, fhirContext);
        }
    }

    public static IAnyResource readResource(String inputPath, FhirContext fhirContext) 
    {
        IAnyResource resource;       
        try
        {
            IParser parser = getParser(getEncoding(inputPath), fhirContext);
            resource = (IAnyResource)parser.parseResource(new FileReader(new File(inputPath.toString())));
        }
        catch (FileNotFoundException fnfe)
        {
            throw new RuntimeException("Error reading file: " + inputPath);
        }
        return resource;
    }

    public static List<IAnyResource> readResources(List<String> inputPaths, FhirContext fhirContext) 
    {
        List<IAnyResource> resources = new ArrayList<>();
        for (String inputPath : inputPaths)
        {
            resources.add(readResource(inputPath, fhirContext));
        }
        return resources;
    }

    public static List<IAnyResource> readResourcesFromDir(String inputDirectoryPath, FhirContext fhirContext, boolean recursive) {
        List<IAnyResource> resources = new ArrayList<>();
        if (!recursive) {
            File inputDir = new File(inputDirectoryPath);
            ArrayList<File> files = new ArrayList<>(Arrays.asList(Optional.ofNullable(inputDir.listFiles()).orElseThrow()));
            if (files.isEmpty()) return new ArrayList<>();
            try {
                files.forEach(file -> resources.add(readResource(file.getPath(), fhirContext)));
            } catch (Exception e) {
                System.out.println("error reading resource: ");
                System.out.println(e.getMessage());
            }

        }
        else {
            File inputDir = new File(inputDirectoryPath);
            ArrayList<File> files = new ArrayList<>(Arrays.asList(Optional.ofNullable(inputDir.listFiles()).orElseThrow()));
            if (files.isEmpty()) return new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    resources.addAll(readResourcesFromDir(file.getPath(), fhirContext, recursive));
                }
                else {
                    try {
                        resources.add(readResource(file.getPath(), fhirContext));
                    } catch (Exception e) {
                        System.out.println("error reading resource: ");
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        return resources;
    }

    public static ArrayList<File> getFilesFromDir(String pathToDir) {
        File dir = new File(pathToDir);
        if(!dir.isDirectory()) {
            throw new IllegalArgumentException("The path to the tests Directory is not a Directory");
        }
        return new ArrayList<>(Arrays.asList(Optional.ofNullable(dir.listFiles()).orElseThrow()));
    }

    public static String getEncoding(String path)
    {
        return FilenameUtils.getExtension(path);
    }

    private static IParser getParser(String encoding, FhirContext fhirContext) 
    {
        IParser parser;
        if (encoding.equals("xml")) 
        {
            parser = fhirContext.newXmlParser();
        }
        else if (encoding.equals("json"))
        {
            parser = fhirContext.newJsonParser();
        }
        else
        {
            throw new RuntimeException("Unknown encoding type: " + encoding);
        }
        return parser;
    }

    public static Boolean pathIncludesElement(String igPath, String pathElement)
    {
        Boolean result = false;
        try
        {
            result = FilenameUtils.directoryContains(igPath, pathElement);
        }
        catch (Exception e) {}
        return result;
    }
}
