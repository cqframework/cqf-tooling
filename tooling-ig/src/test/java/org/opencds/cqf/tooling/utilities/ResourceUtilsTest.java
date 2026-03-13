package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.tooling.utilities.ResourceUtils.FhirVersion;
import org.testng.annotations.Test;

public class ResourceUtilsTest {

    private static final FhirContext R4 = FhirContext.forR4Cached();
    private static final FhirContext DSTU3 = FhirContext.forDstu3Cached();

    // ── FhirVersion.parse ──

    @Test
    public void fhirVersionParse_dstu3_returnsDstu3() {
        assertEquals(FhirVersion.parse("dstu3"), FhirVersion.DSTU3);
    }

    @Test
    public void fhirVersionParse_r4_returnsR4() {
        assertEquals(FhirVersion.parse("r4"), FhirVersion.R4);
    }

    @Test
    public void fhirVersionParse_uppercase_throws() {
        // Intent: version parsing is case-sensitive — "R4" does not match "r4"
        assertThrows(RuntimeException.class, () -> FhirVersion.parse("R4"));
    }

    @Test
    public void fhirVersionParse_unknown_throws() {
        assertThrows(RuntimeException.class, () -> FhirVersion.parse("r5"));
    }

    @Test
    public void fhirVersionToString_matchesCode() {
        assertEquals(FhirVersion.DSTU3.toString(), "dstu3");
        assertEquals(FhirVersion.R4.toString(), "r4");
    }

    // ── getFhirContext ──

    @Test
    public void getFhirContext_dstu3_returnsDstu3Context() {
        FhirContext ctx = ResourceUtils.getFhirContext(FhirVersion.DSTU3);
        assertNotNull(ctx);
        assertEquals(ctx.getVersion().getVersion().name(), "DSTU3");
    }

    @Test
    public void getFhirContext_r4_returnsR4Context() {
        FhirContext ctx = ResourceUtils.getFhirContext(FhirVersion.R4);
        assertNotNull(ctx);
        assertEquals(ctx.getVersion().getVersion().name(), "R4");
    }

    // ── getId ──

    @Test
    public void getId_versioned_includesVersionWithDots() {
        String result = ResourceUtils.getId("my-lib", "1_0_0", true);
        assertEquals(result, "my-lib-1.0.0");
    }

    @Test
    public void getId_unversioned_excludesVersion() {
        String result = ResourceUtils.getId("my-lib", "1.0.0", false);
        assertEquals(result, "my-lib");
    }

    @Test
    public void getId_underscoresInName_replacedWithDashes() {
        String result = ResourceUtils.getId("my_cool_lib", "1.0", false);
        assertEquals(result, "my-cool-lib");
    }

    @Test
    public void getId_versionedWithNullVersion_omitsVersion() {
        // Fixed: null version with versioned=true no longer throws NPE, just omits version
        String result = ResourceUtils.getId("my-lib", null, true);
        assertEquals(result, "my-lib");
    }

    @Test
    public void getId_unversionedWithNullVersion_succeeds() {
        // Unversioned should work fine even with null version since it's not used
        String result = ResourceUtils.getId("my-lib", null, false);
        assertEquals(result, "my-lib");
    }

    // ── setIgId ──

    @Test
    public void setIgId_nonBundleResource_prefixesWithResourceType() {
        Patient patient = new Patient();
        ResourceUtils.setIgId("test-id", patient, false);
        // Non-bundle format: resourceName + "-" + baseId
        assertTrue(patient.getIdElement().getIdPart().startsWith("patient-"),
                "Non-bundle ID should start with lowercase resource type");
        assertTrue(patient.getIdElement().getIdPart().contains("test-id"));
    }

    @Test
    public void setIgId_bundleResource_suffixesWithResourceType() {
        Bundle bundle = new Bundle();
        ResourceUtils.setIgId("test-id", bundle, false);
        // Bundle format: baseId + "-" + resourceName
        String id = bundle.getIdElement().getIdPart();
        assertTrue(id.startsWith("test-id"), "Bundle ID should start with baseId");
        assertTrue(id.endsWith("bundle"), "Bundle ID should end with 'bundle'");
    }

    @Test
    public void setIgId_withVersion_includesVersionInId() {
        Patient patient = new Patient();
        ResourceUtils.setIgId("test-id", patient, "2.0");
        String id = patient.getIdElement().getIdPart();
        assertTrue(id.contains("2.0"), "ID should contain version");
    }

