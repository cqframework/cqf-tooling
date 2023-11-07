package org.opencds.cqf.tooling.questionnaire;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.common.ThreadUtils;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.processor.*;
import org.opencds.cqf.tooling.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class QuestionnaireProcessor extends AbstractResourceProcessor{

    public QuestionnaireProcessor(LibraryProcessor libraryProcessor) {
        setLibraryProcessor(libraryProcessor);
    }

    //abstract methods to override:
    @Override
    protected String getSourcePath(FhirContext fhirContext, Map.Entry<String, IBaseResource> resourceEntry) {
        return IOUtils.getQuestionnairePathMap(fhirContext).get(resourceEntry.getKey());
    }

    @Override
    protected Map<String, IBaseResource> getResources(FhirContext fhirContext) {
        return IOUtils.getQuestionnaires(fhirContext);
    }

    @Override
    protected String getResourceProcessorType() {
        return TYPE_QUESTIONNAIRE;
    }

    @Override
    protected Set<String> getPaths(FhirContext fhirContext) {
        return IOUtils.getQuestionnairePaths(fhirContext);
    }
}