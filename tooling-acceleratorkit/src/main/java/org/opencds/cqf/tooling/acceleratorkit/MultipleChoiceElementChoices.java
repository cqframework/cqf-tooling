package org.opencds.cqf.tooling.acceleratorkit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
