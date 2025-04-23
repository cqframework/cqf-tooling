package org.opencds.cqf.tooling.plandefinition;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.hl7.fhir.r5.model.PlanDefinition;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.parameter.RefreshPlanDefinitionParameters;
import org.opencds.cqf.tooling.processor.BaseProcessor;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.utilities.CanonicalUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PlanDefinitionProcessor extends BaseProcessor {

    protected List<Object> identifiers;
    private final LibraryProcessor libraryProcessor;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PlanDefinitionProcessor(LibraryProcessor libraryProcessor) {
        this.libraryProcessor = libraryProcessor;
    }

    public List<String> refreshIgPlanDefinitionContent(BaseProcessor parentContext, Encoding outputEncoding,
                                                       Boolean versioned, FhirContext fhirContext,
                                                       String planDefinitionToRefreshPath,
                                                       Boolean shouldApplySoftwareSystemStamp) {
        return refreshIgPlanDefinitionContent(parentContext, outputEncoding, null, versioned,
                fhirContext, planDefinitionToRefreshPath, shouldApplySoftwareSystemStamp);
    }

    public List<String> refreshIgPlanDefinitionContent(BaseProcessor parentContext, Encoding outputEncoding,
                                                       String planDefinitionOutputDirectory, Boolean versioned,
                                                       FhirContext fhirContext, String planDefinitionToRefreshPath,
                                                       Boolean shouldApplySoftwareSystemStamp) {
        logger.info("Refreshing PlanDefinitions...");

        PlanDefinitionProcessor planDefinitionProcessor;
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                planDefinitionProcessor = new org.opencds.cqf.tooling.plandefinition.stu3.PlanDefinitionProcessor(
                        libraryProcessor);
                break;
            case R4:
                planDefinitionProcessor = new org.opencds.cqf.tooling.plandefinition.r4.PlanDefinitionProcessor(
                        libraryProcessor);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        var planDefinitionPath = FilenameUtils.concat(
                parentContext.getRootDir(), IGProcessor.PLAN_DEFINITION_PATH_ELEMENT);
        var params = new RefreshPlanDefinitionParameters();
        params.planDefinitionPath = planDefinitionPath;
        params.parentContext = parentContext;
        params.fhirContext = fhirContext;
        params.encoding = outputEncoding;
        params.versioned = versioned;
        params.planDefinitionOutputDirectory = planDefinitionOutputDirectory;
        var contentList = planDefinitionProcessor.refreshPlanDefinitionContent(params);

        if (!planDefinitionProcessor.getIdentifiers().isEmpty()) {
            this.getIdentifiers().addAll(planDefinitionProcessor.getIdentifiers());
        }
        return contentList;
    }

    protected List<Object> getIdentifiers() {
        if (identifiers == null) {
            identifiers = new ArrayList<>();
        }
        return identifiers;
    }

    protected boolean versioned;
    protected FhirContext fhirContext;

    public List<String> refreshPlanDefinitionContent(RefreshPlanDefinitionParameters params) {
        return new ArrayList<>();
    }

    protected List<PlanDefinition> refreshGeneratedContent(List<PlanDefinition> sourcePlanDefinitions) {
        return internalRefreshGeneratedContent(sourcePlanDefinitions);
    }

    private List<PlanDefinition> internalRefreshGeneratedContent(List<PlanDefinition> sourcePlanDefinitions) {
        // for each PlanDefinition, refresh the PlanDefinition based on the primary PlanDefinition library
        var resources = new ArrayList<PlanDefinition>();
        for (var planDefinition : sourcePlanDefinitions) {
            resources.add(refreshGeneratedContent(planDefinition));
        }
        return resources;
    }

    private PlanDefinition refreshGeneratedContent(PlanDefinition planDefinition) {
        var processor = new PlanDefinitionRefreshProcessor();
        var libraryManager = getCqlProcessor().getLibraryManager();
        var cqlCompilerOptions = getCqlProcessor().getCqlTranslatorOptions().getCqlCompilerOptions();
        // Do not attempt to refresh if the PlanDefinition does not have a library
        if (planDefinition.hasLibrary()) {
            var libraryUrl = ResourceUtils.getPrimaryLibraryUrl(planDefinition, fhirContext);
            var primaryLibraryIdentifier = CanonicalUtils.toVersionedIdentifier(libraryUrl);
            var errors = new ArrayList<CqlCompilerException>();
            var compiledLibrary = libraryManager.resolveLibrary(primaryLibraryIdentifier, errors);
            var hasErrors = false;
            if (!errors.isEmpty()) {
                for (var e : errors) {
                    if (e.getSeverity() == CqlCompilerException.ErrorSeverity.Error) {
                        hasErrors = true;
                    }
                    logger.warn(e.getMessage());
                }
            }
            if (!hasErrors) {
                return processor.refreshPlanDefinition(planDefinition, libraryManager, compiledLibrary, cqlCompilerOptions);
            }
        }
        return planDefinition;
    }

}
