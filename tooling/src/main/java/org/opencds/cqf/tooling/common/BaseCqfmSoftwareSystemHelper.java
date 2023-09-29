package org.opencds.cqf.tooling.common;

import org.apache.commons.io.FilenameUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public abstract class BaseCqfmSoftwareSystemHelper {
    private String rootDir;
    protected String getRootDir() {
        return this.rootDir;
    }

    public static String cqfRulerDeviceName =  "cqf-ruler";
    private static String cqfToolingDeviceName = "cqf-tooling";
//    private static String cqfToolingDeviceReferenceID = "#" + cqfToolingDeviceName;
    private static String cqfmSoftwareSystemExtensionUrl = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem";
    protected static String devicePath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(File.separator,
            "input"), "resources"), "device");

    protected String getCqfToolingDeviceName() { return cqfToolingDeviceName; }
//    protected String getCqfToolingDeviceReferenceID() { return cqfToolingDeviceReferenceID; }
    protected String getCqfmSoftwareSystemExtensionUrl() { return cqfmSoftwareSystemExtensionUrl; }

    public BaseCqfmSoftwareSystemHelper() { }

    public BaseCqfmSoftwareSystemHelper(String rootDir) {
        this.rootDir = Objects.requireNonNull(rootDir, "CqfmSoftwareSystemHelper rootDir argument can not be null");
    }

    protected Boolean getSystemIsValid(CqfmSoftwareSystem system) {
        Boolean isValid = false;

        if (system != null) {
            Boolean hasSoftwareSystemName = false;
            if (system.getName() != null && !system.getName().isEmpty()) {
                hasSoftwareSystemName = true;
            }

            Boolean hasVersion = false;
            if (system.getVersion() != null && !system.getVersion().isEmpty()) {
                hasVersion = true;
            }

            isValid = hasSoftwareSystemName && hasVersion;
        }

        return isValid;
    }

    protected void EnsureDevicePath() {
        IOUtils.ensurePath(rootDir + devicePath);
        if (!IOUtils.resourceDirectories.contains(rootDir + devicePath)) {
            IOUtils.resourceDirectories.add(rootDir + devicePath);
        }
    }
}
