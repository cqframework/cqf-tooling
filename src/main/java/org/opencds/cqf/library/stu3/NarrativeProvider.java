package org.opencds.cqf.library.stu3;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.library.BaseNarrativeProvider;

public class NarrativeProvider extends BaseNarrativeProvider<Narrative> {

    public NarrativeProvider() {
        super(NarrativeProvider.class.getClassLoader().getResource("narratives/stu3/narrative.properties").toString());
    }

    public Narrative getNarrative(FhirContext context, IBaseResource resource) {
        Narrative narrative = new Narrative();
        this.getGenerator().generateNarrative(context, resource, narrative);
        return narrative;
    }
}