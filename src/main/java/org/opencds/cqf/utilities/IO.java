package org.opencds.cqf.utilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class IO {    
    public static byte[] parseResource(IBaseResource resource, String encoding, FhirContext fhirContext) {
        IParser jsonParser = fhirContext.newJsonParser();
        IParser xmlParser = fhirContext.newXmlParser();

        return  encoding.equals("json")
            ? jsonParser.setPrettyPrint(true).encodeResourceToString(resource).getBytes()
                : xmlParser.setPrettyPrint(true).encodeResourceToString(resource).getBytes();
    }

    public static void outputResources(Map<String, IBaseResource> resources, String outputPath, String encoding, FhirContext fhirContext) {        
        for (Map.Entry<String, IBaseResource> set : resources.entrySet())
        {
            try (FileOutputStream writer = new FileOutputStream(outputPath + "/" + set.getKey() + "." + encoding))
            {
                writer.write(parseResource(set.getValue(), encoding, fhirContext));
                writer.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                throw new RuntimeException("Error writing Resource Bundle to file: " + e.getMessage());
            }
        }
    }
}
