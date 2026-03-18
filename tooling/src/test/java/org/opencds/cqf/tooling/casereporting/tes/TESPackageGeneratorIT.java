package org.opencds.cqf.tooling.casereporting.tes;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hl7.fhir.r4.model.Bundle;

/**
 * Integration tests for TESPackageGenerator.
 *
 * Test fixtures assumed to live under src/test/resources/casereporting/tes/:
 *
 *   reporting-specification-groupers-bundle.json – A transaction Bundle of Reporting Specification Grouper ValueSets
 *   TES_Groupers.xlsx – The TES groupers workbook (condition + additional-context sheets)
 *   valueset-rckms-condition-codes.json – A ValueSet of condition codes used for the diff report
 *
 * Each test that writes output does so into a temp directory created under
 * target/test-output/tes/ so assertions on the filesystem never bleed between runs.
 */
public class TESPackageGeneratorIT {

    private static final Logger logger = LoggerFactory.getLogger(TESPackageGeneratorIT.class);

    // ---------------------------------------------------------------------------
    // Fixture paths (relative to the project root so IDEs and Maven both resolve)
    // ---------------------------------------------------------------------------
    private static final String RESOURCES_BASE =
            "src/test/resources/casereporting/tes/";
    private static final String INPUT_BUNDLE_JSON =
            RESOURCES_BASE + "reporting-specification-groupers-bundle.json";
    private static final String GROUPERS_WORKBOOK =
            RESOURCES_BASE + "TES_Groupers.xlsx";
    private static final String CONDITION_CODE_VS_JSON =
            RESOURCES_BASE + "valueset-rckms-condition-codes.json";

    // ---------------------------------------------------------------------------
    // Shared state
    // ---------------------------------------------------------------------------
    private FhirContext fhirContext;
    private String tempOutputBase;

    @BeforeClass
    public void setUp() {
        fhirContext = FhirContext.forR4();
        // Each test gets its own subfolder so parallel runs don't collide
        tempOutputBase = "target/test-output/tes/" + System.currentTimeMillis();
        new File(tempOutputBase).mkdirs();
    }

    // ===========================================================================
    // Helper – build a minimal but valid TESPackageGenerateParameters
    // ===========================================================================
    private TESPackageGenerateParameters baseParams(String testName) {
        TESPackageGenerateParameters p = new TESPackageGenerateParameters();
        p.version = "1.0.0";
        p.releaseLabel = "STU1";
        p.pathToInputBundle = INPUT_BUNDLE_JSON;
        p.pathToGroupersWorkbook = GROUPERS_WORKBOOK;
        p.pathToConditionCodeValueSet = "";   // omit diff report unless explicitly needed
        p.outputPath = tempOutputBase + "/" + testName;
        p.outputFileName = "tes-content-bundle";
        p.outputFileEncodings = new HashSet<>();
        p.outputFileEncodings.add(IOUtils.Encoding.JSON);
        p.writeConditionGroupers = false;
        p.writeReportingSpecificationGroupers = false;
        p.writeAdditionalContextGroupers = false;
        new File(p.outputPath).mkdirs();
        return p;
    }

    // ===========================================================================
    // 1. Happy-path – generates a non-null, non-empty Bundle
    // ===========================================================================
    @Test
    public void generateTESPackage_returnsNonEmptyBundle() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("happy-path");

        Bundle result = generator.generatePackage(params);

