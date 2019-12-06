package org.opencds.cqf.library.r4;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Narrative;
import org.opencds.cqf.library.BaseNarrativeProvider;
import org.opencds.cqf.library.stu3.NarrativeProvider;

public class NarrativeProvider extends BaseNarrativeProvider<Narrative> {

    public NarrativeProvider() {
        super(org.opencds.cqf.library.stu3.NarrativeProvider.class.getClassLoader().getResource("narratives/r4/narrative.properties").toString());
    }

    public Narrative getNarrative(FhirContext context, IBaseResource resource) {
        Narrative narrative = new Narrative();
        this.getGenerator().generateNarrative(context, resource, narrative);
        return narrative;
    }
}
