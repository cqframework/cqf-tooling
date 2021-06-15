package org.opencds.cqf.tooling.cql_generation;

import java.net.URI;
import java.util.Map;

import org.hl7.elm.r1.Library;

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
     * @param outputPath path to the output Directory
     * @param fhirVersion fhir Version of elm to output
     */
    public void generateAndWriteToFile(String inputPath, String outputPath, String fhirVersion);/**
    * 
    * @param inputPath path to the input Data Source File
    * @param fhirVersion fhir Version of elm to output
    */
   public Map<String, Library> generate(String inputPath, String fhirVersion);
    /**
     * 
     * @param inputURI URI of the input Data Source File
     * @param outputURI URI of the the output Directory
     * @param fhirVersion fhir Version of elm to output
     */
    public void generateAndWriteToFile(URI inputURI, URI outputURI, String fhirVersion);/**
    * 
    * @param inputURI URI of the input Data Source File
    * @param fhirVersion fhir Version of elm to output
    */
   public Map<String, Library> generate(URI inputURI, String fhirVersion);
}
