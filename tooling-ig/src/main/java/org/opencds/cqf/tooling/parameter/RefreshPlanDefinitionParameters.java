package org.opencds.cqf.tooling.parameter;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.tooling.processor.IProcessorContext;
import org.opencds.cqf.tooling.utilities.IOUtils;

public class RefreshPlanDefinitionParameters {
    /*
     The ig ini file
      */
    public String ini;

    /*
    The canonical base URL of the ig
     */
    public String igCanonicalBase;

    /*
    The path to CQL library content
     */
    public String cqlContentPath;

    /*
    The fhirContext for the current process
     */
    public FhirContext fhirContext;

    /*
    The target encoding for the output
     */
    public IOUtils.Encoding encoding;

    /*
    Whether or not version is included in the name
     */
    public Boolean versioned;

    /*
    The path to the measure resource(s)
     */
    public String planDefinitionPath;

    /*
    An initialized processor context that can provide the IG context directly
     */
    public IProcessorContext parentContext;

    /*
    Directory target for writing output
     */
    public String planDefinitionOutputDirectory;
}
