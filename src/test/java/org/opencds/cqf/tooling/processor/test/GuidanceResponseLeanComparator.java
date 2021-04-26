package org.opencds.cqf.tooling.processor.test;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.GuidanceResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Parameters;
import java.util.Objects;

public class GuidanceResponseLeanComparator implements GuidanceResponseComparator {
    @Override
    public boolean compare(Pair<GuidanceResponse, Parameters> expected, Pair<GuidanceResponse, Parameters> actual) {
        Objects.requireNonNull(expected.getLeft(), "Expected GuidanceResponse can not be null");
        if (actual.getLeft() == null) {
            Objects.requireNonNull(expected.getLeft(), "If actual GuidanceResponse does not exist, then actual Parameters can not be null");
        }
        boolean comparisonResult = true;
        if (expected.getLeft() != null && actual.getLeft() != null) {
            comparisonResult = validateSubject(expected.getLeft().getSubject(), actual.getLeft().getSubject());
            if (comparisonResult) {
                comparisonResult = compareStatus(expected.getLeft().getStatus(), actual.getLeft().getStatus());
            }
        }

        if (comparisonResult) {
            if (expected.getRight() != null && actual.getRight() != null) {
                comparisonResult = compareOutputParameters(expected.getRight(), actual.getRight());
            } else if (expected.getRight() != null || actual.getRight() != null) {
                comparisonResult = false;
            }
        }
        return comparisonResult;
    }

    private boolean compareOutputParameters(Parameters expectedOutputParameters, Parameters actualOutputParameters) {
        boolean parametersMatch = true;
        parametersMatch = expectedOutputParameters.equalsDeep(actualOutputParameters);
        return parametersMatch;
    }

    private boolean validateSubject(Reference expectedSubject, Reference actualSubject) {
        boolean sameSubject = (expectedSubject.getType().equals(actualSubject.getType()));
        if (sameSubject) {
            sameSubject = (expectedSubject.getReference().equals(actualSubject.getReference()));
        } else {
            System.out.printf("Subject type does not match. { %s, %s }", expectedSubject, actualSubject);
        }
        return sameSubject;
    }

    private boolean compareStatus(GuidanceResponse.GuidanceResponseStatus expectedStatus,
            GuidanceResponse.GuidanceResponseStatus actualStatus) {
        boolean sameStatus = true;
        if (!expectedStatus.equals(actualStatus)) {
            sameStatus = false;
            System.out.printf("GuidanceResponse Status does not match: { %s, %s }", expectedStatus, actualStatus);
        }
        return sameStatus;
    }

}