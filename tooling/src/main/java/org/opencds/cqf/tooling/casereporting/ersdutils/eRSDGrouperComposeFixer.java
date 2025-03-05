package org.opencds.cqf.tooling.casereporting.ersdutils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.Operation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class eRSDGrouperComposeFixer extends Operation {

    private String encoding; // -encoding (-e)
    private String path; // -path (-p)
//    private String version; // -version (-v) Can be dstu2, stu3, or r4
//    private Boolean relativeRequestUrls; // -relativerequesturls (-rru_

//    private IBaseResource theResource;
//    private List<IBaseResource> theResources = new ArrayList<>();
    private FhirContext context;

    private List<String> grouperUrls;

    public eRSDGrouperComposeFixer() {
        context = FhirContext.forR4();

        grouperUrls = new ArrayList<>();
        grouperUrls.add("http://ersd.aimsplatform.org/fhir/ValueSet/dxtc");
        grouperUrls.add("http://ersd.aimsplatform.org/fhir/ValueSet/ostc");
        grouperUrls.add("http://ersd.aimsplatform.org/fhir/ValueSet/lotc");
        grouperUrls.add("http://ersd.aimsplatform.org/fhir/ValueSet/lrtc");
        grouperUrls.add("http://ersd.aimsplatform.org/fhir/ValueSet/mrtc");
        grouperUrls.add("http://ersd.aimsplatform.org/fhir/ValueSet/sdtc");
    }

    @Override
    public void execute(String[] args) {
        parseParameters(args);
        fixBundle(args);
    }

    private void parseParameters(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/casereporting/output"); // default

        for (String arg : args) {
            if (arg.equals("-eRSDFixGrouperCompose")) continue;
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
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (path == null) {
            throw new IllegalArgumentException("The path to an eRSD Bundle is required");
        }
    }

    public Bundle fixBundle(String[] params) {
        parseParameters(params);
        Bundle inputBundle = getAndValidateBundle(path);
        for (Bundle.BundleEntryComponent entry : inputBundle.getEntry()) {
            if (entry.getResource() != null) {
                if (entry.getResource().getResourceType().compareTo(ResourceType.ValueSet) == 0
                        && grouperUrls.contains(((ValueSet)entry.getResource()).getUrl())) {
                    var valueSet = (ValueSet)entry.getResource();
                    ValueSet.ValueSetComposeComponent compose = valueSet.getCompose();
                    ValueSet.ValueSetComposeComponent newCompose = new ValueSet.ValueSetComposeComponent();
                    List<ValueSet.ConceptSetComponent> concepts = compose.getInclude();
                    for (ValueSet.ConceptSetComponent concept : concepts) {
                        if (concept.hasValueSet()) {
                            List<CanonicalType> referencedValueSets = concept.getValueSet();
                            if (referencedValueSets.size() > 1) {

                                for (CanonicalType reference : referencedValueSets) {
                                    List<CanonicalType> newInclude = new ArrayList<>();
                                    newInclude.add(reference);
                                    newCompose.addInclude(new ValueSet.ConceptSetComponent().setValueSet(newInclude));
                                }
                            }
                        }
                    }
                    valueSet.setCompose(newCompose);
                }
            }
        }

        output(inputBundle, context);
        return inputBundle;
    }

    private Bundle getAndValidateBundle(String pathToBundle) {
        Bundle sourceBundle = null;
        File bundleFile = new File(pathToBundle);
        if (bundleFile.isFile()) {
            try {
                if (bundleFile.getName().endsWith("json")) {
                    sourceBundle = (Bundle)((JsonParser)context.newJsonParser()).parseResource(new FileInputStream(bundleFile));
                }
                else if (bundleFile.getName().endsWith("xml")) {
                    sourceBundle = (Bundle)((XmlParser)context.newXmlParser()).parseResource(new FileInputStream(bundleFile));
                }
                else {
                    throw new IllegalArgumentException("Unsupported input bundle encoding. Currently, only .json and .xml supported for the input bundle.");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error parsing " + bundleFile.getName());
            }
        }

        return sourceBundle;
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
