package org.opencds.cqf.tooling.operations.stripcontent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;

abstract class BaseStripContent implements IStripContent {

    protected static final Set<String> STRIPPED_CONTENT_TYPES = new HashSet<>(
            Arrays.asList("application/elm+xml", "application/elm+json"));
    protected static final String CQL_CONTENT_TYPE = "text/cql";
    protected static final Set<String> STRIPPED_EXTENSION_URLS = new HashSet<>(
            Arrays.asList("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode"));

    protected abstract FhirContext context();

    protected void writeFile(String fileName, String content) {
        File f = new File(fileName);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        
        try (var writer = new BufferedWriter(new FileWriter(f))) {
            
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected IBaseResource parseResource(File file) {
        IBaseResource theResource = null;
        try {
            if (file.getName().endsWith(".json")) {
                theResource = context().newJsonParser().parseResource(new FileReader(file));
            } else if(file.getName().endsWith(".xml")){
                theResource = context().newXmlParser().parseResource(new FileReader(file));
            }

            if (theResource == null) {
                throw new RuntimeException(String.format("failed to parse resource for file: %s", file.toString()));
            }

            return theResource;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeFile(String fileName, IBaseResource resource) {
        String output = "";
        if (fileName.endsWith(".json")) {
            output = context().newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
        } else if (fileName.endsWith(".xml")) {
            output = context().newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
        }

        writeFile(fileName, output);
    }
}
