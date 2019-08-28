package org.opencds.cqf.library;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class STU3NarrativeProvider extends BaseNarrativeProvider<Narrative> {

    public STU3NarrativeProvider() {
        super(STU3NarrativeProvider.class.getClassLoader().getResource("narratives/stu3/narrative.properties").toString());
    }

    public Narrative getNarrative(FhirContext context, IBaseResource resource) {
        Narrative narrative = new Narrative();
        this.generator.generateNarrative(context, resource, narrative);
        return narrative;
    }
}