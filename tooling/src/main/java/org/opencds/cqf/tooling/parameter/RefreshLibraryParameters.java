package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.tooling.processor.IProcessorContext;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class RefreshLibraryParameters {
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
    Whether or not version is included in the name
     */
    public Boolean versioned;

    /*
    Whether the Software System Stamp should be applied via the crmi-softwaresystem Extension
    */
    public Boolean shouldApplySoftwareSystemStamp;

    /*
    The path to the library resource(s)
     */
    public String libraryPath;

    /*
    An initialized processor context that can provide the IG context directly
     */
    public IProcessorContext parentContext;

    /*
    Path to write updated Libraries to
    */
    public String libraryOutputDirectory;
}
