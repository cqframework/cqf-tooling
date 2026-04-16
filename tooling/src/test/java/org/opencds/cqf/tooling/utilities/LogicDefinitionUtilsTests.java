package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.StringType;
import org.opencds.cqf.tooling.utilities.constants.CqfConstants;
import org.opencds.cqf.tooling.utilities.constants.CqfmConstants;
import org.testng.annotations.Test;

public class LogicDefinitionUtilsTests {

    private static Extension logicDefinition(String url, String libraryName, String name, int displaySequence) {
        Extension ext = new Extension().setUrl(url);
        ext.addExtension(new Extension().setUrl("libraryName").setValue(new StringType(libraryName)));
        ext.addExtension(new Extension().setUrl("name").setValue(new StringType(name)));
        ext.addExtension(new Extension().setUrl("displaySequence").setValue(new IntegerType(displaySequence)));
        return ext;
    }

    @Test
    public void TestKeyUsesLibraryNameAndName() {
        Extension ext = logicDefinition(CqfConstants.LOGIC_DEFINITION_EXT_URL, "HRDMeasure", "Inpatient Beds Initial Population", 38);
        assertEquals(LogicDefinitionUtils.getLogicDefinitionKey(ext), "HRDMeasure|Inpatient Beds Initial Population");
    }

    @Test
    public void TestKeyIsNullWhenLibraryNameMissing() {
        Extension ext = new Extension().setUrl(CqfConstants.LOGIC_DEFINITION_EXT_URL);
        ext.addExtension(new Extension().setUrl("name").setValue(new StringType("X")));
        assertNull(LogicDefinitionUtils.getLogicDefinitionKey(ext));
    }

    @Test
    public void TestIsLogicDefinitionRecognizesBothUrls() {
        Extension cqf = new Extension().setUrl(CqfConstants.LOGIC_DEFINITION_EXT_URL);
        Extension cqfm = new Extension().setUrl(CqfmConstants.LOGIC_DEFINITION_EXT_URL);
        Extension other = new Extension().setUrl("http://example.org/other");
        assertEquals(LogicDefinitionUtils.isLogicDefinition(cqf), true);
        assertEquals(LogicDefinitionUtils.isLogicDefinition(cqfm), true);
        assertEquals(LogicDefinitionUtils.isLogicDefinition(other), false);
    }

    @Test
    public void TestDeduplicateRemovesDuplicatesKeepingFirst() {
        List<Extension> extensions = new ArrayList<>();
        extensions.add(logicDefinition(CqfConstants.LOGIC_DEFINITION_EXT_URL, "HRDMeasure", "Inpatient Beds Initial Population", 38));
        extensions.add(logicDefinition(CqfConstants.LOGIC_DEFINITION_EXT_URL, "HRDMeasure", "Adult Inpatient Beds Initial Population", 40));
        extensions.add(logicDefinition(CqfConstants.LOGIC_DEFINITION_EXT_URL, "HRDMeasure", "Inpatient Beds Initial Population", 82));

        LogicDefinitionUtils.deduplicate(extensions);

        assertEquals(extensions.size(), 2);
        assertEquals(LogicDefinitionUtils.getLogicDefinitionKey(extensions.get(0)), "HRDMeasure|Inpatient Beds Initial Population");
        // The first-encountered entry is kept, so the displaySequence of the survivor is 38, not 82.
        Extension kept = extensions.get(0);
        int displaySequence = ((IntegerType) kept.getExtensionByUrl("displaySequence").getValue()).getValue();
        assertEquals(displaySequence, 38);
    }

    @Test
    public void TestDeduplicatePreservesNonLogicDefinitionExtensions() {
        List<Extension> extensions = new ArrayList<>();
        extensions.add(new Extension().setUrl("http://example.org/other").setValue(new StringType("a")));
        extensions.add(logicDefinition(CqfConstants.LOGIC_DEFINITION_EXT_URL, "Lib", "Def", 1));
        extensions.add(logicDefinition(CqfConstants.LOGIC_DEFINITION_EXT_URL, "Lib", "Def", 2));
        extensions.add(new Extension().setUrl("http://example.org/other").setValue(new StringType("b")));

        LogicDefinitionUtils.deduplicate(extensions);

        // One logicDefinition removed; both unrelated extensions retained.
        assertEquals(extensions.size(), 3);
    }

    @Test
    public void TestDeduplicateDedupesAcrossCqfmAndCqfUrls() {
        List<Extension> extensions = new ArrayList<>();
        extensions.add(logicDefinition(CqfmConstants.LOGIC_DEFINITION_EXT_URL, "Lib", "Def", 1));
        extensions.add(logicDefinition(CqfConstants.LOGIC_DEFINITION_EXT_URL, "Lib", "Def", 2));

        LogicDefinitionUtils.deduplicate(extensions);

        assertEquals(extensions.size(), 1);
    }
}
