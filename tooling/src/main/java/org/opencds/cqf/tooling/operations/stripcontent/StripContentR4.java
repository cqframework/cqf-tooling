package org.opencds.cqf.tooling.operations.stripcontent;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;

class StripContentR4 extends BaseStripContent {
    @Override
    protected FhirContext context() {
        return FhirContext.forR4Cached();
    }

    @Override
    public void stripFile(File inputFile, File outputFile) {
        stripResource(parseResource(inputFile), outputFile);
    }

    private void stripResource(IBaseResource resource, File file) {
        var fileName = file.getAbsolutePath();
        switch (resource.fhirType()) {
            case "Library":
                stripLibraryAndWrite(fileName, (Library) resource);
                break;
            case "Measure":
                stripMeasureAndWrite(fileName, (Measure) resource);
                break;
            case "PlanDefinition":
                stripPlanDefinitionAndWrite(fileName, (PlanDefinition) resource);
                break;
            case "Questionnaire":
                stripQuestionnaireAndWrite(fileName, (Questionnaire) resource);
                break;
            default:
                stripResourceAndWrite(fileName, (DomainResource) resource);
                break;
        }
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

    private void stripLibraryAndWrite(String fileName, Library library) {
        library.setText(null);
        library.setParameter(null);
        library.setDataRequirement(null);
        library.setRelatedArtifact(filterRelatedArtifacts(library.getRelatedArtifact()));
        library.setExtension(filterExtensions(library.getExtension()));
        library.setContent(filterContent(library.getContent()));
        exportCql(library.getContent(), fileName);
        writeFile(fileName, library);

    }

    private void stripMeasureAndWrite(String fileName, Measure measure) {
        measure.setText(null);
        measure.setRelatedArtifact(filterRelatedArtifacts(measure.getRelatedArtifact()));
        measure.setExtension(filterExtensions(measure.getExtension()));
        writeFile(fileName, measure);
    }

    private void stripPlanDefinitionAndWrite(String fileName, PlanDefinition planDefinition) {
        planDefinition.setText(null);
        planDefinition.setRelatedArtifact(filterRelatedArtifacts(planDefinition.getRelatedArtifact()));
        planDefinition.setExtension(filterExtensions(planDefinition.getExtension()));
        writeFile(fileName, planDefinition);
    }

    private void stripQuestionnaireAndWrite(String fileName, Questionnaire questionnaire) {
        questionnaire.setText(null);
        questionnaire.setExtension(filterExtensions(questionnaire.getExtension()));
        writeFile(fileName, questionnaire);
    }

    private void stripResourceAndWrite(String fileName, DomainResource resource) {
        resource.setText(null);
        writeFile(fileName, resource);
    }

    private void exportCql(Attachment content, String fileName) {
        if (content.getData() == null) {
            return;
        }

        var base64 = content.getDataElement().getValueAsString();
        var cql = new String(java.util.Base64.getDecoder().decode(base64));
        var cqlFileName = fileName.replace(".json", ".cql");
        writeFile(cqlFileName, cql);
        content.setUrl(cqlFileName);
    }

    private void exportCql(List<Attachment> content, String fileName) {
        for (Attachment attachment : content) {
            if (CQL_CONTENT_TYPE.equals(attachment.getContentType())) {
               exportCql(attachment, fileName);
            }
        }
    }
}
