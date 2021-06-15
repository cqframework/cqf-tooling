package org.opencds.cqf.tooling.cql_generation;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.testng.annotations.Test;
import org.opencds.cqf.tooling.cql_generation.drool.DroolCqlGenerator;
import org.opencds.cqf.tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;

public class DroolCqlGeneratorTest {

    @Test
    public void   test_worked() throws URISyntaxException {
        String dataInputPath = "default.json";
        String outputPath = "src\\test\\resources\\org\\opencds\\cqf\\tooling\\cql_generation\\drool\\visitor";
        CQLTYPES cqlType = CQLTYPES.CONDITION;
        String fhirVersion = "4.0.0";
        URI encodingUri = DroolCqlGeneratorTest.class.getResource(dataInputPath).toURI();
        URI outputUri = new File(outputPath).toURI();
        CqlGenerator droolIshCqlGenerator = new DroolCqlGenerator(cqlType);
        droolIshCqlGenerator.generateAndWriteToFile(encodingUri, outputUri, fhirVersion);
    }
}
