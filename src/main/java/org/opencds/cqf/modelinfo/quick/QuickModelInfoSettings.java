package org.opencds.cqf.modelinfo.quick;

import java.util.ArrayList;

import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.opencds.cqf.modelinfo.ModelInfoSettings;

public class QuickModelInfoSettings extends ModelInfoSettings {

    public QuickModelInfoSettings(String version) {
        super("QUICK", version, "http://hl7.org/fhir/us/qicore", "QUICK.Patient", "birthDate", "quick");
        //super("QUICK", version, "http://hl7.org/fhir", "QUICK.Patient", "birthDate", "quick");
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