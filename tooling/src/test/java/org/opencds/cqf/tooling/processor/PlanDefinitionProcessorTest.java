package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.plandefinition.PlanDefinitionProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlanDefinitionProcessorTest {

    private Path tempDir;
    private Path iniPath;
    private Path planDefinitionDir;
    private Path libraryDir;
    private final FhirContext fhirContext = FhirContext.forR4Cached();

    @AfterClass
    public void tearDown() throws IOException {
        // Recursively delete the temporary directory.
        deleteRecursively(tempDir);
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteRecursively(entry);
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            try {
                Files.deleteIfExists(path);
                break;
            } catch (IOException e) {
                try {
                    Thread.sleep(100); // Give OS time to release handle
                } catch (InterruptedException ignored) {}
            }
        }
    }

    @Test(priority = 0, description = "Test PlanDefinition processor referencing an IG without any PlanDefinition resources")
    void testEmptyIg() throws IOException {
        boilerplateIg();
        var parentContext = new BaseProcessor();
        parentContext.initializeFromIni(iniPath);
        var processor = new PlanDefinitionProcessor(new LibraryProcessor());
        var result = processor.refreshIgPlanDefinitionContent(parentContext, IOUtils.Encoding.JSON, false,
                fhirContext, planDefinitionDir.toString(), false);
        Assert.assertEquals(result.size(), 0);
    }

    @Test(priority = 1, description = "Test PlanDefinition processor referencing an IG with a very simple PlanDefinition resource")
    void testSimplePlanDefinition() throws IOException {
        buildSimpleIg();
        var parentContext = new BaseProcessor();
        parentContext.initializeFromIni(iniPath);
        LibraryProcessor libraryProcessor = new LibraryProcessor();
        libraryProcessor.initializeFromIni(iniPath);
        libraryProcessor.refreshIgLibraryContent(parentContext, IOUtils.Encoding.JSON, libraryDir.toString(),
                false, fhirContext, false);
        PlanDefinitionProcessor processor = new PlanDefinitionProcessor(libraryProcessor);
        var result = processor.refreshIgPlanDefinitionContent(parentContext, IOUtils.Encoding.JSON, false,
                fhirContext, planDefinitionDir.toString(), false);

        Assert.assertEquals(result.size(), 1);

        try (Reader reader = new FileReader(planDefinitionDir.resolve("Simple.json").toFile())) {
            var planDefinition = fhirContext.newJsonParser().parseResource(reader);
            Assert.assertNotNull(planDefinition);
            Assert.assertTrue(planDefinition instanceof PlanDefinition);
            Assert.assertTrue(((PlanDefinition) planDefinition).hasContained());
            Assert.assertTrue(((PlanDefinition) planDefinition).hasExtension());
        }
    }

    @Test(priority = 2, description = "Test PlanDefinition processor referencing an IG with a more complex PlanDefinition resource")
    void testComplexPlanDefinition() throws IOException {
        buildComplexIg();
        var parentContext = new BaseProcessor();
        parentContext.initializeFromIni(iniPath);
        LibraryProcessor libraryProcessor = new LibraryProcessor();
        libraryProcessor.initializeFromIni(iniPath);
        libraryProcessor.refreshIgLibraryContent(parentContext, IOUtils.Encoding.JSON, libraryDir.toString(),
                false, fhirContext, false);
        PlanDefinitionProcessor processor = new PlanDefinitionProcessor(libraryProcessor);
        var result = processor.refreshIgPlanDefinitionContent(parentContext, IOUtils.Encoding.JSON, false,
                fhirContext, planDefinitionDir.toString(), false);

        Assert.assertEquals(result.size(), 1);

        try (Reader reader = new FileReader(planDefinitionDir.resolve("Complex.json").toFile())) {
            var planDefinition = fhirContext.newJsonParser().parseResource(reader);
            Assert.assertNotNull(planDefinition);
            Assert.assertTrue(planDefinition instanceof PlanDefinition);
            Assert.assertTrue(((PlanDefinition) planDefinition).hasContained());
            Assert.assertTrue(((PlanDefinition) planDefinition).getContained().get(0) instanceof Library);
            var effectiveDateRequirements = (Library) ((PlanDefinition) planDefinition).getContained().get(0);
            Assert.assertTrue(effectiveDateRequirements.hasDataRequirement());
            Assert.assertTrue(effectiveDateRequirements.hasRelatedArtifact());
            Assert.assertTrue(effectiveDateRequirements.hasParameter());
            Assert.assertTrue(((PlanDefinition) planDefinition).hasExtension());
        }
    }

    private void boilerplateIg() throws IOException {
        // Create a temporary directory for the FHIR IG base
        tempDir = Files.createTempDirectory("FHIR-IG-");

        // Create ig.ini in the base directory with sample content for ig and fhir-version.
        iniPath = tempDir.resolve("ig.ini");
        String iniContent = "[IG]\n" +
                "ig = input/resources/ImplementationGuide.json\n" +
                "fhir-version = 4.0.1";
        Files.writeString(iniPath, iniContent);

        // Create the input folder and expected subdirectories.
        Path inputDir = tempDir.resolve("input");
        Files.createDirectory(inputDir);

        // Create input/cql directory (preferred CQL location)
        Path cqlDir = inputDir.resolve("cql");
        Files.createDirectory(cqlDir);

        // Create resources directory.
        Path resourcesDir = inputDir.resolve("resources");
        Files.createDirectory(resourcesDir);

        String igJson = "{\"resourceType\":\"ImplementationGuide\", \"id\":\"test-ig\", \"url\": \"http://fhir.org/guides/example/test-ig/ImplementationGuide/fhir.example.test-ig\", \"packageId\":\"fhir.example.test-ig\", \"fhirVersion\": [\"4.0.1\"]}";
        Path igFile = resourcesDir.resolve("ImplementationGuide.json");
        Files.writeString(igFile, igJson);

        // Create plandefinition directory.
        planDefinitionDir = resourcesDir.resolve("plandefinition");
        Files.createDirectory(planDefinitionDir);

        // Create vocabulary directory.
        Path vocabularyDir = inputDir.resolve("vocabulary");
        Files.createDirectory(vocabularyDir);

        // Create tests directory.
        Path testsDir = inputDir.resolve("tests");
        Files.createDirectory(testsDir);
    }

    private void buildSimpleIg() throws IOException {
        boilerplateIg();

        String cql = "library Simple version '1.0'\ndefine SimpleExpression: 5*5";
        Path cqlFile = tempDir.resolve("input").resolve("cql").resolve("Simple.cql");
        Files.writeString(cqlFile, cql);

        libraryDir = tempDir.resolve("input").resolve("resources").resolve("library");
        Files.createDirectory(libraryDir);
        String libraryJson = "{\"resourceType\":\"Library\", \"id\":\"Simple\", \"version\": \"1.0\", \"name\": \"Simple\", \"type\": { \"coding\": [ { \"system\": \"http://terminology.hl7.org/CodeSystem/library-type\", \"code\": \"logic-library\" } ] }, \"content\": [ { \"id\": \"ig-loader-Simple.cql\" } ]}";
        Path libraryFile = libraryDir.resolve("Simple.json");
        Files.writeString(libraryFile, libraryJson);

        String planDefinitionJson = "{\"resourceType\":\"PlanDefinition\", \"id\":\"Simple\", \"version\": \"1.0\", \"name\": \"Simple\", \"library\": [ \"http://fhir.org/guides/example/test-ig/Library/Simple\" ] } ]}";
        Path planDefinitionFile = planDefinitionDir.resolve("Simple.json");
        Files.writeString(planDefinitionFile, planDefinitionJson);
    }

    private void buildComplexIg() throws IOException {
        boilerplateIg();

        String cql = "library Complex version '1.0'\nusing FHIR version '4.0.1'\ninclude FHIRHelpers version '4.0.1' called FHIRHelpers\nvalueset \"Office Visit\": 'http://fhir.org/guides/example/test-ig/ValueSet/office-visit'\ndefine OfficeVisitEncounters: [Encounter: type in \"Office Visit\"]";
        Path cqlFile = tempDir.resolve("input").resolve("cql").resolve("Complex.cql");
        Files.writeString(cqlFile, cql);

        var vocabularyDir = tempDir.resolve("input").resolve("resources").resolve("vocabulary");
        Files.createDirectory(vocabularyDir);
        var valueSetDir = vocabularyDir.resolve("valueset");
        Files.createDirectory(valueSetDir);
        String valueSetJson = "{\"resourceType\":\"ValueSet\", \"id\":\"office-visit\", \"name\": \"OfficeVisit\", \"url\": \"http://fhir.org/guides/example/test-ig/ValueSet/office-visit\", \"expansion\": { \"contains\": [ { \"system\": \"http://snomed.info/sct\", \"code\": \"185463005\", \"display\": \"Visit out of hours (procedure)\" } ] } }";
        Path valueSetFile = valueSetDir.resolve("office-visit.json");
        Files.writeString(valueSetFile, valueSetJson);

        libraryDir = tempDir.resolve("input").resolve("resources").resolve("library");
        Files.createDirectory(libraryDir);
        String libraryJson = "{\"resourceType\":\"Library\", \"id\":\"Complex\", \"version\": \"1.0\", \"name\": \"Complex\", \"type\": { \"coding\": [ { \"system\": \"http://terminology.hl7.org/CodeSystem/library-type\", \"code\": \"logic-library\" } ] }, \"content\": [ { \"id\": \"ig-loader-Complex.cql\" } ]}";
        Path libraryFile = libraryDir.resolve("Complex.json");
        Files.writeString(libraryFile, libraryJson);

        String planDefinitionJson = "{\"resourceType\":\"PlanDefinition\", \"id\":\"Complex\", \"version\": \"1.0\", \"name\": \"Complex\", \"library\": [ \"http://fhir.org/guides/example/test-ig/Library/Complex\" ], \"action\": [ { \"condition\": [ { \"kind\": \"applicability\", \"expression\": { \"language\": \"text/cql.identifier\", \"expression\": \"OfficeVisitEncounters\" } } ] } ] }";
        Path planDefinitionFile = planDefinitionDir.resolve("Complex.json");
        Files.writeString(planDefinitionFile, planDefinitionJson);
    }
}
