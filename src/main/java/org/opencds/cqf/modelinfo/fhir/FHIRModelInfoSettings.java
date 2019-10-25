package org.opencds.cqf.modelinfo.fhir;

import java.util.ArrayList;

import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.opencds.cqf.modelinfo.ModelInfoSettings;

public class FHIRModelInfoSettings extends ModelInfoSettings {

    public FHIRModelInfoSettings(String version) {
        super("FHIR", version, "http://hl7.org/fhir", "FHIR.Patient", "birthDate.value", "fhir");
        this.conversionInfos = new ArrayList<ConversionInfo>() {
            {
                add(new ConversionInfo().withFromType("FHIR.Coding").withToType("System.Code").withFunctionName("FHIRHelpers.ToCode"));
                add(new ConversionInfo().withFromType("FHIR.CodeableConcept").withToType("System.Concept")
                    .withFunctionName("FHIRHelpers.ToCode"));
                add(new ConversionInfo().withFromType("FHIR.Quantity").withToType("System.Quantity")
                    .withFunctionName("FHIRHelpers.ToQuantity"));
                add(new ConversionInfo().withFromType("FHIR.SimpleQuantity").withToType("System.Quantity")
                        .withFunctionName("FHIRHelpers.ToQuantity"));
                add(new ConversionInfo().withFromType("FHIR.Age").withToType("System.Quantity")
                        .withFunctionName("FHIRHelpers.ToQuantity"));
                add(new ConversionInfo().withFromType("FHIR.Distance").withToType("System.Quantity")
                        .withFunctionName("FHIRHelpers.ToQuantity"));
                add(new ConversionInfo().withFromType("FHIR.Duration").withToType("System.Quantity")
                        .withFunctionName("FHIRHelpers.ToQuantity"));
                add(new ConversionInfo().withFromType("FHIR.Count").withToType("System.Quantity")
                        .withFunctionName("FHIRHelpers.ToQuantity"));
                add(new ConversionInfo().withFromType("FHIR.MoneyQuantity").withToType("System.Quantity")
                        .withFunctionName("FHIRHelpers.ToQuantity"));
                add(new ConversionInfo().withFromType("FHIR.Period").withToType("Interval<System.DateTime>")
                            .withFunctionName("FHIRHelpers.ToInterval"));
                add(new ConversionInfo().withFromType("FHIR.Range").withToType("Interval<System.Quantity>")
                        .withFunctionName("FHIRHelpers.ToInterval"));
            }
        };
        
    }
}