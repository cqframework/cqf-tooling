package org.opencds.cqf.tooling.parameter;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.tooling.processor.IProcessorContext;

public class TestIGParameters {
    /*
    The ig ini file
    */
    public String ini;

    /*
    The root directory of the IG
    */
    public String rootDir;

    /*
    The path to the Implementation Guide resource file
    */
    public String igPath;

//    /*
//    The canonical base URL of the ig
//    */
//    public String igCanonicalBase;

    /*
    The fhirContext for the current process
    */
    public FhirContext fhirContext;

    /*
    An initialized processor context that can provide the IG context directly
    */
    public IProcessorContext parentContext;

    /*
    Path to the directory containing the test cases to be executed (e.g., <IG root>/input/tests)
    */
    public String testCasesPath;

    /*
    Execution engine (i.e., CQF Ruler) to load the test content to and run the evaluation on.
    */
    public String fhirServerUri;
}
