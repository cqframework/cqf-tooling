package org.opencds.cqf.tooling.casereporting.tes;

public class ConditionGrouperEntry extends TESGrouperEntry {

    private String reportingSpecificationGrouperTitle;

    public String getReportingSpecificationGrouperTitle() {
        return reportingSpecificationGrouperTitle;
    }

    private String reportingSpecificationConditionCode;

    public String getReportingSpecificationConditionCode() {
        return reportingSpecificationConditionCode;
    }

    private String reportingSpecificationConditionDescription;

    public String getReportingSpecificationConditionDescription() {
        return reportingSpecificationConditionDescription;
    }

    public ConditionGrouperEntry(
            String conditionGrouperUrl,
            String conditionGrouperTitle,
            String reportingSpecificationGrouperTitle,
            String reportingSpecificationConditionCode,
            String reportingSpecificationConditionDescription) {
        super(conditionGrouperUrl, conditionGrouperTitle);
        this.reportingSpecificationGrouperTitle = reportingSpecificationGrouperTitle;
        this.reportingSpecificationConditionCode = reportingSpecificationConditionCode;
        this.reportingSpecificationConditionDescription = reportingSpecificationConditionDescription;
    }
}