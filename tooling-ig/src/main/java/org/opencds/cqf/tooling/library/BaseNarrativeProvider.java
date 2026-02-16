package org.opencds.cqf.tooling.library;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.INarrative;
import org.opencds.cqf.tooling.common.JarEnabledCustomThymeleafNarrativeGenerator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.INarrativeGenerator;

import java.util.Objects;

public abstract class BaseNarrativeProvider<T extends INarrative> {
    private final INarrativeGenerator generator;
    
    public INarrativeGenerator getGenerator() {
        return this.generator;
    }

    public BaseNarrativeProvider() {
        this(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("narratives/narrative.properties")).toString());
    }

    public BaseNarrativeProvider(String pathToPropertiesFile) {
        this.generator = new JarEnabledCustomThymeleafNarrativeGenerator("classpath:ca/uhn/fhir/narrative/narratives.properties", pathToPropertiesFile);
    }

    public abstract T getNarrative(FhirContext context, IBaseResource resource);
}
