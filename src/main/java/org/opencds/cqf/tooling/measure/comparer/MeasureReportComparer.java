package org.opencds.cqf.tooling.measure.comparer;

import java.util.List;

import org.hl7.fhir.Parameters;
import org.hl7.fhir.ParametersParameter;
import org.hl7.fhir.String;
import org.opencds.cqf.tooling.measure.MeasureTestProcessor;
import org.opencds.cqf.tooling.measure.adapters.IMeasureReportAdapter;

import ca.uhn.fhir.context.FhirContext;

public class MeasureReportComparer {

    // private FhirContext fhirContext;

    public MeasureReportComparer(FhirContext fhirContext) {
        // this.fhirContext = fhirContext;
    }

    public Parameters compare(IMeasureReportAdapter actual, IMeasureReportAdapter expected) {
        Parameters results = new Parameters();
        boolean overallPassFail = true;

        ParametersParameter parameter = new ParametersParameter();

        overallPassFail = overallPassFail & compareField("ReportType", actual.getReportType(), expected.getReportType(), results);
        overallPassFail = overallPassFail & compareField("MeasureId", actual.getMeasureId(), expected.getMeasureId(), results);
        overallPassFail = overallPassFail & compareField("PatientId", actual.getPatientId(), expected.getPatientId(), results);

        //NOTE: Set of groups must be equal. Expected must be a subset of Actual, but not a proper subset.
        if (actual.getGroups().size() != expected.getGroups().size()) {
            addResultParameter("GroupCount", results, false);
            overallPassFail = false;
        }

        // Compare the measurescore of each group.
        List<IMeasureReportAdapter.Group> actualGroups = actual.getGroups();

        for (IMeasureReportAdapter.Group expectedGroup : expected.getGroups()) {
            boolean foundMatch = false;
            for (IMeasureReportAdapter.Group actualGroup : actualGroups) {
                if (actualGroup.getName().equals(expectedGroup.getName())) {
                    if (actualGroup.getScore().compareTo(expectedGroup.getScore()) == 0) {
                        foundMatch = true;
                        break;
                    }
                }
            }
            addResultParameter("Group[" + expectedGroup.getName() + "].score", results, foundMatch);
            overallPassFail = overallPassFail & foundMatch;
        }

        //TODO: Compare group population scores

        parameter.setName(new String().withValue("Measure '" + expected.getMeasureId() + "' " + MeasureTestProcessor.TestPassedKey));
        parameter.setValueBoolean(new org.hl7.fhir.Boolean().withValue(overallPassFail));
        results.getParameter().add(parameter);

        return results;
    }

    private boolean compareField(java.lang.String fieldName, java.lang.String actual, java.lang.String expected, Parameters results) {
        boolean matches = actual.equals(expected);
        addResultParameter(fieldName, results, matches);

        return matches;
    }

    private void addResultParameter(java.lang.String fieldName, Parameters results, boolean passed) {
        ParametersParameter param = new ParametersParameter();
        param.setName(new String().withValue(fieldName));
        param.setValueBoolean(new org.hl7.fhir.Boolean().withValue(passed));
        results.getParameter().add(param);
    }
}