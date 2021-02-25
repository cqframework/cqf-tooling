package org.opencds.cqf.individual_tooling.cql_generation;

import java.net.URI;

/**
 * Provides an interface for generating Elm from a given Data Source encoding / uri
 * and modelVersion **only Fhir as of now**
 * @author  Joshua Reynolds
 * @since   2021-02-24 
 */
public interface CqlGenerator {
    /**
     * 
     * @param inputPath path to the input Data Source File
     * @param fhirVersion fhir Version of elm to output
     */
    public void generate(String inputPath, String fhirVersion);
    /**
     * 
     * @param inputURI URI of the input Data Source File
     * @param fhirVersion fhir Version of elm to output
     */
    public void generate(URI inputURI, String fhirVersion);
}
