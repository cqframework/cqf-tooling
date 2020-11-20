package org.opencds.cqf.processing.test.library.src;

import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.GuidanceResponse;
import org.hl7.fhir.r4.model.Parameters;


public interface GuidanceResponseComparator {
    public boolean compare(Pair<GuidanceResponse, Parameters> expected, Pair<GuidanceResponse, Parameters> actual);
}