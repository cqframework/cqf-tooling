package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.PlanDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultipleChoiceElementChoices {
    private DictionaryFhirElementPath fhirElementPath;
    public DictionaryFhirElementPath getFhirElementPath() {
        return this.fhirElementPath;
    }
    public void setFhirElementPath(DictionaryFhirElementPath fhirElementPath) {
        this.fhirElementPath = fhirElementPath;
    }

    private Map<String, List<DictionaryCode>> valueSetCodes;
    public Map<String, List<DictionaryCode>> getValueSetCodes() {
        if (this.valueSetCodes == null) {
            this.valueSetCodes = new LinkedHashMap<>();
        }

        return this.valueSetCodes;
    }
}