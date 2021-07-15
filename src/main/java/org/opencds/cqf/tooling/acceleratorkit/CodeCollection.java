package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.Coding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CodeCollection {
    public CodeCollection() {
        this.codes = new ArrayList<DictionaryCode>();
    }

    public CodeCollection(List<DictionaryCode> codes) {
        this.codes = codes;
    }

    private List<DictionaryCode> codes;
    public List<DictionaryCode> getCodes() {
        if (this.codes == null) {
            this.codes = new ArrayList<>();
        }
        return this.codes;
    }

    public List<DictionaryCode> getValidCodes() {
        return this.getCodes().stream()
                .filter((c) -> !c.getCode().trim().isEmpty())
                .collect(Collectors.toList());
    }

    public List<DictionaryCode> getCodesForSystem(String system) {
        if (this.codes == null) {
            this.codes = new ArrayList<>();
        }
        List<DictionaryCode> codes = this.getValidCodes().stream()
                .filter((c) -> c.getSystem().equals(system))
                .collect(Collectors.toList());
        return codes;
    }

    private ArrayList<String> codeSystemUrls;
    public List<String> getCodeSystemUrls() {
        if (this.codeSystemUrls == null) {
            this.codeSystemUrls = new ArrayList<>();
        }
        List<String> codeSystemUrls = this.getValidCodes().stream()
                .map((c) -> c.getSystem())
                .distinct()
                .collect(Collectors.toList());
        return codeSystemUrls;
    }

    public int size() {
        return getCodes().size();
    }

    public List<Coding> toCodings() {
        List<Coding> codings = new ArrayList<>();
        for (DictionaryCode code : this.getCodes()) {
            Coding coding = new Coding();
            coding.setCode(code.getCode());
            coding.setSystem(code.getSystem());
            coding.setDisplay(code.getDisplay());

            codings.add(coding);
        }

        return codings;
    }
}