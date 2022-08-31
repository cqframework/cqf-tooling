package org.opencds.cqf.tooling.cql_generation;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Parameters;
import org.opencds.cqf.cql.evaluator.builder.Constants;
import org.opencds.cqf.cql.evaluator.library.LibraryProcessor;
import org.opencds.cqf.tooling.cql_generation.drool.DroolCqlGenerator;
import org.opencds.cqf.tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest
@ContextConfiguration(classes = SpringTestConfig.class)
public class DroolCqlGeneratorIT  extends AbstractTestNGSpringContextTests {

    private static final Logger logger = LoggerFactory.getLogger(DroolCqlGeneratorIT.class);

    @Autowired
    private LibraryProcessor libraryProcessor;

    private Endpoint terminologyEndpoint;
    private Endpoint dataEndpoint;
    private Endpoint libraryEndpoint;
    private File outputFile;

    public DroolCqlGeneratorIT() {
        String outputPath = "target/test-output/cql-from-drool";
        this.outputFile = new File(outputPath); 
        this.libraryEndpoint= new Endpoint()
            .setAddress(outputFile.getParent())
            .setConnectionType(new Coding().setCode(Constants.HL7_CQL_FILES));
        this.terminologyEndpoint = new Endpoint()
            .setAddress(this.getClass().getResource("concepts_full.json").getPath())
            .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_FILES));
        this.dataEndpoint = new Endpoint()
            .setAddress(this.getClass().getResource("AllResources.json").getPath())
            .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_FILES));
    }

    @BeforeClass
    public void setup() throws URISyntaxException {
        String dataInputPath = "default.json";
        CQLTYPES cqlType = CQLTYPES.CONDITIONREL;
        String fhirVersion = "4.0.0";
        URI encodingUri = DroolCqlGeneratorIT.class.getResource(dataInputPath).toURI();
        CqlGenerator droolIshCqlGenerator = new DroolCqlGenerator(cqlType);
        droolIshCqlGenerator.generateAndWriteToFile(encodingUri, outputFile.toURI(), fhirVersion);
    }

    @Test(testName="MedicationsAdministered_2764c")
    public void test_ModelingLAndD() throws IOException {
        String expressionListFilePath = "expressions/_Medications_Adminis_Expression.txt";
        Set<String> expressions = readFileInList(expressionListFilePath);
        
        VersionedIdentifier vi = new VersionedIdentifier();
        vi.setId("MedicationsAdministered_2764c");
        vi.setVersion("1.0.0");
        Parameters result = (Parameters) libraryProcessor.evaluate(vi, null, null, libraryEndpoint, terminologyEndpoint, dataEndpoint, null, expressions);

        for (String expression : expressions) {
            BooleanType value = (BooleanType) result.getParameter(expression);
            assertTrue(value.getValue());
        }
    }

    @Test(testName="Patientisdeceased_84755")
    public void test_ModelingN() throws IOException {
        String expressionListFilePath = "expressions/Patient_is_deceased__Expression.txt";
        Set<String> expressions = readFileInList(expressionListFilePath);
        
        VersionedIdentifier vi = new VersionedIdentifier();
        vi.setId("Patientisdeceased_84755");
        vi.setVersion("1.0.0");
        Parameters result = (Parameters) libraryProcessor.evaluate(vi, null, null, libraryEndpoint, terminologyEndpoint, dataEndpoint, null, expressions);

        for (String expression : expressions) {
            BooleanType value = (BooleanType) result.getParameter(expression);
            assertTrue(value.getValue());
        }
    }

    @Test(testName="AbdominalCramps_314c4")
    public void other() throws IOException {
        String expressionListFilePath = "expressions/Abdominal_Cramps_314_Expression.txt";
        Set<String> expressions = readFileInList(expressionListFilePath);
        
        VersionedIdentifier vi = new VersionedIdentifier();
        vi.setId("AbdominalCramps_314c4");
        vi.setVersion("1.0.0");
        Parameters result = (Parameters) libraryProcessor.evaluate(vi, null, null, libraryEndpoint, terminologyEndpoint, dataEndpoint, null, expressions);

        for (String expression : expressions) {
            BooleanType value = (BooleanType) result.getParameter(expression);
            assertTrue(value.getValue());
        }
    }

    @Test(testName="Anorexia_baace")
    public void otherOther() throws IOException {
        String expressionListFilePath = "expressions/Anorexia_baace_Expression.txt";
        Set<String> expressions = readFileInList(expressionListFilePath);
        
        VersionedIdentifier vi = new VersionedIdentifier();
        vi.setId("Anorexia_baace");
        vi.setVersion("1.0.0");
        Parameters result = (Parameters) libraryProcessor.evaluate(vi, null, null, libraryEndpoint, terminologyEndpoint, dataEndpoint, null, expressions);

        for (String expression : expressions) {
            BooleanType value = (BooleanType) result.getParameter(expression);
            assertTrue(value.getValue());
        }
    }

    public static Set<String> readFileInList(String fileName) throws IOException {
        Set<String> list = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(DroolCqlGeneratorIT.class.getResourceAsStream(fileName)));
        while(reader.ready()) {
            list.add(reader.readLine());
        }
        return list;
    }
}
