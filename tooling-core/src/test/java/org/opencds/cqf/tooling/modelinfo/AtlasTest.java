package org.opencds.cqf.tooling.modelinfo;

import static org.testng.Assert.*;

import ca.uhn.fhir.context.FhirContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class AtlasTest {

    private Path tempDir;

    @AfterMethod
    public void cleanup() throws IOException {
        if (tempDir != null && tempDir.toFile().exists()) {
            IOUtils.deleteDirectory(tempDir.toString());
        }
        IOUtils.cleanUp();
    }

    private void writeJsonResource(Path dir, String filename, Resource resource) throws IOException {
        String json = FhirContext.forR4Cached().newJsonParser().encodeResourceToString(resource);
        Files.writeString(dir.resolve(filename), json, StandardCharsets.UTF_8);
    }

    // ========== Constructor and getters ==========

    @Test
    public void testEmptyAtlasHasEmptyMaps() {
        Atlas atlas = new Atlas();
        assertTrue(atlas.getResources().isEmpty());
        assertTrue(atlas.getCapabilityStatements().isEmpty());
        assertTrue(atlas.getCompartmentDefinitions().isEmpty());
        assertTrue(atlas.getStructureDefinitions().isEmpty());
        assertTrue(atlas.getOperationDefinitions().isEmpty());
        assertTrue(atlas.getSearchParameters().isEmpty());
        assertTrue(atlas.getImplementationGuides().isEmpty());
        assertTrue(atlas.getCodeSystems().isEmpty());
        assertTrue(atlas.getValueSets().isEmpty());
        assertTrue(atlas.getConceptMaps().isEmpty());
        assertTrue(atlas.getNamingSystems().isEmpty());
        assertTrue(atlas.getParameters().isEmpty());
    }

    // ========== loadPaths and indexing ==========

    @Test
    public void testLoadStructureDefinition() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        StructureDefinition sd = new StructureDefinition();
        sd.setUrl("http://hl7.org/fhir/StructureDefinition/Patient");
        sd.setId("Patient");
        writeJsonResource(resourceDir, "StructureDefinition-Patient.json", sd);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getStructureDefinitions().size(), 1);
        assertNotNull(atlas.getStructureDefinitions().get("Patient"));
        assertEquals(atlas.getResources().size(), 1);
    }

    @Test
    public void testLoadValueSet() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ValueSet vs = new ValueSet();
        vs.setUrl("http://example.org/ValueSet/test-vs");
        vs.setId("test-vs");
        writeJsonResource(resourceDir, "ValueSet-test.json", vs);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getValueSets().size(), 1);
        assertNotNull(atlas.getValueSets().get("test-vs"));
    }

    @Test
    public void testLoadCodeSystem() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        CodeSystem cs = new CodeSystem();
        cs.setUrl("http://example.org/CodeSystem/test-cs");
        cs.setId("test-cs");
        writeJsonResource(resourceDir, "CodeSystem-test.json", cs);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getCodeSystems().size(), 1);
        assertNotNull(atlas.getCodeSystems().get("test-cs"));
    }

    @Test
    public void testLoadCapabilityStatement() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        CapabilityStatement cs = new CapabilityStatement();
        cs.setUrl("http://example.org/CapabilityStatement/test");
        cs.setId("test");
        writeJsonResource(resourceDir, "CapabilityStatement-test.json", cs);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getCapabilityStatements().size(), 1);
    }

    @Test
    public void testLoadOperationDefinition() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        OperationDefinition od = new OperationDefinition();
        od.setUrl("http://example.org/OperationDefinition/test");
        od.setId("test");
        writeJsonResource(resourceDir, "OperationDefinition-test.json", od);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getOperationDefinitions().size(), 1);
    }

    @Test
    public void testLoadSearchParameter() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        SearchParameter sp = new SearchParameter();
        sp.setUrl("http://example.org/SearchParameter/test");
        sp.setId("test");
        sp.setName("testParam");
        sp.addBase("Patient");
        writeJsonResource(resourceDir, "SearchParameter-test.json", sp);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getSearchParameters().size(), 1);
    }

    @Test
    public void testLoadImplementationGuide() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ImplementationGuide ig = new ImplementationGuide();
        ig.setUrl("http://example.org/ImplementationGuide/test");
        ig.setId("test");
        writeJsonResource(resourceDir, "ImplementationGuide-test.json", ig);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getImplementationGuides().size(), 1);
    }

    @Test
    public void testLoadConceptMap() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ConceptMap cm = new ConceptMap();
        cm.setUrl("http://example.org/ConceptMap/test");
        cm.setId("test");
        writeJsonResource(resourceDir, "ConceptMap-test.json", cm);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getConceptMaps().size(), 1);
    }

    @Test
    public void testLoadCompartmentDefinition() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        CompartmentDefinition cd = new CompartmentDefinition();
        cd.setUrl("http://example.org/CompartmentDefinition/test");
        cd.setId("test");
        writeJsonResource(resourceDir, "CompartmentDefinition-test.json", cd);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getCompartmentDefinitions().size(), 1);
    }

    @Test
    public void testLoadNamingSystem() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        NamingSystem ns = new NamingSystem();
        ns.setUrl("http://example.org/NamingSystem/test");
        ns.setId("test");
        ns.setName("TestNS");
        ns.setStatus(Enumerations.PublicationStatus.ACTIVE);
        ns.setKind(NamingSystem.NamingSystemType.CODESYSTEM);
        ns.addUniqueId().setType(NamingSystem.NamingSystemIdentifierType.URI).setValue("urn:test");
        writeJsonResource(resourceDir, "NamingSystem-test.json", ns);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getNamingSystems().size(), 1);
    }

    @Test
    public void testLoadParameters() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        Parameters params = new Parameters();
        params.setId("test-params");
        params.addParameter().setName("key").setValue(new StringType("value"));
        writeJsonResource(resourceDir, "Parameters-test.json", params);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getParameters().size(), 1);
        // Parameters.getId() may include the resource type prefix after round-trip
        assertFalse(atlas.getParameters().isEmpty(), "Parameters should be indexed");
    }

    // ========== Bundle unrolling ==========

    @Test
    public void testLoadBundleUnrollsEntries() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ValueSet vs = new ValueSet();
        vs.setUrl("http://example.org/ValueSet/bundled-vs");
        vs.setId("bundled-vs");

        CodeSystem cs = new CodeSystem();
        cs.setUrl("http://example.org/CodeSystem/bundled-cs");
        cs.setId("bundled-cs");

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);
        bundle.addEntry().setResource(vs);
        bundle.addEntry().setResource(cs);

        writeJsonResource(resourceDir, "Bundle-mixed.json", bundle);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getValueSets().size(), 1, "Bundle entry ValueSet should be indexed");
        assertEquals(atlas.getCodeSystems().size(), 1, "Bundle entry CodeSystem should be indexed");
    }

    @Test
    public void testLoadNestedBundleUnrollsRecursively() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ValueSet vs = new ValueSet();
        vs.setUrl("http://example.org/ValueSet/nested-vs");
        vs.setId("nested-vs");

        Bundle innerBundle = new Bundle();
        innerBundle.setType(Bundle.BundleType.COLLECTION);
        innerBundle.addEntry().setResource(vs);

        Bundle outerBundle = new Bundle();
        outerBundle.setType(Bundle.BundleType.COLLECTION);
        outerBundle.addEntry().setResource(innerBundle);

        writeJsonResource(resourceDir, "Bundle-nested.json", outerBundle);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getValueSets().size(), 1,
                "Nested bundle should be recursively unrolled to find inner resources");
    }

    // ========== Duplicate handling ==========

    @Test
    public void testDuplicateUrlIsSkipped() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        StructureDefinition sd1 = new StructureDefinition();
        sd1.setUrl("http://example.org/StructureDefinition/Dup");
        sd1.setId("Dup");
        sd1.setName("First");

        StructureDefinition sd2 = new StructureDefinition();
        sd2.setUrl("http://example.org/StructureDefinition/Dup");
        sd2.setId("Dup");
        sd2.setName("Second");

        writeJsonResource(resourceDir, "SD-dup1.json", sd1);
        writeJsonResource(resourceDir, "SD-dup2.json", sd2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getStructureDefinitions().size(), 1,
                "Duplicate URLs should not create duplicate entries");
        assertEquals(atlas.getResources().size(), 1);
    }

    @Test
    public void testDuplicateParametersIdIsSkipped() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        Parameters p1 = new Parameters();
        p1.setId("same-id");
        p1.addParameter().setName("first").setValue(new StringType("1"));

        Parameters p2 = new Parameters();
        p2.setId("same-id");
        p2.addParameter().setName("second").setValue(new StringType("2"));

        writeJsonResource(resourceDir, "Params1.json", p1);
        writeJsonResource(resourceDir, "Params2.json", p2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getParameters().size(), 1, "Duplicate Parameters IDs should be skipped");
    }

    // ========== Multiple paths ==========

    @Test
    public void testLoadMultiplePaths() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path dir1 = Files.createDirectory(tempDir.resolve("path1"));
        Path dir2 = Files.createDirectory(tempDir.resolve("path2"));

        StructureDefinition sd = new StructureDefinition();
        sd.setUrl("http://example.org/StructureDefinition/A");
        sd.setId("A");
        writeJsonResource(dir1, "SD-A.json", sd);

        ValueSet vs = new ValueSet();
        vs.setUrl("http://example.org/ValueSet/B");
        vs.setId("B");
        writeJsonResource(dir2, "VS-B.json", vs);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "path1;path2");

        assertEquals(atlas.getStructureDefinitions().size(), 1);
        assertEquals(atlas.getValueSets().size(), 1);
    }

    // ========== resolveSearchParameter ==========

    @Test
    public void testResolveSearchParameterFound() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        SearchParameter sp = new SearchParameter();
        sp.setUrl("http://example.org/SearchParameter/patient-name");
        sp.setId("patient-name");
        sp.setName("name");
        sp.addBase("Patient");
        writeJsonResource(resourceDir, "SP-name.json", sp);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        SearchParameter resolved = atlas.resolveSearchParameter("Patient", "name");
        assertNotNull(resolved);
        assertEquals(resolved.getName(), "name");
    }

    @Test
    public void testResolveSearchParameterNotFound() {
        Atlas atlas = new Atlas();
        SearchParameter result = atlas.resolveSearchParameter("Patient", "nonexistent");
        assertNull(result);
    }

    @Test
    public void testResolveSearchParameterWrongResourceType() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        SearchParameter sp = new SearchParameter();
        sp.setUrl("http://example.org/SearchParameter/obs-code");
        sp.setId("obs-code");
        sp.setName("code");
        sp.addBase("Observation");
        writeJsonResource(resourceDir, "SP-code.json", sp);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        // Searching for Patient should not find an Observation-only search param
        assertNull(atlas.resolveSearchParameter("Patient", "code"));
        // But Observation should work
        assertNotNull(atlas.resolveSearchParameter("Observation", "code"));
    }

    // ========== Unrecognized resource type is skipped ==========

    @Test
    public void testUnrecognizedResourceTypeSkipped() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        // Patient is not one of the indexed types — should be skipped
        Patient patient = new Patient();
        patient.setId("test-patient");
        writeJsonResource(resourceDir, "Patient-test.json", patient);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertTrue(atlas.getResources().isEmpty(), "Patient is not an indexed conformance type");
    }

    // ========== Duplicate URL for ValueSet ==========

    @Test
    public void testDuplicateValueSetUrl() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ValueSet vs1 = new ValueSet();
        vs1.setUrl("http://example.org/ValueSet/dup");
        writeJsonResource(resourceDir, "VS-1.json", vs1);

        ValueSet vs2 = new ValueSet();
        vs2.setUrl("http://example.org/ValueSet/dup"); // same URL
        writeJsonResource(resourceDir, "VS-2.json", vs2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        // Same URL means the second is treated as a duplicate
        assertEquals(atlas.getValueSets().size(), 1);
        assertEquals(atlas.getResources().size(), 1, "Duplicate URL should not be added to resources");
    }

    // ========== Duplicate tail-ID but different URL ==========

    @Test
    public void testDuplicateTailIdDifferentUrl() throws IOException {
        // Two resources with different URLs but the same tail ID (last segment of URL)
        // Atlas keys the type-specific map by CanonicalUtils.getTail(url),
        // so same tail = duplicate even if the full URLs differ
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ValueSet vs1 = new ValueSet();
        vs1.setUrl("http://example.org/ValueSet/my-vs");
        vs1.setId("my-vs");
        vs1.setName("First");

        ValueSet vs2 = new ValueSet();
        vs2.setUrl("http://other.org/ValueSet/my-vs"); // different URL, same tail "my-vs"
        vs2.setId("my-vs-alt");
        vs2.setName("Second");

        writeJsonResource(resourceDir, "VS-1.json", vs1);
        writeJsonResource(resourceDir, "VS-2.json", vs2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        // Both have different full URLs, so both go into the resources map
        assertEquals(atlas.getResources().size(), 2);
        // Both have the same tail ID ("my-vs"), so the second is treated as a duplicate
        // and only the first is kept in the valueSets map
        assertEquals(atlas.getValueSets().size(), 1);
    }

    @Test
    public void testDuplicateCapabilityStatementId() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        CapabilityStatement cs1 = new CapabilityStatement();
        cs1.setUrl("http://example.org/CapabilityStatement/dup");
        writeJsonResource(resourceDir, "CS-1.json", cs1);

        CapabilityStatement cs2 = new CapabilityStatement();
        cs2.setUrl("http://example.org/CapabilityStatement/dup");
        writeJsonResource(resourceDir, "CS-2.json", cs2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");
        assertEquals(atlas.getCapabilityStatements().size(), 1);
    }

    @Test
    public void testDuplicateCompartmentDefinitionId() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        CompartmentDefinition cd1 = new CompartmentDefinition();
        cd1.setUrl("http://example.org/CompartmentDefinition/dup");
        writeJsonResource(resourceDir, "CD-1.json", cd1);

        CompartmentDefinition cd2 = new CompartmentDefinition();
        cd2.setUrl("http://example.org/CompartmentDefinition/dup");
        writeJsonResource(resourceDir, "CD-2.json", cd2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");
        assertEquals(atlas.getCompartmentDefinitions().size(), 1);
    }

    @Test
    public void testDuplicateOperationDefinitionId() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        OperationDefinition od1 = new OperationDefinition();
        od1.setUrl("http://example.org/OperationDefinition/dup");
        writeJsonResource(resourceDir, "OD-1.json", od1);

        OperationDefinition od2 = new OperationDefinition();
        od2.setUrl("http://example.org/OperationDefinition/dup");
        writeJsonResource(resourceDir, "OD-2.json", od2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");
        assertEquals(atlas.getOperationDefinitions().size(), 1);
    }

    @Test
    public void testDuplicateSearchParameterId() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        SearchParameter sp1 = new SearchParameter();
        sp1.setUrl("http://example.org/SearchParameter/dup");
        sp1.setName("dup");
        sp1.addBase("Patient");
        writeJsonResource(resourceDir, "SP-1.json", sp1);

        SearchParameter sp2 = new SearchParameter();
        sp2.setUrl("http://example.org/SearchParameter/dup");
        sp2.setName("dup");
        sp2.addBase("Patient");
        writeJsonResource(resourceDir, "SP-2.json", sp2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");
        assertEquals(atlas.getSearchParameters().size(), 1);
    }

    @Test
    public void testDuplicateImplementationGuideId() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ImplementationGuide ig1 = new ImplementationGuide();
        ig1.setUrl("http://example.org/ImplementationGuide/dup");
        writeJsonResource(resourceDir, "IG-1.json", ig1);

        ImplementationGuide ig2 = new ImplementationGuide();
        ig2.setUrl("http://example.org/ImplementationGuide/dup");
        writeJsonResource(resourceDir, "IG-2.json", ig2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");
        assertEquals(atlas.getImplementationGuides().size(), 1);
    }

    @Test
    public void testDuplicateCodeSystemId() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        CodeSystem cs1 = new CodeSystem();
        cs1.setUrl("http://example.org/CodeSystem/dup");
        writeJsonResource(resourceDir, "CS-1.json", cs1);

        CodeSystem cs2 = new CodeSystem();
        cs2.setUrl("http://example.org/CodeSystem/dup");
        writeJsonResource(resourceDir, "CS-2.json", cs2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");
        assertEquals(atlas.getCodeSystems().size(), 1);
    }

    @Test
    public void testDuplicateConceptMapId() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ConceptMap cm1 = new ConceptMap();
        cm1.setUrl("http://example.org/ConceptMap/dup");
        writeJsonResource(resourceDir, "CM-1.json", cm1);

        ConceptMap cm2 = new ConceptMap();
        cm2.setUrl("http://example.org/ConceptMap/dup");
        writeJsonResource(resourceDir, "CM-2.json", cm2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");
        assertEquals(atlas.getConceptMaps().size(), 1);
    }

    @Test
    public void testDuplicateNamingSystemId() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        NamingSystem ns1 = new NamingSystem();
        ns1.setUrl("http://example.org/NamingSystem/dup");
        ns1.setName("DupNS");
        ns1.setStatus(Enumerations.PublicationStatus.ACTIVE);
        ns1.setKind(NamingSystem.NamingSystemType.CODESYSTEM);
        ns1.addUniqueId().setType(NamingSystem.NamingSystemIdentifierType.URI).setValue("urn:dup");
        writeJsonResource(resourceDir, "NS-1.json", ns1);

        NamingSystem ns2 = new NamingSystem();
        ns2.setUrl("http://example.org/NamingSystem/dup");
        ns2.setName("DupNS2");
        ns2.setStatus(Enumerations.PublicationStatus.ACTIVE);
        ns2.setKind(NamingSystem.NamingSystemType.CODESYSTEM);
        ns2.addUniqueId().setType(NamingSystem.NamingSystemIdentifierType.URI).setValue("urn:dup2");
        writeJsonResource(resourceDir, "NS-2.json", ns2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");
        assertEquals(atlas.getNamingSystems().size(), 1);
    }

    // ========== Bundle edge cases ==========

    @Test
    public void testEmptyBundleProducesNoResources() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        // Bundle with no entries
        Bundle emptyBundle = new Bundle();
        emptyBundle.setType(Bundle.BundleType.COLLECTION);
        writeJsonResource(resourceDir, "empty-bundle.json", emptyBundle);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertTrue(atlas.getResources().isEmpty(), "Empty bundle should produce no indexed resources");
    }

    @Test
    public void testBundleWithEntryMissingResource() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        // Bundle with an entry that has no resource (just a request)
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);
        Bundle.BundleEntryComponent entry = bundle.addEntry();
        entry.setFullUrl("http://example.org/missing");
        // no entry.setResource(...) — the entry has no resource

        ValueSet vs = new ValueSet();
        vs.setUrl("http://example.org/ValueSet/real");
        Bundle.BundleEntryComponent goodEntry = bundle.addEntry();
        goodEntry.setResource(vs);

        writeJsonResource(resourceDir, "mixed-bundle.json", bundle);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        // Only the good entry should be indexed
        assertEquals(atlas.getValueSets().size(), 1);
    }

    // ========== Duplicate tail ID (different URL) per type ==========

    @Test
    public void testDuplicateTailIdCapabilityStatement() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        CapabilityStatement cs1 = new CapabilityStatement();
        cs1.setUrl("http://example.org/CapabilityStatement/same-tail");
        writeJsonResource(resourceDir, "CS-A.json", cs1);

        CapabilityStatement cs2 = new CapabilityStatement();
        cs2.setUrl("http://other.org/CapabilityStatement/same-tail");
        writeJsonResource(resourceDir, "CS-B.json", cs2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getResources().size(), 2, "Different URLs should both be in resources");
        assertEquals(atlas.getCapabilityStatements().size(), 1, "Same tail ID should deduplicate");
    }

    @Test
    public void testDuplicateTailIdStructureDefinition() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        StructureDefinition sd1 = new StructureDefinition();
        sd1.setUrl("http://example.org/StructureDefinition/my-sd");
        writeJsonResource(resourceDir, "SD-A.json", sd1);

        StructureDefinition sd2 = new StructureDefinition();
        sd2.setUrl("http://other.org/StructureDefinition/my-sd");
        writeJsonResource(resourceDir, "SD-B.json", sd2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getResources().size(), 2);
        assertEquals(atlas.getStructureDefinitions().size(), 1, "Same tail should deduplicate");
    }

    @Test
    public void testDuplicateTailIdOperationDefinition() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        OperationDefinition od1 = new OperationDefinition();
        od1.setUrl("http://example.org/OperationDefinition/same");
        od1.setCode("op");
        writeJsonResource(resourceDir, "OD-A.json", od1);

        OperationDefinition od2 = new OperationDefinition();
        od2.setUrl("http://other.org/OperationDefinition/same");
        od2.setCode("op");
        writeJsonResource(resourceDir, "OD-B.json", od2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getResources().size(), 2);
        assertEquals(atlas.getOperationDefinitions().size(), 1);
    }

    @Test
    public void testDuplicateTailIdSearchParameter() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        SearchParameter sp1 = new SearchParameter();
        sp1.setUrl("http://example.org/SearchParameter/same");
        sp1.setName("Same");
        sp1.addBase("Patient");
        writeJsonResource(resourceDir, "SP-A.json", sp1);

        SearchParameter sp2 = new SearchParameter();
        sp2.setUrl("http://other.org/SearchParameter/same");
        sp2.setName("Same");
        sp2.addBase("Observation");
        writeJsonResource(resourceDir, "SP-B.json", sp2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getResources().size(), 2);
        assertEquals(atlas.getSearchParameters().size(), 1);
    }

    @Test
    public void testDuplicateTailIdCodeSystem() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        CodeSystem cs1 = new CodeSystem();
        cs1.setUrl("http://example.org/CodeSystem/same");
        writeJsonResource(resourceDir, "CS-A.json", cs1);

        CodeSystem cs2 = new CodeSystem();
        cs2.setUrl("http://other.org/CodeSystem/same");
        writeJsonResource(resourceDir, "CS-B.json", cs2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getResources().size(), 2);
        assertEquals(atlas.getCodeSystems().size(), 1);
    }

    @Test
    public void testDuplicateTailIdConceptMap() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ConceptMap cm1 = new ConceptMap();
        cm1.setUrl("http://example.org/ConceptMap/same");
        writeJsonResource(resourceDir, "CM-A.json", cm1);

        ConceptMap cm2 = new ConceptMap();
        cm2.setUrl("http://other.org/ConceptMap/same");
        writeJsonResource(resourceDir, "CM-B.json", cm2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getResources().size(), 2);
        assertEquals(atlas.getConceptMaps().size(), 1);
    }

    @Test
    public void testDuplicateTailIdImplementationGuide() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        ImplementationGuide ig1 = new ImplementationGuide();
        ig1.setUrl("http://example.org/ImplementationGuide/same");
        writeJsonResource(resourceDir, "IG-A.json", ig1);

        ImplementationGuide ig2 = new ImplementationGuide();
        ig2.setUrl("http://other.org/ImplementationGuide/same");
        writeJsonResource(resourceDir, "IG-B.json", ig2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getResources().size(), 2);
        assertEquals(atlas.getImplementationGuides().size(), 1);
    }

    @Test
    public void testDuplicateTailIdNamingSystem() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        NamingSystem ns1 = new NamingSystem();
        ns1.setUrl("http://example.org/NamingSystem/same");
        ns1.setName("NS1");
        ns1.setStatus(Enumerations.PublicationStatus.ACTIVE);
        ns1.setKind(NamingSystem.NamingSystemType.CODESYSTEM);
        ns1.addUniqueId().setType(NamingSystem.NamingSystemIdentifierType.URI).setValue("urn:ns1");
        writeJsonResource(resourceDir, "NS-A.json", ns1);

        NamingSystem ns2 = new NamingSystem();
        ns2.setUrl("http://other.org/NamingSystem/same");
        ns2.setName("NS2");
        ns2.setStatus(Enumerations.PublicationStatus.ACTIVE);
        ns2.setKind(NamingSystem.NamingSystemType.CODESYSTEM);
        ns2.addUniqueId().setType(NamingSystem.NamingSystemIdentifierType.URI).setValue("urn:ns2");
        writeJsonResource(resourceDir, "NS-B.json", ns2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getResources().size(), 2);
        assertEquals(atlas.getNamingSystems().size(), 1);
    }

    @Test
    public void testDuplicateTailIdCompartmentDefinition() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        CompartmentDefinition cd1 = new CompartmentDefinition();
        cd1.setUrl("http://example.org/CompartmentDefinition/same");
        cd1.setCode(CompartmentDefinition.CompartmentType.PATIENT);
        writeJsonResource(resourceDir, "CD-A.json", cd1);

        CompartmentDefinition cd2 = new CompartmentDefinition();
        cd2.setUrl("http://other.org/CompartmentDefinition/same");
        cd2.setCode(CompartmentDefinition.CompartmentType.ENCOUNTER);
        writeJsonResource(resourceDir, "CD-B.json", cd2);

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertEquals(atlas.getResources().size(), 2);
        assertEquals(atlas.getCompartmentDefinitions().size(), 1);
    }

    // ========== Non-JSON files are ignored ==========

    @Test
    public void testNonJsonFilesIgnored() throws IOException {
        tempDir = Files.createTempDirectory("atlas-test");
        Path resourceDir = Files.createDirectory(tempDir.resolve("specs"));

        Files.writeString(resourceDir.resolve("readme.txt"), "not a resource");
        Files.writeString(resourceDir.resolve("data.xml"), "<Patient/>"); // XML, not JSON

        Atlas atlas = new Atlas();
        atlas.loadPaths(tempDir.toString(), "specs");

        assertTrue(atlas.getResources().isEmpty(), "Only .json files should be loaded");
    }
}
