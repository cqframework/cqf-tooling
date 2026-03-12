package org.opencds.cqf.tooling.operations.bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Operation(name = "MakeTransaction")
public class BundleToTransaction implements ExecutableOperation {
    private static final Logger logger = LoggerFactory.getLogger(BundleToTransaction.class);

    private static final String DEFAULT_OUTPUT_PATH =
            "src/main/resources/org/opencds/cqf/tooling/bundle/output";

    @OperationParam(
            alias = {"e", "encoding"},
            setter = "setEncoding",
            defaultValue = "json",
            description = "Output encoding { json, xml } (default json)")
    private String encoding;

    @OperationParam(
            alias = {"p", "path"},
            setter = "setPath",
            required = true,
            description = "Path to a Bundle file or directory of Bundles (required)")
    private String path;

    @OperationParam(
            alias = {"op", "outputpath"},
            setter = "setOutputPath",
            description = "Output directory for the converted transaction Bundles")
    private String outputPath;

    @OperationParam(
            alias = {"v", "version"},
            setter = "setVersion",
            defaultValue = "r4",
            description = "FHIR version { dstu2, stu3, r4 } (default r4)")
    private String version;

    @Override
    public void execute() {
        if (path == null) {
            throw new IllegalArgumentException("The path to a Bundle or directory of resources is required");
        }

        if (outputPath == null) {
            outputPath = DEFAULT_OUTPUT_PATH;
        }

        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        if (encoding == null) {
            encoding = "json";
        }

        FhirContext context = FhirContextCache.getContext(version);

        File file = new File(path);
        File[] bundles;
        if (file.isDirectory()) {
            bundles = file.listFiles();
        } else {
            bundles = new File[] {file};
        }

        List<IBaseResource> resources = getResources(bundles, context);
        convertToTransaction(resources, context);
    }

    private void convertToTransaction(List<IBaseResource> resources, FhirContext context) {
        if (context.getVersion().getVersion() == FhirVersionEnum.DSTU3) {
            for (IBaseResource resource : resources) {
                if (resource instanceof org.hl7.fhir.dstu3.model.Bundle) {
                    org.hl7.fhir.dstu3.model.Bundle bundle = (org.hl7.fhir.dstu3.model.Bundle) resource;
                    for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                        if (entry.getResource() != null) {
                            entry.setRequest(new org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent()
                                    .setUrl(entry.getResource().getId())
                                    .setMethod(org.hl7.fhir.dstu3.model.Bundle.HTTPVerb.PUT));
                        }
                    }
                    bundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);
                    writeResource(bundle, context);
                }
            }
        } else if (context.getVersion().getVersion() == FhirVersionEnum.R4) {
            for (IBaseResource resource : resources) {
                if (resource instanceof org.hl7.fhir.r4.model.Bundle) {
                    org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) resource;
                    for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                        if (entry.getResource() != null) {
                            if (entry.getResource().getResourceType().compareTo(ResourceType.ValueSet) == 0) {
                                var valueSet = (ValueSet) entry.getResource();
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
                                                newCompose.addInclude(
                                                        new ValueSet.ConceptSetComponent().setValueSet(newInclude));
                                            }
                                        }
                                    }
                                }
                                valueSet.setCompose(newCompose);
                            }

                            entry.setRequest(new org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent()
                                    .setUrl(entry.getResource().getResourceType() + "/"
                                            + entry.getResource().getIdPart())
                                    .setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.PUT));
                        }
                    }
                    bundle.setType(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION);
                    writeResource(bundle, context);
                }
            }
        }
    }

    private List<IBaseResource> getResources(File[] files, FhirContext context) {
        List<IBaseResource> resources = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                resources.addAll(getResources(file.listFiles(), context));
                continue;
            }

            IBaseResource resource = null;
            if (file.getPath().endsWith(".xml")) {
                try {
                    resource = context.newXmlParser().parseResource(new FileReader(file));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e.getMessage(), e);
                } catch (Exception e) {
                    logger.debug("Skipping non-parseable file: {}", file.getName());
                    continue;
                }
            } else if (file.getPath().endsWith(".json")) {
                try {
                    resource = context.newJsonParser().parseResource(new FileReader(file));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e.getMessage(), e);
                } catch (Exception e) {
                    logger.debug("Skipping non-parseable file: {}", file.getName());
                    continue;
                }
            } else {
                continue;
            }
            resources.add(resource);
        }
        return resources;
    }

    private void writeResource(IBaseResource resource, FhirContext context) {
        String fileName = resource.getIdElement().getResourceType() + "-"
                + resource.getIdElement().getIdPart() + "." + encoding;
        File outputFile = new File(outputPath, fileName);
        try (FileOutputStream writer = new FileOutputStream(outputFile)) {
            String encoded = encoding.equals("json")
                    ? context.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource)
                    : context.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
            writer.write(encoded.getBytes());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
