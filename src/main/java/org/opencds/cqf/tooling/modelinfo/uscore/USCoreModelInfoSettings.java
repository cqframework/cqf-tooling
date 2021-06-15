package org.opencds.cqf.tooling.modelinfo.uscore;

import org.opencds.cqf.tooling.modelinfo.ModelInfoSettings;
/**
 * @author Adam Stevenson
 */
public class USCoreModelInfoSettings extends ModelInfoSettings {

    public USCoreModelInfoSettings(String version) {
        super("USCore", version, "http://hl7.org/fhir/us/core", "PatientProfile", "birthDate", "uscore");
    }
}