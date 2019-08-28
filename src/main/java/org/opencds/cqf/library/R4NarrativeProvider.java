package org.opencds.cqf.library;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Narrative;

public class R4NarrativeProvider extends BaseNarrativeProvider<Narrative> {

    public R4NarrativeProvider() {
        super(STU3NarrativeProvider.class.getClassLoader().getResource("narratives/r4/narrative.properties").toString());
    }

    public Narrative getNarrative(FhirContext context, IBaseResource resource) {
        Narrative narrative = new Narrative();
        this.generator.generateNarrative(context, resource, narrative);
        return narrative;
    }
}
