package org.opencds.cqf.tooling.modelinfo.qicore;

import org.opencds.cqf.tooling.modelinfo.ModelInfoSettings;

public class QICoreModelInfoSettings extends ModelInfoSettings {

    public QICoreModelInfoSettings(String version) {
        super("QICore", version, "http://hl7.org/fhir/us/qicore", "Patient", "birthDate", "qicore", "http://hl7.org/fhir");
    }
}