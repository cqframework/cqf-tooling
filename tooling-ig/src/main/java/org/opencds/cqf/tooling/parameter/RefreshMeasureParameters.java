package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.tooling.processor.IProcessorContext;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class RefreshMeasureParameters {
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
    public Encoding encoding;

    /*
    Whether the version is included in the name
     */
    public Boolean versioned;

    /*
    The path to the measure resource(s)
     */
    public String measurePath;

    /*
    An initialized processor context that can provide the IG context directly
     */
    public IProcessorContext parentContext;

    /*
    Directory target for writing output
     */
    public String measureOutputDirectory;

    public Boolean verboseMessaging;

    public Boolean shouldApplySoftwareSystemStamp;

    public Boolean includePopulationDataRequirements;
}
