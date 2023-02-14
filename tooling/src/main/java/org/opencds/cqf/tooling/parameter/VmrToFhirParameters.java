package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
/**
 * @author Joshua Reynolds
 */
public class VmrToFhirParameters {
    public String vmrDataPath;
    public String fhirOutputPath;
    public Encoding encoding;
    public String fhirVersion;
}
