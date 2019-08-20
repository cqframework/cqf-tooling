package org.opencds.cqf.modelinfo.fhir;

import java.util.ArrayList;

import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.opencds.cqf.modelinfo.ModelInfoSettings;

public class FHIRModelInfoSettings extends ModelInfoSettings {

    public FHIRModelInfoSettings(String version) {
        super("FHIR", version, "http://hl7.org/fhir", "FHIR.Patient", "birthDate.value", "fhir");
        this.conversionInfos = new ArrayList<ConversionInfo>() {
            {
                add(new ConversionInfo().withFromType("Coding").withToType("System.Code").withFunctionName("ToCode"));
                add(new ConversionInfo().withFromType("CodeableConcept").withToType("System.Concept")
                    .withFunctionName("ToCode"));
                add(new ConversionInfo().withFromType("Quantity").withToType("System.Quantity")
                    .withFunctionName("ToQuantity"));
                add(new ConversionInfo().withFromType("Period").withToType("Interval<System.DateTime>")
                            .withFunctionName("ToInterval"));
                add(new ConversionInfo().withFromType("Range").withToType("Interval<System.Quantity>")
                        .withFunctionName("ToInterval"));
            }
        };
        
    }
}