    @Test
    public void setIgId_withNullVersion_noVersionSuffix() {
        Patient patient = new Patient();
        ResourceUtils.setIgId("test-id", patient, (String) null);
        String id = patient.getIdElement().getIdPart();
        assertEquals(id, "patient-test-id");
    }

    @Test
    public void setIgId_underscoresReplacedWithDashes() {
        Patient patient = new Patient();
        ResourceUtils.setIgId("test_id_here", patient, false);
        String id = patient.getIdElement().getIdPart();
        assertFalse(id.contains("_"), "Underscores should be replaced with dashes");
    }

    @Test
    public void setIgId_dstu3Bundle_suffixesWithResourceType() {
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        ResourceUtils.setIgId("test-id", bundle, false);
        String id = bundle.getIdElement().getIdPart();
        assertTrue(id.startsWith("test-id"));
        assertTrue(id.endsWith("bundle"));
    }

    // ── getUrl ──

    @Test
    public void getUrl_r4Library_returnsUrl() {
        Library library = new Library();
        library.setUrl("http://example.org/Library/test");
        assertEquals(ResourceUtils.getUrl(library, R4), "http://example.org/Library/test");
    }

    @Test
    public void getUrl_r4Measure_returnsUrl() {
        Measure measure = new Measure();
        measure.setUrl("http://example.org/Measure/test");
        assertEquals(ResourceUtils.getUrl(measure, R4), "http://example.org/Measure/test");
    }

    @Test
    public void getUrl_dstu3Library_returnsUrl() {
        org.hl7.fhir.dstu3.model.Library library = new org.hl7.fhir.dstu3.model.Library();
        library.setUrl("http://example.org/Library/test");
        assertEquals(ResourceUtils.getUrl(library, DSTU3), "http://example.org/Library/test");
    }

    @Test
    public void getUrl_r4UnsupportedType_throws() {
        // Patient doesn't have a canonical URL — should throw
        Patient patient = new Patient();
        assertThrows(IllegalArgumentException.class,
                () -> ResourceUtils.getUrl(patient, R4));
    }

    @Test
    public void getUrl_dstu3UnsupportedType_throws() {
        org.hl7.fhir.dstu3.model.Patient patient = new org.hl7.fhir.dstu3.model.Patient();
        assertThrows(IllegalArgumentException.class,
                () -> ResourceUtils.getUrl(patient, DSTU3));
    }

    @Test
    public void getUrl_r4LibraryNoUrl_returnsNull() {
        Library library = new Library();
        // url not set
        assertNull(ResourceUtils.getUrl(library, R4));
    }

    // ── getVersion ──

    @Test
    public void getVersion_r4LibraryWithVersion_returnsVersion() {
        Library library = new Library();
        library.setVersion("1.2.3");
        assertEquals(ResourceUtils.getVersion(library, R4), "1.2.3");
    }

    @Test
    public void getVersion_r4LibraryNoVersion_returnsNull() {
        Library library = new Library();
        assertNull(ResourceUtils.getVersion(library, R4));
    }

    @Test
    public void getVersion_dstu3LibraryWithVersion_returnsVersion() {
        org.hl7.fhir.dstu3.model.Library library = new org.hl7.fhir.dstu3.model.Library();
        library.setVersion("2.0.0");
        assertEquals(ResourceUtils.getVersion(library, DSTU3), "2.0.0");
    }

    @Test
    public void getVersion_r4PatientNoVersionField_returnsNull() {
        // Patient doesn't have a "version" field — should return null gracefully
        Patient patient = new Patient();
        assertNull(ResourceUtils.getVersion(patient, R4));
    }

    // ── getName ──

    @Test
    public void getName_r4Library_returnsName() {
        Library library = new Library();
        library.setName("TestLibrary");
        assertEquals(ResourceUtils.getName(library, R4), "TestLibrary");
    }

    @Test
    public void getName_dstu3Measure_returnsName() {
        org.hl7.fhir.dstu3.model.Measure measure = new org.hl7.fhir.dstu3.model.Measure();
        measure.setName("TestMeasure");
        assertEquals(ResourceUtils.getName(measure, DSTU3), "TestMeasure");
    }

    @Test
    public void getName_r4UnsupportedType_throws() {
        Patient patient = new Patient();
        assertThrows(IllegalArgumentException.class,
                () -> ResourceUtils.getName(patient, R4));
    }

    // ── compareResourcePrimitiveElements ──

    @Test
    public void compareResourcePrimitiveElements_sameValues_returnsTrue() {
        Library lib1 = new Library();
        lib1.setUrl("http://example.org/Library/test");
        lib1.setVersion("1.0");

        Library lib2 = new Library();
        lib2.setUrl("http://example.org/Library/test");
        lib2.setVersion("1.0");

        assertTrue(ResourceUtils.compareResourcePrimitiveElements(
                lib1, lib2, R4, "url", "version"));
    }

