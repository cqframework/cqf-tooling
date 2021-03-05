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

    private String customValueSetName;
    public String getCustomValueSetName() { return this.customValueSetName; }
    public void setCustomValueSetName(String customValueSetName) {
        this.customValueSetName = customValueSetName;
    }

    private CodeCollection codes;
    public CodeCollection getCodes() {
        if (this.codes == null) {
            this.codes = new CodeCollection();
        }

        return this.codes;
    }
}