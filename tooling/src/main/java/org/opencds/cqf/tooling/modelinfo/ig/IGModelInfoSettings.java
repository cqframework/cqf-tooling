package org.opencds.cqf.tooling.modelinfo.ig;

import org.opencds.cqf.tooling.modelinfo.ModelInfoSettings;

public class IGModelInfoSettings extends ModelInfoSettings {

    public IGModelInfoSettings(String name, String version, String url, String patientClassName,
                             String patientBirthDatePropertyName, String targetQualifier, String targetUrl) {
        super(name, version, url, patientClassName, patientBirthDatePropertyName, targetQualifier, targetUrl);
    }
}