    @Test
    public void compareResourcePrimitiveElements_differentValues_returnsFalse() {
        Library lib1 = new Library();
        lib1.setUrl("http://example.org/Library/test");
        lib1.setVersion("1.0");

        Library lib2 = new Library();
        lib2.setUrl("http://example.org/Library/test");
        lib2.setVersion("2.0");

        assertFalse(ResourceUtils.compareResourcePrimitiveElements(
                lib1, lib2, R4, "url", "version"));
    }

    @Test
    public void compareResourcePrimitiveElements_nullRes1_returnsFalse() {
        Library lib = new Library();
        assertFalse(ResourceUtils.compareResourcePrimitiveElements(null, lib, R4, "url"));
    }

    @Test
    public void compareResourcePrimitiveElements_nullRes2_returnsFalse() {
        Library lib = new Library();
        assertFalse(ResourceUtils.compareResourcePrimitiveElements(lib, null, R4, "url"));
    }

    @Test
    public void compareResourcePrimitiveElements_bothNull_returnsFalse() {
        assertFalse(ResourceUtils.compareResourcePrimitiveElements(null, null, R4, "url"));
    }

    @Test
    public void compareResourcePrimitiveElements_differentTypes_returnsFalse() {
        Library lib = new Library();
        lib.setUrl("http://example.org/Library/test");

        Measure measure = new Measure();
        measure.setUrl("http://example.org/Library/test");

        assertFalse(ResourceUtils.compareResourcePrimitiveElements(
                lib, measure, R4, "url"));
    }

    @Test
    public void compareResourcePrimitiveElements_oneHasValueOtherDoesNot_returnsFalse() {
        // Fixed: when one resource has a value and the other doesn't, returns false
        Library lib1 = new Library();
        lib1.setUrl("http://example.org/Library/test");

        Library lib2 = new Library();
        // url not set on lib2

        boolean result = ResourceUtils.compareResourcePrimitiveElements(
                lib1, lib2, R4, "url");
        assertFalse(result,
                "Should return false when one resource has a value and the other doesn't");
    }

    @Test
    public void compareResourcePrimitiveElements_noElements_returnsTrue() {
        Library lib1 = new Library();
        Library lib2 = new Library();
        // No elements to compare — vacuously true
        assertTrue(ResourceUtils.compareResourcePrimitiveElements(lib1, lib2, R4));
    }

    // ── compareResourceIdUrlAndVersion ──

    @Test
    public void compareResourceIdUrlAndVersion_matching_returnsTrue() {
        Library lib1 = new Library();
        lib1.setId("Library/test");
        lib1.setUrl("http://example.org/Library/test");
        lib1.setVersion("1.0");

        Library lib2 = new Library();
        lib2.setId("Library/test");
        lib2.setUrl("http://example.org/Library/test");
        lib2.setVersion("1.0");

        assertTrue(ResourceUtils.compareResourceIdUrlAndVersion(lib1, lib2, R4));
    }

    @Test
    public void compareResourceIdUrlAndVersion_differentVersion_returnsFalse() {
        Library lib1 = new Library();
        lib1.setId("Library/test");
        lib1.setUrl("http://example.org/Library/test");
        lib1.setVersion("1.0");

        Library lib2 = new Library();
        lib2.setId("Library/test");
        lib2.setUrl("http://example.org/Library/test");
        lib2.setVersion("2.0");

        assertFalse(ResourceUtils.compareResourceIdUrlAndVersion(lib1, lib2, R4));
    }

    // ── getPrimaryLibraryUrl ──

    @Test
    public void getPrimaryLibraryUrl_r4MeasureWithOneLibrary_returnsUrl() {
        Measure measure = new Measure();
        measure.addLibrary("http://example.org/Library/TestLib");

        String url = ResourceUtils.getPrimaryLibraryUrl(measure, R4);
        assertEquals(url, "http://example.org/Library/TestLib");
    }

    @Test
    public void getPrimaryLibraryUrl_r4MeasureNoLibrary_throws() {
        Measure measure = new Measure();
        assertThrows(IllegalArgumentException.class,
                () -> ResourceUtils.getPrimaryLibraryUrl(measure, R4));
    }

