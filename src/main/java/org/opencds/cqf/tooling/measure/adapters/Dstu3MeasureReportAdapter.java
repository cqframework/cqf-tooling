package org.opencds.cqf.tooling.measure.adapters;


import org.hl7.fhir.dstu3.model.Measure;
import org.hl7.fhir.dstu3.model.MeasureReport;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Dstu3MeasureReportAdapter implements IMeasureReportAdapter {
    private MeasureReport measureReport;
    public Dstu3MeasureReportAdapter(MeasureReport measureReport) {
        this.measureReport = measureReport;
    }

    @Override
    public String getReportType() {
        String reportType = measureReport.getType().toString();
        return reportType;
    }

    @Override
    public String getPatientId() {
        String id = measureReport.getPatient().getId();
        return id;
    }

    @Override
    public String getMeasureId() {
        String measureId = measureReport.getMeasure().getId();
        return measureId;
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
                score = group.getMeasureScore();
                break;
            }
        }
        return score;
    }
}