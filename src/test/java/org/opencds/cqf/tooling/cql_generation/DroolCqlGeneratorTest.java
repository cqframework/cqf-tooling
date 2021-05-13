package org.opencds.cqf.tooling.cql_generation;

import java.io.File;
import java.net.URI;

import org.testng.annotations.Test;
import org.opencds.cqf.tooling.cql_generation.drool.DroolCqlGenerator;
import org.opencds.cqf.tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;

public class DroolCqlGeneratorTest {

    @Test
    public void   test_worked() {
        String dataInputPath = "../CQLGenerationDocs/NonGeneratedDocs/default.json";
        String outputPath = "../CQLGenerationDocs/GeneratedDocs/elm";
        CQLTYPES cqlType = CQLTYPES.CONDITION;
        String fhirVersion = "4.0.0";
        File file = new File(dataInputPath);
        if (file.isFile()) {
            URI encodingUri = file.toURI();
            URI outputUri = new File(outputPath).toURI();
            CqlGenerator droolIshCqlGenerator = new DroolCqlGenerator(cqlType);
            droolIshCqlGenerator.generateAndWriteToFile(encodingUri, outputUri, fhirVersion);
        } else {
            System.out.println("I am Failure.");
        }
    }
}