        Assert.assertNotNull(result, "generatePackage should never return null");
        Assert.assertFalse(
                result.getEntry().isEmpty(),
                "Result bundle should contain at least one entry");
    }

    // ===========================================================================
    // 2. Bundle contains a Library manifest entry
    // ===========================================================================
    @Test
    public void generateTESPackage_bundleContainsManifestLibrary() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("manifest-check");

        Bundle result = generator.generatePackage(params);

        long libraryCount = result.getEntry().stream()
                .filter(e -> e.getResource() instanceof Library)
                .count();
        Assert.assertEquals(
                libraryCount,
                1,
                "Bundle should contain exactly one manifest Library");
    }

    // ===========================================================================
    // 3. Manifest Library has expected metadata
    // ===========================================================================
    @Test
    public void generateTESPackage_manifestLibraryHasExpectedMetadata() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("manifest-metadata");

        Bundle result = generator.generatePackage(params);

        Library manifest = result.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(r -> r instanceof Library)
                .map(r -> (Library) r)
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(manifest, "Manifest Library must be present");
        Assert.assertEquals(manifest.getVersion(), "1.0.0", "Library version should match input version");
        Assert.assertEquals(manifest.getName(), "TESContentLibrary", "Library name should be TESContentLibrary");
        Assert.assertFalse(
                manifest.getRelatedArtifact().isEmpty(),
                "Manifest should reference at least one related artifact");
    }

    // ===========================================================================
    // 4. Bundle contains Condition Grouper ValueSets
    // ===========================================================================
    @Test
    public void generateTESPackage_bundleContainsConditionGroupers() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("condition-groupers");

        Bundle result = generator.generatePackage(params);

        long conditionGrouperCount = result.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(r -> r instanceof ValueSet)
                .map(r -> (ValueSet) r)
                .filter(vs -> vs.getUseContext().stream()
                        .anyMatch(uc -> uc.hasValueCodeableConcept()
                                && uc.getValueCodeableConcept().getCodingFirstRep() != null
                                && "condition-grouper".equals(
                                uc.getValueCodeableConcept().getCodingFirstRep().getCode())))
                .count();

        Assert.assertTrue(
                conditionGrouperCount > 0,
                "Bundle should contain at least one Condition Grouper ValueSet");
    }

    // ===========================================================================
    // 5. Condition Groupers all have a compose element (runSimpleValidation passes)
    // ===========================================================================
    @Test
    public void generateTESPackage_conditionGroupersHaveCompose() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("compose-check");

        Bundle result = generator.generatePackage(params);

        List<ValueSet> conditionGroupers = result.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(r -> r instanceof ValueSet)
                .map(r -> (ValueSet) r)
                .filter(vs -> vs.getUseContext().stream()
                        .anyMatch(uc -> uc.hasValueCodeableConcept()
                                && "condition-grouper".equals(
                                uc.getValueCodeableConcept().getCodingFirstRep().getCode())))
                .collect(Collectors.toList());

        Assert.assertFalse(conditionGroupers.isEmpty(), "Should have found condition groupers to validate");

        for (ValueSet vs : conditionGroupers) {
            Assert.assertTrue(
                    vs.hasCompose(),
                    String.format("Condition Grouper '%s' should have a compose element", vs.getTitle()));
        }
    }

    // ===========================================================================
    // 6. Bundle contains Additional Context Grouper ValueSets
    // ===========================================================================
    @Test
    public void generateTESPackage_bundleContainsAdditionalContextGroupers() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("ac-groupers");

        Bundle result = generator.generatePackage(params);

        long acGrouperCount = result.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(r -> r instanceof ValueSet)
                .map(r -> (ValueSet) r)
                .filter(vs -> vs.getUseContext().stream()
                        .anyMatch(uc -> uc.hasValueCodeableConcept()
                                && "additional-context-grouper".equals(
                                uc.getValueCodeableConcept().getCodingFirstRep().getCode())))
                .count();

        Assert.assertTrue(
                acGrouperCount > 0,
                "Bundle should contain at least one Additional Context Grouper ValueSet");
    }

    // ===========================================================================
    // 7. Output Bundle is serialisable as JSON (round-trip check)
    // ===========================================================================
    @Test
    public void generateTESPackage_outputBundleIsSerializableAsJson() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("json-round-trip");

        Bundle result = generator.generatePackage(params);

        String json = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(result);
        Assert.assertNotNull(json, "Serialised JSON should not be null");
        Assert.assertTrue(json.contains("\"resourceType\""), "JSON should contain resourceType field");
        Assert.assertTrue(json.contains("\"Bundle\""), "JSON resourceType should be Bundle");
    }

    // ===========================================================================
    // 8. Output files are written to disk when encoding is JSON
    // ===========================================================================
    @Test
    public void generateTESPackage_writesJsonOutputBundleToDisk() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("disk-write-json");

        generator.generatePackage(params);

        File expectedFile = new File(params.outputPath + "/tes-content-bundle.json");
        Assert.assertTrue(
                expectedFile.exists(),
                "Expected tes-content-bundle.json to be written at: " + expectedFile.getAbsolutePath());
        Assert.assertTrue(expectedFile.length() > 0, "Output file should not be empty");
    }

    // ===========================================================================
    // 0. Output files are written to disk when encoding is XML
    // ===========================================================================
    @Test
    public void generateTESPackage_writesXmlOutputBundleToDisk() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("disk-write-xml");
        params.outputFileEncodings = new HashSet<>();
        params.outputFileEncodings.add(IOUtils.Encoding.XML);

        generator.generatePackage(params);

        File expectedFile = new File(params.outputPath + "/tes-content-bundle.xml");
        Assert.assertTrue(
                expectedFile.exists(),
                "Expected tes-content-bundle.xml to be written at: " + expectedFile.getAbsolutePath());
        Assert.assertTrue(expectedFile.length() > 0, "XML output file should not be empty");
    }

    // ===========================================================================
    // 10. writeConditionGroupers=true writes individual Condition Grouper files
    // ===========================================================================
    @Test
    public void generateTESPackage_writesConditionGrouperFiles_whenFlagIsTrue() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("write-cg");
        params.writeConditionGroupers = true;

        generator.generatePackage(params);

        File grouperDir = new File(params.outputPath + "/condition-groupers");
        Assert.assertTrue(
                grouperDir.exists() && grouperDir.isDirectory(),
                "condition-groupers directory should be created");

        File[] files = grouperDir.listFiles((d, name) -> name.endsWith(".json"));
        Assert.assertNotNull(files, "File listing should not be null");
        Assert.assertTrue(files.length > 0, "At least one Condition Grouper file should be written");
    }

    // ===========================================================================
    // 11. writeAdditionalContextGroupers=true writes individual AC Grouper files
    // ===========================================================================
    @Test
    public void generateTESPackage_writesAdditionalContextGrouperFiles_whenFlagIsTrue() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("write-acg");
        params.writeAdditionalContextGroupers = true;

        generator.generatePackage(params);

        File grouperDir = new File(params.outputPath + "/additional-context-groupers");
        Assert.assertTrue(
                grouperDir.exists() && grouperDir.isDirectory(),
                "additional-context-groupers directory should be created");

        File[] files = grouperDir.listFiles((d, name) -> name.endsWith(".json"));
        Assert.assertNotNull(files);
        Assert.assertTrue(files.length > 0, "At least one Additional Context Grouper file should be written");
    }

    // ===========================================================================
    // 12. generateConditionCodeUsageComparison – diff workbook is created
    // ===========================================================================
    @Test
    public void generateTESPackage_writesDiffWorkbook_whenConditionCodeValueSetProvided() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("diff-workbook");
        params.pathToConditionCodeValueSet = CONDITION_CODE_VS_JSON;

        generator.generatePackage(params);

        File diffFile = new File(params.outputPath + "/condition-code-diff.xlsx");
        Assert.assertTrue(
                diffFile.exists(),
                "condition-code-diff.xlsx should be created when a condition code value set is provided");
    }

    // ===========================================================================
    // 13. generated-grouper-urls workbook is always created
    // ===========================================================================
    @Test
    public void generateTESPackage_writesGeneratedGrouperUrlsWorkbook() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("grouper-urls");

        generator.generatePackage(params);

        File urlsFile = new File(params.outputPath + "/generated-grouper-urls.xlsx");
        Assert.assertTrue(
                urlsFile.exists(),
                "generated-grouper-urls.xlsx should always be written");
    }

    // ===========================================================================
    // 14. Missing pathToInputBundle throws an IllegalArgumentException
    // ===========================================================================
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void execute_missingInputBundle_throwsIllegalArgumentException() {
        TESPackageGenerator generator = new TESPackageGenerator();
        // -CaseReportingTESGeneratePackage is the operation discriminator; no -pathtoinputbundle is provided
        generator.execute(new String[]{"-CaseReportingTESGeneratePackage", "-version=1.0.0"});
    }

    // ===========================================================================
    // 15. Unsupported encoding flag throws an IllegalArgumentException
    // ===========================================================================
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void execute_unsupportedEncoding_throwsIllegalArgumentException() {
        TESPackageGenerator generator = new TESPackageGenerator();
        generator.execute(new String[]{
                "-CaseReportingTESGeneratePackage",
                "-pathtoinputbundle=" + INPUT_BUNDLE_JSON,
                "-encoding=csv"   // csv is not a supported encoding
        });
    }

    // ===========================================================================
    // 16. Unknown flag throws an IllegalArgumentException
    // ===========================================================================
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void execute_unknownFlag_throwsIllegalArgumentException() {
        TESPackageGenerator generator = new TESPackageGenerator();
        generator.execute(new String[]{
                "-CaseReportingTESGeneratePackage",
                "-pathtoinputbundle=" + INPUT_BUNDLE_JSON,
                "-thisIsNotAValidFlag=someValue"
        });
    }

    // ===========================================================================
    // 17. Bundle entries use conditional URL transaction entries
    // ===========================================================================
    @Test
    public void generateTESPackage_bundleEntriesHaveRequestUrls() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("transaction-entries");

        Bundle result = generator.generatePackage(params);

        long entriesWithRequest = result.getEntry().stream()
                .filter(e -> e.hasRequest() && e.getRequest().hasUrl())
                .count();

        Assert.assertEquals(
                entriesWithRequest,
                result.getEntry().size(),
                "Every bundle entry should have a transaction request URL");
    }

    // ===========================================================================
    // 18. Versioned bundle id is set correctly
    // ===========================================================================
    @Test
    public void generateTESPackage_bundleIdContainsVersion() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("bundle-id");

        Bundle result = generator.generatePackage(params);

        Assert.assertNotNull(result.getId(), "Bundle ID should not be null");
        Assert.assertTrue(
                result.getId().contains(params.version),
                "Bundle ID should contain the version string");
    }

    // ===========================================================================
    // 19. Manifest releaseLabel extension is set when provided
    // ===========================================================================
    @Test
    public void generateTESPackage_manifestContainsReleaseLabelExtension_whenReleaseLabelProvided() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("release-label");
        params.releaseLabel = "STU1-ballot";

        Bundle result = generator.generatePackage(params);

        Library manifest = result.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(r -> r instanceof Library)
                .map(r -> (Library) r)
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(manifest);

        boolean hasReleaseLabelExt = manifest.getExtension().stream()
                .anyMatch(ext -> ext.getUrl().contains("artifact-releaseLabel"));
        Assert.assertTrue(
                hasReleaseLabelExt,
                "Manifest should carry the artifact-releaseLabel extension when a releaseLabel is provided");
    }

    // ===========================================================================
    // 20. Manifest omits releaseLabel extension when not provided
    // ===========================================================================
    @Test
    public void generateTESPackage_manifestOmitsReleaseLabelExtension_whenNotProvided() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("no-release-label");
        params.releaseLabel = null;

        Bundle result = generator.generatePackage(params);

        Library manifest = result.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(r -> r instanceof Library)
                .map(r -> (Library) r)
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(manifest);

        boolean hasReleaseLabelExt = manifest.getExtension().stream()
                .anyMatch(ext -> ext.getUrl().contains("artifact-releaseLabel"));
        Assert.assertFalse(
                hasReleaseLabelExt,
                "Manifest should NOT carry the artifact-releaseLabel extension when no releaseLabel is provided");
    }

    // ===========================================================================
    // 21. Written JSON bundle can be re-parsed by HAPI to produce an equivalent Bundle
    // ===========================================================================
    @Test
    public void generateTESPackage_writtenJsonBundle_isReparseable() throws Exception {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("reparse-json");

        generator.generatePackage(params);

        File bundleFile = new File(params.outputPath + "/tes-content-bundle.json");

        try (FileInputStream fis = new FileInputStream(bundleFile)) {
            Bundle reparsed = (Bundle) ((JsonParser) fhirContext.newJsonParser())
                    .parseResource(fis);

            Assert.assertNotNull(reparsed, "Re-parsed bundle should not be null");
            Assert.assertFalse(reparsed.getEntry().isEmpty(), "Re-parsed bundle should have entries");
        }
    }

    // ===========================================================================
    // 22. Written XML bundle can be re-parsed by HAPI to produce an equivalent Bundle
    // ===========================================================================
    @Test
    public void generateTESPackage_writtenXmlBundle_isReparseable() throws Exception {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("reparse-xml");
        params.outputFileEncodings = new HashSet<>();
        params.outputFileEncodings.add(IOUtils.Encoding.XML);

        generator.generatePackage(params);

        File bundleFile = new File(params.outputPath + "/tes-content-bundle.xml");

        try (FileInputStream fis = new FileInputStream(bundleFile)) {
            Bundle reparsed = (Bundle) ((XmlParser) fhirContext.newXmlParser())
                    .parseResource(fis);

            Assert.assertNotNull(reparsed, "Re-parsed bundle should not be null");
            Assert.assertFalse(reparsed.getEntry().isEmpty(), "Re-parsed bundle should have entries");
        }
    }

    // ===========================================================================
    // 23. Condition Grouper ValueSets carry the expected profile in meta
    // ===========================================================================
    @Test
    public void generateTESPackage_conditionGroupers_haveExpectedProfile() {
        TESPackageGenerator generator = new TESPackageGenerator();
        TESPackageGenerateParameters params = baseParams("cg-profile");

        Bundle result = generator.generatePackage(params);

        List<ValueSet> conditionGroupers = result.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(r -> r instanceof ValueSet)
                .map(r -> (ValueSet) r)
                .filter(vs -> vs.getUseContext().stream()
                        .anyMatch(uc -> uc.hasValueCodeableConcept()
                                && "condition-grouper".equals(
                                uc.getValueCodeableConcept().getCodingFirstRep().getCode())))
                .collect(Collectors.toList());

        Assert.assertFalse(conditionGroupers.isEmpty());
        for (ValueSet vs : conditionGroupers) {
            boolean hasProfile = vs.getMeta().getProfile().stream()
                    .anyMatch(p -> p.getValue().contains("vsm-conditiongroupervalueset"));
            Assert.assertTrue(
                    hasProfile,
                    String.format("Condition Grouper '%s' should carry the vsm-conditiongroupervalueset profile",
                            vs.getTitle()));
        }
    }

    // ===========================================================================
    // 24. Generating a package twice with the same params is idempotent
    //     (entry counts should be the same)
    // ===========================================================================
    @Test
    public void generateTESPackage_isIdempotent() {
        TESPackageGenerator gen1 = new TESPackageGenerator();
        TESPackageGenerateParameters params1 = baseParams("idempotent-run1");
        Bundle result1 = gen1.generatePackage(params1);

        TESPackageGenerator gen2 = new TESPackageGenerator();
        TESPackageGenerateParameters params2 = baseParams("idempotent-run2");
        Bundle result2 = gen2.generatePackage(params2);

        Assert.assertEquals(
                result1.getEntry().size(),
                result2.getEntry().size(),
                "Two runs with identical parameters should produce the same number of bundle entries");
    }
}