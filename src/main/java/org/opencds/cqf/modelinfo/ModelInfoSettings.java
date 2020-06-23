package org.opencds.cqf.modelinfo;

import java.util.Collection;

import org.hl7.elm_modelinfo.r1.ConversionInfo;

public class ModelInfoSettings {

    public String name;
    public String version;
    public String url;
    public String patientClassName;
    public String patientBirthDatePropertyName;
    public String targetQualifier;

    public Collection<ConversionInfo> conversionInfos;

    public ModelInfoSettings(String name, String version, String url, String patientClassName,
            String patientBirthDatePropertyName, String targetQualifier) {
        this.name = name;
        this.version = version;
        this.url = url;
        this.patientClassName = patientClassName;
        this.patientBirthDatePropertyName = patientBirthDatePropertyName;
        this.targetQualifier = targetQualifier;
    }
}
