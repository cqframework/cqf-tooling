package org.opencds.cqf.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static <T extends IAnyResource> void writeResource(T resource, String fileName, String outputPath, String encoding, FhirContext fhirContext) 
    {        
        try (FileOutputStream writer = new FileOutputStream(outputPath + "/" + fileName + "." + encoding))
        {
            writer.write(parseResource(resource, encoding, fhirContext));
            writer.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error writing Resource Bundle to file: " + e.getMessage());
        }
    }

    public static <T extends IAnyResource> void writeResources(Map<String, T> resources, String outputPath, String encoding, FhirContext fhirContext)
    {        
        for (Map.Entry<String, T> set : resources.entrySet())
        {
            writeResource(set.getValue(), set.getKey(), outputPath, encoding, fhirContext);
        }
    }

    public static IAnyResource readResource(String inputPath, FhirContext fhirContext) 
    {
        IAnyResource resource;       
        IParser parser = getParser(getEncoding(inputPath), fhirContext);
        try
        {            
            resource = (IAnyResource)parser.parseResource(new FileReader(new File(inputPath.toString())));
        }
        catch (FileNotFoundException fnfe)
        {
            throw new RuntimeException("Error reading file: " + inputPath.toString());
        }
        return resource;
    }

    public static List<IAnyResource> readResource(List<String> inputPaths, FhirContext fhirContext) 
    {
        List<IAnyResource> resources = new ArrayList<IAnyResource>();
        for (String inputPath : inputPaths)
        {
            resources.add(readResource(inputPath, fhirContext));
        }
        return resources;
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
            throw new RuntimeException("Unknown file type: " + encoding);
        }
        return parser;
    }
}
