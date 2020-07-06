package org.opencds.cqf.modelinfo.uscore;

import java.util.ArrayList;

import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.opencds.cqf.modelinfo.ModelInfoSettings;

public class USCoreModelInfoSettings extends ModelInfoSettings {

    public USCoreModelInfoSettings(String version) {
        super("USCore", version, "http://hl7.org/fhir/us/core", "PatientProfile", "birthDate", "uscore");
    }
}