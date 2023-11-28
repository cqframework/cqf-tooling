package org.opencds.cqf.tooling.operation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.opencds.cqf.tooling.Operation;

import ca.uhn.fhir.context.FhirContext;

public class StripGeneratedContentOperation extends Operation {

    private String pathToResource;
    private FhirContext context;
    String version;

    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals("-StripGeneratedContent")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1].replace("\"", ""); // Strip quotes

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break;
                case "pathtores":
                case "ptr":
                    pathToResource = value;
                    break;
                case "version": case "v":
                    version = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
        this.context = contextForVersion(version);
        File res  = validateDirectory(pathToResource);
        var files = getListOfActionableFiles(res);

        for(File file : files) {
            parseAndStripResource(file);
        }

    }

    private File validateDirectory(String pathToDir) {
        if (pathToDir == null) {
            throw new IllegalArgumentException("The path to the directory is required");
        }

        File bundleDirectory = new File(pathToDir);
        if (!bundleDirectory.isDirectory()) {
            throw new IllegalArgumentException("The path supplied is not a directory");
        }
        return bundleDirectory;
    }

    private Collection<File> getListOfActionableFiles(File file) {
        return FileUtils.listFiles(file, new String[] { "json", "xml"}, true);
    }

    private FhirContext contextForVersion(String version) {
        if (StringUtils.isEmpty(version)) {
            return FhirContext.forR4Cached();
        } else {
            switch (version.toLowerCase()) {
                case "dstu3":
                    return FhirContext.forDstu3Cached();
                case "r4":
                    return FhirContext.forR4Cached();
                case "r5":
                    return  FhirContext.forR5Cached();
                default:
                    throw new IllegalArgumentException("Unknown fhir version: " + version);
            }
        }
    }

    private void parseAndStripResource(File file) {
        IBaseResource theResource = null;
        try {
            if (file.getName().endsWith(".json")) {
                theResource = context.newJsonParser().parseResource(new FileReader(file));
            } else if(file.getName().endsWith(".xml")){
                theResource = context.newXmlParser().parseResource(new FileReader(file));
            }

            if (theResource == null) {
                throw new RuntimeException(String.format("failed to parse resource for file: %s", file.toString()));
            }

            stripResource(file.getPath().substring(pathToResource.length()), theResource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            String message = String.format("'%s' will not be included in the resource because the following error occurred: '%s'", file.getName(), e.getMessage());
            System.out.println(message);
        }
    }

    private void stripResource(String fileName, IBaseResource resource) {
        switch(context.getVersion().getVersion().name().toLowerCase()) {
            case "dstu3":
                if(resource.getIdElement().getResourceType().equals("Library")) {
                    org.hl7.fhir.dstu3.model.Library library = (org.hl7.fhir.dstu3.model.Library) resource;
                    stripLibraryAndWrite(fileName, library);
                } else if (resource.getIdElement().getResourceType().equals("Measure")) {
                    org.hl7.fhir.dstu3.model.Measure measure = (org.hl7.fhir.dstu3.model.Measure) resource;
                    stripMeasureAndWrite(fileName, measure);
                } else if (resource.getIdElement().getResourceType().equals("PlanDefinition")) {
                    org.hl7.fhir.dstu3.model.PlanDefinition planDefinition = (org.hl7.fhir.dstu3.model.PlanDefinition) resource;
                    stripPlanDefinitionAndWrite(fileName, planDefinition);
                } else if (resource.getIdElement().getResourceType().equals("Questionnaire")) {
                    org.hl7.fhir.dstu3.model.Questionnaire questionnaire = (org.hl7.fhir.dstu3.model.Questionnaire) resource;
                    stripQuestionnaireAndWrite(fileName, questionnaire);
                } else if (resource instanceof org.hl7.fhir.dstu3.model.DomainResource) {
                    stripResourceAndWrite(fileName, (org.hl7.fhir.dstu3.model.DomainResource)resource);
                }
                break;
            case "r4":
                if(resource.getIdElement().getResourceType().equals("Library")) {
                    Library library = (Library) resource;
                    stripLibraryAndWrite(fileName, library);
                } else if (resource.getIdElement().getResourceType().equals("Measure")) {
                    Measure measure = (Measure) resource;
                    stripMeasureAndWrite(fileName, measure);
                } else if (resource.getIdElement().getResourceType().equals("PlanDefinition")) {
                    PlanDefinition planDefinition = (PlanDefinition) resource;
                    stripPlanDefinitionAndWrite(fileName, planDefinition);
                } else if (resource.getIdElement().getResourceType().equals("Questionnaire")) {
                    Questionnaire questionnaire = (Questionnaire) resource;
                    stripQuestionnaireAndWrite(fileName, questionnaire);
                } else if (resource instanceof org.hl7.fhir.r4.model.DomainResource) {
                    stripResourceAndWrite(fileName, (org.hl7.fhir.r4.model.DomainResource)resource);
                }
                break;
            case "r5":
                if(resource.getIdElement().getResourceType().equals("Library")) {
                    org.hl7.fhir.r5.model.Library library = (org.hl7.fhir.r5.model.Library) resource;
                    stripLibraryAndWrite(fileName, library);
                } else if (resource.getIdElement().getResourceType().equals("Measure")) {
                    org.hl7.fhir.r5.model.Measure measure = (org.hl7.fhir.r5.model.Measure) resource;
                    stripMeasureAndWrite(fileName, measure);
                } else if (resource.getIdElement().getResourceType().equals("PlanDefinition")) {
                    org.hl7.fhir.r5.model.PlanDefinition planDefinition = (org.hl7.fhir.r5.model.PlanDefinition) resource;
                    stripPlanDefinitionAndWrite(fileName, planDefinition);
                } else if (resource.getIdElement().getResourceType().equals("Questionnaire")) {
                    org.hl7.fhir.r5.model.Questionnaire questionnaire = (org.hl7.fhir.r5.model.Questionnaire) resource;
                    stripQuestionnaireAndWrite(fileName, questionnaire);
                } else if (resource instanceof org.hl7.fhir.r5.model.DomainResource) {
                    stripResourceAndWrite(fileName, (org.hl7.fhir.r5.model.DomainResource)resource);
                }
                break;

        }

    }

    private void stripResourceAndWrite(String fileName, org.hl7.fhir.dstu3.model.DomainResource resource) {
        resource.setText(null);
        writeFile(fileName, resource);
    }

    private void stripResourceAndWrite(String fileName, org.hl7.fhir.r4.model.DomainResource resource) {
        resource.setText(null);
        writeFile(fileName, resource);
    }

    private void stripResourceAndWrite(String fileName, org.hl7.fhir.r5.model.DomainResource resource) {
        resource.setText(null);
        writeFile(fileName, resource);
    }

    private void stripLibraryAndWrite(String fileName, Library library) {
        if(library.hasText()) {
            library.setText(new Narrative());
        }
        if(library.hasParameter()) {
            library.setParameter(Collections.emptyList());
        }
        if(library.hasDataRequirement()) {
            library.setDataRequirement(Collections.emptyList());
        }
        if (library.hasRelatedArtifact()) {
            List<RelatedArtifact> list = library.getRelatedArtifact()
                    .stream()
                    .filter(relatedArtifact -> (relatedArtifact.hasType() &&
                            relatedArtifact.getType() != RelatedArtifact.RelatedArtifactType.DEPENDSON))
                    .collect(Collectors.toList());
            library.setRelatedArtifact(list);
        }

        if(library.hasExtension()) {
            List<Extension> list = library.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            library.setExtension(list);
        }
        if(library.hasContent()) {
            List<Attachment> attachments =
                    library.getContent()
                            .stream()
                            .filter(attachment ->
                                    !(attachment.getContentType().equalsIgnoreCase("application/elm+xml"))
                                            && !(attachment.getContentType().equalsIgnoreCase("application/elm+json"))
                            && !(attachment.getContentType().equalsIgnoreCase("text/cql") &&
                                            (StringUtils.isEmpty(attachment.getUrl()) && attachment.getData() != null)))
                            .collect(Collectors.toList());
            library.setContent(attachments);
        }
        writeFile(fileName, library);

    }

    private void stripMeasureAndWrite(String fileName, Measure measure) {
        if(measure.hasText()) {
            measure.setText(new Narrative());
        }
        if(measure.hasExtension()) {
            List<Extension> list = measure.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            measure.setExtension(list);
        }

        if(measure.hasRelatedArtifact()) {
            measure.setRelatedArtifact(Collections.emptyList());
        }
        writeFile(fileName, measure);
    }

    private void stripPlanDefinitionAndWrite(String fileName, PlanDefinition planDefinition) {
        if(planDefinition.hasText()) {
            planDefinition.setText(new Narrative());
        }

        if(planDefinition.hasRelatedArtifact()) {
            planDefinition.setRelatedArtifact(Collections.emptyList());
        }

        if(planDefinition.hasExtension()) {
            List<Extension> list = planDefinition.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            planDefinition.setExtension(list);
        }
        writeFile(fileName, planDefinition);
    }

    private void stripQuestionnaireAndWrite(String fileName, Questionnaire questionnaire) {
        if(questionnaire.hasText()) {
            questionnaire.setText(new Narrative());
        }


        if(questionnaire.hasExtension()) {
            List<Extension> list = questionnaire.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            questionnaire.setExtension(list);
        }
        writeFile(fileName, questionnaire);
    }

    private void stripLibraryAndWrite(String fileName, org.hl7.fhir.dstu3.model.Library library) {
        if(library.hasText()) {
            library.setText(new org.hl7.fhir.dstu3.model.Narrative());
        }
        if(library.hasParameter()) {
            library.setParameter(Collections.emptyList());
        }
        if(library.hasDataRequirement()) {
            library.setDataRequirement(Collections.emptyList());
        }
        if (library.hasRelatedArtifact()) {
            List<org.hl7.fhir.dstu3.model.RelatedArtifact> list = library.getRelatedArtifact()
                    .stream()
                    .filter(relatedArtifact -> (relatedArtifact.hasType() &&
                            !(relatedArtifact.getType()== org.hl7.fhir.dstu3.model.RelatedArtifact.RelatedArtifactType.DEPENDSON)))
                    .collect(Collectors.toList());
            library.setRelatedArtifact(list);
        }

        if(library.hasExtension()) {
            List<org.hl7.fhir.dstu3.model.Extension> list = library.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            library.setExtension(list);
        }
        if(library.hasContent()) {
            List<org.hl7.fhir.dstu3.model.Attachment> attachments =
                    library.getContent()
                            .stream()
                            .filter(attachment ->
                                    !(attachment.getContentType().equalsIgnoreCase("application/elm+xml"))
                                            && !(attachment.getContentType().equalsIgnoreCase("application/elm+json")))
                            .collect(Collectors.toList());
            library.setContent(attachments);
        }
        writeFile(fileName, library);

    }

    private void stripMeasureAndWrite(String fileName, org.hl7.fhir.dstu3.model.Measure measure) {
        if(measure.hasText()) {
            measure.setText(new org.hl7.fhir.dstu3.model.Narrative());
        }
        if(measure.hasExtension()) {
            List<org.hl7.fhir.dstu3.model.Extension> list = measure.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            measure.setExtension(list);
        }

        if(measure.hasRelatedArtifact()) {
            measure.setRelatedArtifact(Collections.emptyList());
        }
        writeFile(fileName, measure);
    }

    private void stripPlanDefinitionAndWrite(String fileName, org.hl7.fhir.dstu3.model.PlanDefinition planDefinition) {
        if(planDefinition.hasText()) {
            planDefinition.setText(new org.hl7.fhir.dstu3.model.Narrative());
        }

        if(planDefinition.hasRelatedArtifact()) {
            planDefinition.setRelatedArtifact(Collections.emptyList());
        }

        if(planDefinition.hasExtension()) {
            List<org.hl7.fhir.dstu3.model.Extension> list = planDefinition.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            planDefinition.setExtension(list);
        }
        writeFile(fileName, planDefinition);
    }

    private void stripQuestionnaireAndWrite(String fileName, org.hl7.fhir.dstu3.model.Questionnaire questionnaire) {
        if(questionnaire.hasText()) {
            questionnaire.setText(new org.hl7.fhir.dstu3.model.Narrative());
        }


        if(questionnaire.hasExtension()) {
            List<org.hl7.fhir.dstu3.model.Extension> list = questionnaire.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            questionnaire.setExtension(list);
        }
        writeFile(fileName, questionnaire);
    }

    private void stripLibraryAndWrite(String fileName, org.hl7.fhir.r5.model.Library library) {
        if(library.hasText()) {
            library.setText(new org.hl7.fhir.r5.model.Narrative());
        }
        if(library.hasParameter()) {
            library.setParameter(Collections.emptyList());
        }
        if(library.hasDataRequirement()) {
            library.setDataRequirement(Collections.emptyList());
        }
        if (library.hasRelatedArtifact()) {
            List<org.hl7.fhir.r5.model.RelatedArtifact> list = library.getRelatedArtifact()
                    .stream()
                    .filter(relatedArtifact -> (relatedArtifact.hasType() &&
                            !(relatedArtifact.getType() == org.hl7.fhir.r5.model.RelatedArtifact.RelatedArtifactType.DEPENDSON)))
                    .collect(Collectors.toList());
            library.setRelatedArtifact(list);
        }

        if(library.hasExtension()) {
            List<org.hl7.fhir.r5.model.Extension> list = library.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            library.setExtension(list);
        }
        if(library.hasContent()) {
            List<org.hl7.fhir.r5.model.Attachment> attachments =
                    library.getContent()
                            .stream()
                            .filter(attachment ->
                                    !(attachment.getContentType().equalsIgnoreCase("application/elm+xml"))
                                            && !(attachment.getContentType().equalsIgnoreCase("application/elm+json")))
                            .collect(Collectors.toList());
            library.setContent(attachments);
        }
        writeFile(fileName, library);

    }

    private void stripMeasureAndWrite(String fileName, org.hl7.fhir.r5.model.Measure measure) {
        if(measure.hasText()) {
            measure.setText(new org.hl7.fhir.r5.model.Narrative());
        }
        if(measure.hasExtension()) {
            List<org.hl7.fhir.r5.model.Extension> list = measure.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            measure.setExtension(list);
        }

        if(measure.hasRelatedArtifact()) {
            measure.setRelatedArtifact(Collections.emptyList());
        }
        writeFile(fileName, measure);
    }

    private void stripPlanDefinitionAndWrite(String fileName, org.hl7.fhir.r5.model.PlanDefinition planDefinition) {
        if(planDefinition.hasText()) {
            planDefinition.setText(new org.hl7.fhir.r5.model.Narrative());
        }

        if(planDefinition.hasRelatedArtifact()) {
            planDefinition.setRelatedArtifact(Collections.emptyList());
        }

        if(planDefinition.hasExtension()) {
            List<org.hl7.fhir.r5.model.Extension> list = planDefinition.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            planDefinition.setExtension(list);
        }
        writeFile(fileName, planDefinition);
    }

    private void stripQuestionnaireAndWrite(String fileName, org.hl7.fhir.r5.model.Questionnaire questionnaire) {
        if(questionnaire.hasText()) {
            questionnaire.setText(new org.hl7.fhir.r5.model.Narrative());
        }


        if(questionnaire.hasExtension()) {
            List<org.hl7.fhir.r5.model.Extension> list = questionnaire.getExtension()
                    .stream()
                    .filter(extension ->
                            !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"))
                                    && !(extension.hasUrl() && extension.getUrl().equals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")))
                    .collect(Collectors.toList());
            questionnaire.setExtension(list);
        }
        writeFile(fileName, questionnaire);
    }


    private void writeFile(String fileName, IBaseResource resource)  {
        String output = "";
        if (fileName.endsWith(".json")) {
            output = context.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
        } else if (fileName.endsWith(".xml")) {
            output = context.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
        }
        BufferedWriter writer;
        String outFileName = String.format("%s%s", getOutputPath(), fileName);
        try {
            File f = new File(outFileName);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            if (!f.exists()) {
                f.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(outFileName));
            writer.write(output);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
