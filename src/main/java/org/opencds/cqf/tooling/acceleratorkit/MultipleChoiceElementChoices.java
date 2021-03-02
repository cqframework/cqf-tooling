package org.opencds.cqf.tooling.acceleratorkit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleChoiceElementChoices {
    private DictionaryFhirElementPath fhirElementPath;
    public DictionaryFhirElementPath getFhirElementPath() {
        return this.fhirElementPath;
    }
    public void setFhirElementPath(DictionaryFhirElementPath fhirElementPath) {
        this.fhirElementPath = fhirElementPath;
    }

    private CodeCollection codes;
    public CodeCollection getCodes() {
        return this.codes;
    }
}