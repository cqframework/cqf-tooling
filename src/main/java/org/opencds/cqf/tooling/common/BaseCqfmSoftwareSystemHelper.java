package org.opencds.cqf.tooling.common;

import org.apache.commons.io.FilenameUtils;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.LogUtils;

import java.io.IOException;
import java.util.Objects;

public abstract class BaseCqfmSoftwareSystemHelper {
    private String igPath;
    protected String getIgPath() {
        return this.igPath;
    }

    private static String cqfToolingDeviceName = "cqf-tooling";
//    private static String cqfToolingDeviceReferenceID = "#" + cqfToolingDeviceName;
    private static String cqfmSoftwareSystemExtensionUrl = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem";
    protected static String devicePath = "/input/resources/device";

    protected String getCqfToolingDeviceName() { return cqfToolingDeviceName; }
//    protected String getCqfToolingDeviceReferenceID() { return cqfToolingDeviceReferenceID; }
    protected String getCqfmSoftwareSystemExtensionUrl() { return cqfmSoftwareSystemExtensionUrl; }

    public BaseCqfmSoftwareSystemHelper() { }

    public BaseCqfmSoftwareSystemHelper(String igPath) {
        this.igPath = Objects.requireNonNull(igPath, "CqfmSoftwareSystemHelper igPath argument can not be null");
    }

    protected Boolean getSystemIsValid(CqfmSoftwareSystem system) {
        Boolean isValid = false;

        if (system != null) {
            String softwareSystemName = null;
            Boolean hasSoftwareSystemName = false;
            if (system.getName() != null && !system.getName().isEmpty()) {
                softwareSystemName = system.getName();
                hasSoftwareSystemName = true;
            }

            String version = null;
            Boolean hasVersion = false;
            if (system.getVersion() != null && !system.getVersion().isEmpty()) {
                version = system.getVersion();
                hasVersion = true;
            }

            isValid = hasSoftwareSystemName && hasVersion;
        }

        return isValid;
    }

    protected void EnsureDevicePath() {
        try {
            IOUtils.ensurePath(igPath + devicePath);
        }
        catch (IOException ex) {
            LogUtils.putException("EnsureDevicePath", ex.getMessage());
        }
    }
}
