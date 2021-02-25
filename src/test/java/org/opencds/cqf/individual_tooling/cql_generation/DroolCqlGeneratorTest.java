package org.opencds.cqf.individual_tooling.cql_generation;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import org.opencds.cqf.individual_tooling.cql_generation.drool.DroolCqlGenerator;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;

public class DroolCqlGeneratorTest {

    @Test
    public void test_worked() {
        String encodingPath = "../CQLGenerationDocs/NonGeneratedDocs/default.json";
        String outputPath = "../CQLGenerationDocs/GeneratedDocs/elm";
        CQLTYPES cqlType = CQLTYPES.CONDITION;
        String fhirVersion = "4.0.0";
        File file = new File(encodingPath);
        Boolean fileIsFile = file.isFile();
        if (fileIsFile) {
            URI encodingUri = file.toURI();

            CqlGenerator droolIshCqlGenerator = new DroolCqlGenerator(outputPath, cqlType);

            File outputFile = new File(outputPath);
            Boolean outputFileIsFile = outputFile.isFile();
            if (!outputFileIsFile) {
                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            droolIshCqlGenerator.generate(encodingUri, fhirVersion);
        } else {
            System.out.println("I am Failure.");
        }
    }
}
