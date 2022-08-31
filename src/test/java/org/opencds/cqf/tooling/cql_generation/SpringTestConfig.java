package org.opencds.cqf.tooling.cql_generation;

import org.opencds.cqf.cql.evaluator.spring.EvaluatorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

@Import(EvaluatorConfiguration.class)
@Configuration
public class SpringTestConfig {
    
  // Needed for the evaluator configuration
  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forCached(FhirVersionEnum.R4);
  }
}
