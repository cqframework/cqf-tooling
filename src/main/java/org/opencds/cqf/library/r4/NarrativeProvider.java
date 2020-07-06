package org.opencds.cqf.library.r4;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Narrative;
import org.opencds.cqf.library.BaseNarrativeProvider;

import ca.uhn.fhir.context.FhirContext;

public class NarrativeProvider extends BaseNarrativeProvider<Narrative> {
    public NarrativeProvider() {
        super(Thread.currentThread().getContextClassLoader().getResource("narratives/r4/narrative.properties").toString()); 
    }

    public NarrativeProvider(String pathToPropertiesFile)
    {
        super(pathToPropertiesFile);
    }

    public Narrative getNarrative(FhirContext context, IBaseResource resource) {
        super.getGenerator().populateResourceNarrative(context, resource);
        return ((DomainResource) resource).getText();
    }
}
