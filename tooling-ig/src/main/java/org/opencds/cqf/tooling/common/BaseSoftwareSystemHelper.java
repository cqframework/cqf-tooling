package org.opencds.cqf.tooling.common;

import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.File;
import java.util.Objects;


public abstract class BaseSoftwareSystemHelper {
    private String rootDir;
    protected String getRootDir() {
        return this.rootDir;
    }

    public static String cqfRulerDeviceName =  "cqf-ruler";
    protected static String devicePath = IOUtils.concatFilePath(File.separator,
            "input", "resources", "device");

    public BaseSoftwareSystemHelper() { }

    public BaseSoftwareSystemHelper(String rootDir) {
        this.rootDir = Objects.requireNonNull(rootDir, "SoftwareSystemHelper rootDir argument can not be null");
    }

    protected Boolean getSystemIsValid(SoftwareSystem system) {
        boolean isValid = false;

        if (system != null) {
            boolean hasSoftwareSystemName = system.getName() != null && !system.getName().isEmpty();
            boolean hasVersion = system.getVersion() != null && !system.getVersion().isEmpty();
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
