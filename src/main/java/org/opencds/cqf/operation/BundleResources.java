package org.opencds.cqf.operation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.Operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

public class BundleResources extends Operation {

    private String encoding; // -encoding (-e)
    private String pathToDirectory; // -pathtodir (-ptd)
    private String version; // -version (-v) Can be dstu2, stu3, or r4

    private IBaseResource theResource;
    private List<IBaseResource> theResources = new ArrayList<>();
    private FhirContext context;

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/bundle/output"); // default

        for (String arg : args) {
            if (arg.equals("-BundleResources")) continue;
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
                case "pathtodir":
                case "ptd":
                    pathToDirectory = value;
                    break;
                case "version": case "v":
                    version = value;
                    break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToDirectory == null) {
            throw new IllegalArgumentException("The path to the resource directory is required");
        }

        File resourceDirectory = new File(pathToDirectory);
        if (!resourceDirectory.isDirectory()) {
            throw new RuntimeException("The specified path to resource files is not a directory");
        }

        File[] resources = resourceDirectory.listFiles();
        if (resources == null) {
            throw new RuntimeException("The specified path to resource files is empty");
        }

        if (version == null) {
            context = FhirContext.forDstu3();
        }
        else {
            switch (version.toLowerCase()) {
                case "dstu2":
                    context = FhirContext.forDstu2();
                    break;
                case "stu3":
                    context = FhirContext.forDstu3();
                    break;
                case "r4":
                    context = FhirContext.forR4();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown fhir version: " + version);
            }
        }
        
        getResources(resources);

        if (context.getVersion().getVersion() == FhirVersionEnum.DSTU3) {
            org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
            bundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);
            for (IBaseResource resource : theResources) {
                bundle.addEntry(
                        new org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent()
                                .setResource((org.hl7.fhir.dstu3.model.Resource) resource)
                                .setRequest(
                                        new org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent()
                                                .setMethod(org.hl7.fhir.dstu3.model.Bundle.HTTPVerb.PUT)
                                                .setUrl(((org.hl7.fhir.dstu3.model.Resource) resource).getId())
                                )
                );
            }
            output(bundle, context);
        }
        else if (context.getVersion().getVersion() == FhirVersionEnum.R4) {
            org.hl7.fhir.r4.model.Bundle bundle = new org.hl7.fhir.r4.model.Bundle();
            bundle.setType(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION);
            for (IBaseResource resource : theResources) {
                bundle.addEntry(
                        new org.hl7.fhir.r4.model.Bundle.BundleEntryComponent()
                                .setResource((org.hl7.fhir.r4.model.Resource) resource)
                                .setRequest(
                                        new org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent()
                                                .setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.PUT)
                                                .setUrl(((org.hl7.fhir.r4.model.Resource) resource).getId())
                                )
                );
            }
            output(bundle, context);
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
        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + getOutputPath().substring(getOutputPath().lastIndexOf("/")) + "-bundle." + encoding)) {
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
