package org.opencds.cqf.tooling.casereporting.tes;

public class TESGrouperEntry {
    private String conditionGrouperUrl;

    public String getConditionGrouperUrl() {
        return conditionGrouperUrl;
    }

    private String conditionGrouperTitle;

    public String getConditionGrouperTitle() {
        return conditionGrouperTitle;
    }

    public TESGrouperEntry(String conditionGrouperUrl, String conditionGrouperTitle) {
        this.conditionGrouperUrl = conditionGrouperUrl;
        this.conditionGrouperTitle = conditionGrouperTitle;
    }
}