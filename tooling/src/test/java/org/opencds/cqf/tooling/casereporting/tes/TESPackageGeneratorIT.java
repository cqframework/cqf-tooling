package org.opencds.cqf.tooling.casereporting.tes;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class TESPackageGeneratorIT {
    private static final Logger logger = LoggerFactory.getLogger(TESPackageGeneratorIT.class);

    private Bundle generateTESPackage(TESPackageGenerateParameters params) throws Exception {
        TESPackageGenerator TESPackageGenerator = new TESPackageGenerator();

        return TESPackageGenerator.generatePackage(params);
    }

    @Test
    public void testTESPackageGenerate() throws Exception {
        TESPackageGenerateParameters params = new TESPackageGenerateParameters();
        params.pathToInputBundle = "src/test/resources/casereporting/tes/reporting-specification-groupers-bundle.json";
        params.pathToGroupersWorkbook = "src/test/resources/casereporting/tes/TES_Groupers.xlsx";
        params.pathToConditionCodeValueSet = "src/test/resources/casereporting/tes/valueset-rckms-condition-codes.json";
        params.outputPath = "src/test/resources/casereporting/tes/output";
        params.outputFileName = "condition-groupers-bundle.json";
        params.version = "4.0.0";
        params.releaseLabel = "2025-12-04 Release";
        params.writeConditionGroupers = true;
        params.writeAdditionalContextGroupers = true;
        params.writeReportingSpecificationGroupers = true;

        Bundle tesPackage = generateTESPackage(params);

        assertEquals(tesPackage.getEntry().size(), 657); // 656 ValueSets, 1 Library
        assertNotNull(tesPackage);
    }
}
