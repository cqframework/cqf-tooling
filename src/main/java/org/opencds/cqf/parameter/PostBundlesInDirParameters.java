package org.opencds.cqf.parameter;

import org.opencds.cqf.processor.PostBundlesInDirProcessor.FHIRVersion;
import org.opencds.cqf.utilities.IOUtils;

public class PostBundlesInDirParameters {  
    public String directoryPath;
    public String fhirUri;
    public FHIRVersion fhirVersion;
    public IOUtils.Encoding encoding;
}