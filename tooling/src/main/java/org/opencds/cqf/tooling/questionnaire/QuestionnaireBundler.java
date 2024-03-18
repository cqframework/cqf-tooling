package org.opencds.cqf.tooling.questionnaire;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.processor.AbstractBundler;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class QuestionnaireBundler extends AbstractBundler {

    @SuppressWarnings("this-escape")
    public QuestionnaireBundler(LibraryProcessor libraryProcessor) {
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
    protected String getResourceBundlerType() {
        return TYPE_QUESTIONNAIRE;
    }

    @Override
    protected int persistFilesFolder(String bundleDestPath, String libraryName, IOUtils.Encoding encoding, FhirContext fhirContext, String fhirUri) {
        //do nothing
        return 0;
    }

    @Override
    protected Set<String> getPaths(FhirContext fhirContext) {
        return IOUtils.getQuestionnairePaths(fhirContext);
    }

}