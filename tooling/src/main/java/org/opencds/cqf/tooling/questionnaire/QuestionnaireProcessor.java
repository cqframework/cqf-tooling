package org.opencds.cqf.tooling.questionnaire;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.processor.AbstractResourceProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.Map;
import java.util.Set;

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

    @Override
    protected void persistTestFiles(String bundleDestPath, String libraryName, IOUtils.Encoding encoding, FhirContext fhirContext, String fhirUri) {
        //not needed
    }
}