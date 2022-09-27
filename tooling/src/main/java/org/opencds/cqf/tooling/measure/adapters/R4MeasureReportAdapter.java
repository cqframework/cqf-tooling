package org.opencds.cqf.tooling.measure.adapters;

import org.hl7.fhir.r4.model.MeasureReport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class R4MeasureReportAdapter implements IMeasureReportAdapter {

    private MeasureReport measureReport;
    public R4MeasureReportAdapter(MeasureReport measureReport) {
        this.measureReport = measureReport;
    }

    @Override
    public String getReportType() {
        String reportType = measureReport.getType().toString();
        return reportType;
    }

    //TODO: In R4 the Subject will not necessarily be a Patient.
    @Override
    public String getPatientId() {
        String[] subjectRefParts = measureReport.getSubject().getReference().split("/");
        String patientId = subjectRefParts[subjectRefParts.length - 1];
        return patientId;
    }

    @Override
    public String getMeasureId() {
        String[] measureRefParts = measureReport.getMeasure().split("/");
        String measureId = measureRefParts[measureRefParts.length - 1];
        return measureId;

//        String measureId = null;
//        measureId = measureReport.getMeasure() .getId();
//
//        if (measureId == null) {
//            String[] measureRefParts = measureReport.getMeasure().getReference().split("/");
//            measureId = measureRefParts[measureRefParts.length - 1];
//        }
//
//        return measureId;
    }

    @Override
    public Date getPeriodStart() {
        Date periodStart = measureReport.getPeriod().getStart();
        if (periodStart != null) {
            return periodStart;
        }
        return null;
    }

    @Override
    public Date getPeriodEnd() {
        Date periodEnd = measureReport.getPeriod().getEnd();
        if (periodEnd != null) {
            return periodEnd;
        }
        return null;
    }

    @Override
    public List<Group> getGroups() {
        List<Group> groups = new ArrayList<Group>();
        for (MeasureReport.MeasureReportGroupComponent groupComponent : measureReport.getGroup()) {
            Group group = new Group();
            group.name = groupComponent.getId();
            group.score = groupComponent.getMeasureScore().getValue();
            groups.add(group);
        }
        return groups;
    }

    @Override
    public BigDecimal getGroupScore(String groupId) {
        Objects.requireNonNull(groupId, "groupId can not be null.");
        BigDecimal score = null;
        List<MeasureReport.MeasureReportGroupComponent> groups = measureReport.getGroup();
        for (MeasureReport.MeasureReportGroupComponent group : groups) {
            if (group.getId().equals(groupId)) {
                score = group.getMeasureScore().getValue();
                break;
            }
        }
        return score;
    }
}