package org.opencds.cqf.library;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.INarrative;

public abstract class BaseNarrativeProvider<T extends INarrative> {

    INarrativeGenerator generator;

    public BaseNarrativeProvider(String pathToPropertiesFile) {
        CustomThymeleafNarrativeGenerator myGenerator = new CustomThymeleafNarrativeGenerator("classpath:ca/uhn/fhir/narrative/narratives.properties", pathToPropertiesFile);
        myGenerator.setIgnoreFailures(false);
        myGenerator.setIgnoreMissingTemplates(false);
        this.generator = myGenerator;
    }

    public abstract T getNarrative(FhirContext context, IBaseResource resource);
}
