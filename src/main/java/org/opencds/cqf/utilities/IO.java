package org.opencds.cqf.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IAnyResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class IO 
{    
    public static byte[] parseResource(IAnyResource resource, String encoding, FhirContext fhirContext) 
    {
        IParser parser = getParser(encoding, fhirContext);      
        return parser.setPrettyPrint(true).encodeResourceToString(resource).getBytes();
    }

    public static void outputResource(IAnyResource resource, String fileName, String outputPath, String encoding, FhirContext fhirContext) 
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

    public static void outputResources(Map<String, IAnyResource> resources, String outputPath, String encoding, FhirContext fhirContext) 
    {        
        for (Map.Entry<String, IAnyResource> set : resources.entrySet())
        {
            outputResource(set.getValue(), set.getKey(), outputPath, encoding, fhirContext);
        }
    }

    public static IAnyResource inputResource(String inputPath, FhirContext fhirContext) 
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

    public static String getEncoding(String path)
    {
        return path.substring(path.lastIndexOf("."));
    }

    private static IParser getParser(String encoding, FhirContext fhirContext) 
    {
        IParser parser;
        if (encoding == "xml") 
        {
            parser = fhirContext.newXmlParser();
        }
        else if (encoding == "json")
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
