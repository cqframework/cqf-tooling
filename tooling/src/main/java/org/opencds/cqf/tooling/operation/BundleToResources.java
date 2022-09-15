package org.opencds.cqf.tooling.operation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

public class BundleToResources extends Operation {

    private String encoding; // -encoding (-e)
    private String path; // -path (-p)
    private String version; // -version (-v) Can be dstu2, stu3, or r4

    private IBaseResource theResource;
    private List<IBaseResource> theResources = new ArrayList<>();
    private FhirContext context;

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/bundle/output"); // default

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
                    setOutputPath(value);
                    break; // -outputpath (-op)
                case "path":
                case "p":
                    path = value;
                    break;
                case "version": case "v":
                    version = value;
                    break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (path == null) {
            throw new IllegalArgumentException("The path to a Bundle or directory of resources is required");
        }

        File file = new File(path);
        File[] bundles = null;
        if (file.isDirectory()) {
            bundles = file.listFiles();
        }
        else {
            bundles = new File[] { file };
        }

        if (encoding == null) {
            encoding = "json";
        }

        if (version == null) {
            context = FhirContext.forR4Cached();
        }
        else {
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
        
        getResources(bundles);

        if (context.getVersion().getVersion() == FhirVersionEnum.DSTU3) {
            // foreach resource, if it's a bundle, output all the resources it contains
            for (IBaseResource resource : theResources) {
                if (resource instanceof org.hl7.fhir.dstu3.model.Bundle) {
                    org.hl7.fhir.dstu3.model.Bundle bundle = (org.hl7.fhir.dstu3.model.Bundle)resource;
                    for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                        if (entry.getResource() != null) {
                            output(entry.getResource(), context);
                        }
                    }
                }
            }
        }
        else if (context.getVersion().getVersion() == FhirVersionEnum.R4) {
            for (IBaseResource resource : theResources) {
                if (resource instanceof org.hl7.fhir.r4.model.Bundle) {
                    org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle)resource;
                    for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                        if (entry.getResource() != null) {
                            output(entry.getResource(), context);
                        }
                    }
                }
            }
        }

        // TODO: add DSTU2
    }

    private void getResources(File[] resources) {
        for (File resource : resources) {

            if(resource.isDirectory()) {
                getResources(resource.listFiles());
                continue;
            }

            if (resource.getPath().endsWith(".xml")) {
                try {
                    theResource = context.newXmlParser().parseResource(new FileReader(resource));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
                catch (Exception e) {
                    continue;
                }
            }
            else if (resource.getPath().endsWith(".json")) {
                try {
                    theResource = context.newJsonParser().parseResource(new FileReader(resource));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
                catch (Exception e) {
                    continue;
                }
            }
            else {
                continue;
            }
            theResources.add(theResource);
        }
    }
    
    // Output
    public void output(IBaseResource resource, FhirContext context) {
        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + resource.getIdElement().getResourceType() + "-" + resource.getIdElement().getIdPart() + "." + encoding)) {
            writer.write(
                encoding.equals("json")
                    ? context.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
                    : context.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
            );
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