    @Test
    public void getPrimaryLibraryUrl_r4MeasureTwoLibraries_throws() {
        Measure measure = new Measure();
        measure.addLibrary("http://example.org/Library/Lib1");
        measure.addLibrary("http://example.org/Library/Lib2");
        assertThrows(IllegalArgumentException.class,
                () -> ResourceUtils.getPrimaryLibraryUrl(measure, R4));
    }

    @Test
    public void getPrimaryLibraryUrl_dstu3MeasureWithOneLibrary_returnsName() {
        org.hl7.fhir.dstu3.model.Measure measure = new org.hl7.fhir.dstu3.model.Measure();
        measure.addLibrary().setReference("Library/TestLib");

        String url = ResourceUtils.getPrimaryLibraryUrl(measure, DSTU3);
        assertEquals(url, "TestLib");
    }

    @Test
    public void getPrimaryLibraryUrl_r4QuestionnaireNoCqfLibrary_returnsNull() {
        org.hl7.fhir.r4.model.Questionnaire questionnaire = new org.hl7.fhir.r4.model.Questionnaire();
        String url = ResourceUtils.getPrimaryLibraryUrl(questionnaire, R4);
        assertNull(url);
    }

    @Test
    public void getPrimaryLibraryUrl_r4PlanDefinitionWithOneLibrary_returnsUrl() {
        org.hl7.fhir.r4.model.PlanDefinition pd = new org.hl7.fhir.r4.model.PlanDefinition();
        pd.addLibrary("http://example.org/Library/PlanLib");

        String url = ResourceUtils.getPrimaryLibraryUrl(pd, R4);
        assertEquals(url, "http://example.org/Library/PlanLib");
    }

    // ── getPrimaryLibraryName ──

    @Test
    public void getPrimaryLibraryName_r4Measure_returnsTailOfUrl() {
        Measure measure = new Measure();
        measure.addLibrary("http://example.org/Library/TestLib");

        String name = ResourceUtils.getPrimaryLibraryName(measure, R4);
        assertEquals(name, "TestLib");
    }

    @Test
    public void getPrimaryLibraryName_r4NonMeasure_throwsIllegalArgument() {
        // Fixed: getPrimaryLibraryName now checks type before casting
        Library library = new Library();
        assertThrows(IllegalArgumentException.class,
                () -> ResourceUtils.getPrimaryLibraryName(library, R4));
    }

    @Test
    public void getPrimaryLibraryName_dstu3Measure_returnsTailOfReference() {
        org.hl7.fhir.dstu3.model.Measure measure = new org.hl7.fhir.dstu3.model.Measure();
        measure.addLibrary().setReference("Library/TestLib");

        String name = ResourceUtils.getPrimaryLibraryName(measure, DSTU3);
        assertEquals(name, "TestLib");
    }

    // ── resolveProperty ──

    @Test
    public void resolveProperty_nullTarget_returnsNull() {
        assertNull(ResourceUtils.resolveProperty(null, "url", R4));
    }

    @Test
    public void resolveProperty_resourceWithValue_returnsValue() {
        Library library = new Library();
        library.setUrl("http://example.org/Library/test");

        Object result = ResourceUtils.resolveProperty(library, "url", R4);
        assertNotNull(result);
    }

    @Test
    public void resolveProperty_resourceWithNoValue_returnsNull() {
        Library library = new Library();
        // url not set
        Object result = ResourceUtils.resolveProperty(library, "url", R4);
        assertNull(result);
    }

    // ── getResourceDefinition ──

    @Test
    public void getResourceDefinition_patient_returnsDefinition() {
        RuntimeResourceDefinition def = ResourceUtils.getResourceDefinition(R4, "Patient");
        assertNotNull(def);
        assertEquals(def.getName(), "Patient");
    }

    @Test
    public void getResourceDefinition_bundle_returnsDefinition() {
        RuntimeResourceDefinition def = ResourceUtils.getResourceDefinition(R4, "Bundle");
        assertNotNull(def);
        assertEquals(def.getName(), "Bundle");
    }

    // ── getR4Dependencies ──

    @Test
    public void getR4Dependencies_emptyList_returnsEmpty() {
        java.util.List<org.hl7.fhir.r4.model.RelatedArtifact> artifacts = new java.util.ArrayList<>();
        java.util.List<String> deps = ResourceUtils.getR4Dependencies(artifacts);
        assertTrue(deps.isEmpty());
    }

