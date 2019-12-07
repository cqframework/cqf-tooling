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
    public enum Encoding 
    { 
        JSON("json"), XML("xml"); 
  
        private String string; 
    
        public String toString() 
        { 
            return this.string; 
        } 
    
        private Encoding(String string) 
        { 
            this.string = string; 
        }

        public static Encoding parse(String value) {
            switch (value) {
                case "json": 
                    return JSON;
                case "xml":
                    return XML;
                default: 
                    throw new RuntimeException("Unable to parse Encoding value:" + value);
            }
        }
    } 

    public static byte[] parseResource(IAnyResource resource, Encoding encoding, FhirContext fhirContext) 
    {
        IParser parser = getParser(encoding, fhirContext);      
        return parser.setPrettyPrint(true).encodeResourceToString(resource).getBytes();
    }

    public static <T extends IAnyResource> void writeResource(T resource, String outputPath, String baseFileName, Encoding encoding, FhirContext fhirContext) 
    {        
        try (FileOutputStream writer = new FileOutputStream(FilenameUtils.concat(outputPath, baseFileName + "." + encoding.toString())))
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

    public static <T extends IAnyResource> void writeResources(Map<String, T> resources, String outputPath, Encoding encoding, FhirContext fhirContext)
    {        
        for (Map.Entry<String, T> set : resources.entrySet())
        {
            writeResource(set.getValue(), outputPath, set.getValue().getId(), encoding, fhirContext);
        }
    }

    //There's a special operation to write a bundle because I can't find a type that will reference both dstu3 and r4.
    public static void writeBundle(Object bundle, String outputPath, String baseFileName, Encoding encoding, FhirContext fhirContext) {
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                writeResource(((org.hl7.fhir.dstu3.model.Bundle)bundle), outputPath, baseFileName, encoding, fhirContext);
            case R4:
                writeResource(((org.hl7.fhir.r4.model.Bundle)bundle), outputPath, baseFileName, encoding, fhirContext);
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    public static IAnyResource readResource(String path, FhirContext fhirContext) 
    {
        IAnyResource resource;       
        try
        {
            IParser parser = getParser(getEncoding(path), fhirContext);
            resource = (IAnyResource)parser.parseResource(new FileReader(new File(path)));
        }
        catch (FileNotFoundException fnfe)
        {
            throw new RuntimeException("Error reading file: " + path);
        }
        return resource;
    }

    public static List<IAnyResource> readResources(List<String> paths, FhirContext fhirContext) 
    {
        List<IAnyResource> resources = new ArrayList<>();
        for (String path : paths)
        {
            resources.add(readResource(path, fhirContext));
        }
        return resources;
    }

    public static List<String> getFilePaths(String directoryPath, Boolean recursive)
    {
        List<String> filePaths = new ArrayList<String>();
        File inputDir = new File(directoryPath);
        ArrayList<File> files = new ArrayList<>(Arrays.asList(Optional.ofNullable(inputDir.listFiles()).orElseThrow()));
       
        for (File file : files) {
            if (file.isDirectory()  && recursive) {
                filePaths.addAll(getFilePaths(file.getPath(), recursive));
            }
            else {
               filePaths.add(file.getPath());
            }
        }
        return filePaths;
    }

    public static List<String> getDirectoryPaths(String path, Boolean recursive)
    {
        List<String> directoryPaths = new ArrayList<String>();
        File parentDirectory = new File(path);
        ArrayList<File> directories = new ArrayList<>(Arrays.asList(Optional.ofNullable(parentDirectory.listFiles()).orElseThrow()));
       
        for (File directory : directories) {
            if (directory.isDirectory()  && recursive) {
                directoryPaths.addAll(getDirectoryPaths(directory.getPath(), recursive));
                directoryPaths.add(directory.getPath());
            }
        }
        return directoryPaths;
    }

    public static Encoding getEncoding(String path)
    {
        return Encoding.parse(FilenameUtils.getExtension(path));
    }

    private static IParser getParser(Encoding encoding, FhirContext fhirContext) 
    {
        switch (encoding) {
            case XML: 
                return fhirContext.newXmlParser();
            case JSON:
                return fhirContext.newJsonParser();
            default: 
                throw new RuntimeException("Unknown encoding type: " + encoding.toString());
        }
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
