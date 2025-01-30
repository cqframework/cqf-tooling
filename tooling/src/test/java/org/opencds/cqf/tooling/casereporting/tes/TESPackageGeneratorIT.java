package org.opencds.cqf.tooling.casereporting.tes;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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
        params.pathToConditionGrouperWorkbook = "src/test/resources/casereporting/tes/TES_Condition_Groupers_20240920.xlsx";
        params.pathToConditionCodeValueSet = "src/test/resources/casereporting/tes/valueset-rckms-condition-codes.json";
        params.outputPath = "src/test/resources/casereporting/tes/output";
        params.outputFileName = "condition-groupers-bundle.json";
        params.version = "0.0.1-alpha";
        params.releaseLabel = "2024-09-26 Release";
        params.writeConditionGroupers = true;

        Bundle tesPackage = generateTESPackage(params);

        assertEquals(tesPackage.getEntry().size(), 444);
        assertNotNull(tesPackage);
    }
}