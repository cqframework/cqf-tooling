package org.opencds.cqf.library;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.INarrative;
import org.opencds.cqf.common.JarEnabledCustomThymeleafNarrativeGenerator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.narrative2.ThymeleafNarrativeGenerator;

public abstract class BaseNarrativeProvider<T extends INarrative> {
    private INarrativeGenerator generator;
    
    public INarrativeGenerator getGenerator() {
        return this.generator;
    }

    public BaseNarrativeProvider() {
        this(Thread.currentThread().getContextClassLoader().getResource("narratives/narrative.properties").toString());         
    }

    public BaseNarrativeProvider(String pathToPropertiesFile)
    {
        ThymeleafNarrativeGenerator myGenerator = new JarEnabledCustomThymeleafNarrativeGenerator("classpath:ca/uhn/fhir/narrative/narratives.properties", pathToPropertiesFile);
        this.generator = myGenerator;
    }

    public abstract T getNarrative(FhirContext context, IBaseResource resource);
}