    @Test
    public void getR4Dependencies_dependsOnWithResource_returnsUrl() {
        java.util.List<org.hl7.fhir.r4.model.RelatedArtifact> artifacts = new java.util.ArrayList<>();
        org.hl7.fhir.r4.model.RelatedArtifact artifact = new org.hl7.fhir.r4.model.RelatedArtifact();
        artifact.setType(org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DEPENDSON);
        artifact.setResource("http://example.org/Library/dep");
        artifacts.add(artifact);

        java.util.List<String> deps = ResourceUtils.getR4Dependencies(artifacts);
        assertEquals(deps.size(), 1);
        assertEquals(deps.get(0), "http://example.org/Library/dep");
    }

    @Test
    public void getR4Dependencies_nonDependsOn_excluded() {
        java.util.List<org.hl7.fhir.r4.model.RelatedArtifact> artifacts = new java.util.ArrayList<>();
        org.hl7.fhir.r4.model.RelatedArtifact artifact = new org.hl7.fhir.r4.model.RelatedArtifact();
        artifact.setType(org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.CITATION);
        artifact.setResource("http://example.org/Library/cited");
        artifacts.add(artifact);

        java.util.List<String> deps = ResourceUtils.getR4Dependencies(artifacts);
        assertTrue(deps.isEmpty());
    }

    @Test
    public void getR4Dependencies_dependsOnNoResource_excluded() {
        java.util.List<org.hl7.fhir.r4.model.RelatedArtifact> artifacts = new java.util.ArrayList<>();
        org.hl7.fhir.r4.model.RelatedArtifact artifact = new org.hl7.fhir.r4.model.RelatedArtifact();
        artifact.setType(org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DEPENDSON);
        // resource not set
        artifacts.add(artifact);

        java.util.List<String> deps = ResourceUtils.getR4Dependencies(artifacts);
        assertTrue(deps.isEmpty());
    }

    // ── getDependencies ──

    @Test
    public void getDependencies_r4Library_returnsDependsOn() {
        Library library = new Library();
        org.hl7.fhir.r4.model.RelatedArtifact artifact = new org.hl7.fhir.r4.model.RelatedArtifact();
        artifact.setType(org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DEPENDSON);
        artifact.setResource("http://example.org/Library/dep");
        library.addRelatedArtifact(artifact);

        java.util.List<String> deps = ResourceUtils.getDependencies(library, R4);
        assertEquals(deps.size(), 1);
    }

    @Test
    public void getDependencies_r4Patient_throws() {
        Patient patient = new Patient();
        assertThrows(IllegalArgumentException.class,
                () -> ResourceUtils.getDependencies(patient, R4));
    }

    // ── getTerminologyDependencies ──

    @Test
    public void getTerminologyDependencies_filtersToCodeSystemAndValueSet() {
        Library library = new Library();

        org.hl7.fhir.r4.model.RelatedArtifact vsArtifact = new org.hl7.fhir.r4.model.RelatedArtifact();
        vsArtifact.setType(org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DEPENDSON);
        vsArtifact.setResource("http://example.org/ValueSet/vs1");
        library.addRelatedArtifact(vsArtifact);

        org.hl7.fhir.r4.model.RelatedArtifact libArtifact = new org.hl7.fhir.r4.model.RelatedArtifact();
        libArtifact.setType(org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DEPENDSON);
        libArtifact.setResource("http://example.org/Library/lib1");
        library.addRelatedArtifact(libArtifact);

        java.util.List<String> termDeps = ResourceUtils.getTerminologyDependencies(library, R4);
        assertEquals(termDeps.size(), 1);
        assertTrue(termDeps.get(0).contains("ValueSet"));
    }

    // ── getLibraryDependencies ──

    @Test
    public void getLibraryDependencies_filtersToLibrary() {
        Library library = new Library();

        org.hl7.fhir.r4.model.RelatedArtifact vsArtifact = new org.hl7.fhir.r4.model.RelatedArtifact();
        vsArtifact.setType(org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DEPENDSON);
        vsArtifact.setResource("http://example.org/ValueSet/vs1");
        library.addRelatedArtifact(vsArtifact);

        org.hl7.fhir.r4.model.RelatedArtifact libArtifact = new org.hl7.fhir.r4.model.RelatedArtifact();
        libArtifact.setType(org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DEPENDSON);
        libArtifact.setResource("http://example.org/Library/lib1");
        library.addRelatedArtifact(libArtifact);

        java.util.List<String> libDeps = ResourceUtils.getLibraryDependencies(library, R4);
        assertEquals(libDeps.size(), 1);
        assertTrue(libDeps.get(0).contains("Library"));
    }

    // ── cleanUp ──

    @Test
    public void cleanUp_doesNotThrow() {
        // cleanUp should be safe to call at any time
        ResourceUtils.cleanUp();
    }
}
