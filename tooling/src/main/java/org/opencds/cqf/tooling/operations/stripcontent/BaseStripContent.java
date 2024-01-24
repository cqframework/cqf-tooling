package org.opencds.cqf.tooling.operations.stripcontent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Attachment;
import org.hl7.fhir.r5.model.DomainResource;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.r5.model.Measure;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.PlanDefinition;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.RelatedArtifact;
import org.hl7.fhir.r5.model.Resource;

import ca.uhn.fhir.context.FhirContext;

abstract class BaseStripContent<T extends IBaseResource> implements IStripContent {
    protected static final Set<String> STRIPPED_CONTENT_TYPES = new HashSet<>(
            Arrays.asList("application/elm+xml", "application/elm+json"));
    protected static final String CQL_CONTENT_TYPE = "text/cql";
    protected static final Set<String> STRIPPED_EXTENSION_URLS = new HashSet<>(
            Arrays.asList("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode",
                    "http://hl7.org/fhir/StructureDefinition/cqf-cqlOptions"));

    protected abstract FhirContext context();

    protected abstract Resource convertToR5(T resource);
    protected abstract T convertFromR5(Resource resource);

    public void stripFile(File inputFile, File outputFile) {
        var resource = parseResource(inputFile);
        var upgraded = convertToR5((T)resource);
        stripResource(upgraded, outputFile);
        var downgraded = convertFromR5(upgraded);
        writeFile(outputFile, downgraded);
    }

    protected void writeFile(File f, String content) {
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

    protected void writeFile(File f, IBaseResource resource) {
        String output = "";
        if (f.getName().endsWith(".json")) {
            output = context().newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
        } else if (f.getName().endsWith(".xml")) {
            output = context().newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
        }

        writeFile(f, output);
    }

    private Resource stripResource(IBaseResource resource, File outputFile) {
        switch (resource.fhirType()) {
            case "Library":
                return stripLibrary((Library) resource, outputFile);
            case "Measure":
                return stripMeasure((Measure) resource);
            case "PlanDefinition":
                return stripPlanDefinition((PlanDefinition) resource);
            case "Questionnaire":
                return stripQuestionnaire((Questionnaire) resource);
            default:
                return stripResource((DomainResource) resource);
        }
    }

    private boolean isCqlOptionsParameters(Resource resource) {
        if (!(resource instanceof Parameters)) {
            return false;
        }

        var parameters = (Parameters) resource;
        return "options".equals(parameters.getId());
    }

    private List<Resource> filterContained(List<Resource> contained) {
        return contained.stream()
                .filter(this::isCqlOptionsParameters)
                .collect(Collectors.toList());
    }

    private List<Extension> filterExtensions(List<Extension> extensions) {
        return extensions.stream()
                .filter(extension -> !STRIPPED_EXTENSION_URLS.contains(extension.getUrl()))
                .collect(Collectors.toList());
    }

    private List<Attachment> filterContent(List<Attachment> attachments) {
        return attachments.stream()
                .filter(attachment -> !STRIPPED_CONTENT_TYPES.contains(attachment.getContentType()))
                .collect(Collectors.toList());
    }

    private List<RelatedArtifact> filterRelatedArtifacts(List<RelatedArtifact> artifacts) {
        return artifacts
                .stream()
                .filter(x -> !RelatedArtifact.RelatedArtifactType.DEPENDSON.equals(x.getType()))
                .collect(Collectors.toList());
    }

    // Strip library includes functionality to export the cql file,
    // so it requires knowledge of the target directory for the Library.
    private Library stripLibrary(Library library, File outputFile) {
        library.setText(null);
        library.setParameter(null);
        library.setDataRequirement(null);
        library.setContained(filterContained(library.getContained()));
        library.setRelatedArtifact(filterRelatedArtifacts(library.getRelatedArtifact()));
        library.setExtension(filterExtensions(library.getExtension()));
        library.setContent(filterContent(library.getContent()));
        exportCql(library.getContent(), outputFile);
        return library;
    }

    private Measure stripMeasure(Measure measure) {
        measure.setText(null);
        measure.setRelatedArtifact(filterRelatedArtifacts(measure.getRelatedArtifact()));
        measure.setExtension(filterExtensions(measure.getExtension()));
        return measure;
    }

    private PlanDefinition stripPlanDefinition(PlanDefinition planDefinition) {
        planDefinition.setText(null);
        planDefinition.setRelatedArtifact(filterRelatedArtifacts(planDefinition.getRelatedArtifact()));
        planDefinition.setExtension(filterExtensions(planDefinition.getExtension()));
        return planDefinition;
    }

    private Questionnaire stripQuestionnaire(Questionnaire questionnaire) {
        questionnaire.setText(null);
        questionnaire.setExtension(filterExtensions(questionnaire.getExtension()));
        return questionnaire;
    }

    private DomainResource stripResource(DomainResource resource) {
        resource.setText(null);
        return resource;
    }

    private void exportCql(Attachment content, File libraryOutputLocation) {
        if (content.getData() == null) {
            return;
        }

        var base64 = content.getDataElement().getValueAsString();
        var cql = new String(java.util.Base64.getDecoder().decode(base64));
        var cqlFileName = libraryOutputLocation.getName();
        if (cqlFileName.toLowerCase().startsWith("library-")) {
            cqlFileName = cqlFileName.substring(8);
        }

        cqlFileName = cqlFileName.substring(0, cqlFileName.lastIndexOf('.')) + ".cql";
        var cqlFile = libraryOutputLocation.toPath().getParent().resolve(cqlFileName).toFile();

        content.setUrl(libraryOutputLocation.toPath().relativize(cqlFile.toPath()).toString());
        content.setDataElement(null);
        writeFile(cqlFile, cql);


    }

    private void exportCql(List<Attachment> content, File libraryOutputFile) {
        for (Attachment attachment : content) {
            if (CQL_CONTENT_TYPE.equals(attachment.getContentType())) {
               exportCql(attachment, libraryOutputFile);
            }
        }
    }
}
