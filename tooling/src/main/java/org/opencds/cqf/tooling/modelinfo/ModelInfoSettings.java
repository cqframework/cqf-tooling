package org.opencds.cqf.tooling.modelinfo;

import java.util.Collection;

import org.hl7.elm_modelinfo.r1.ConversionInfo;

public class ModelInfoSettings {

    public String name;
    public String version;
    public String url;
    public String patientClassName;
    public String patientBirthDatePropertyName;
    public String targetQualifier;
    public String targetUrl;
    //public Map<String, String> primarySearchPath = new HashMap<String, String>();

    public Collection<ConversionInfo> conversionInfos;

    public ModelInfoSettings(String name, String version, String url, String patientClassName,
            String patientBirthDatePropertyName, String targetQualifier, String targetUrl) {
        this.name = name;
        this.version = version;
        this.url = url;
        this.patientClassName = patientClassName;
        this.patientBirthDatePropertyName = patientBirthDatePropertyName;
        this.targetQualifier = targetQualifier;
        this.targetUrl = targetUrl;
    }
}
