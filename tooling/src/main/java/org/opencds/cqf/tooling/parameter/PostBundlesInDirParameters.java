package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.tooling.processor.PostBundlesInDirProcessor.FHIRVersion;
import org.opencds.cqf.tooling.utilities.IOUtils;

public class PostBundlesInDirParameters {  
    public String directoryPath;
    public String fhirUri;
    public FHIRVersion fhirVersion;
    public IOUtils.Encoding encoding;
}