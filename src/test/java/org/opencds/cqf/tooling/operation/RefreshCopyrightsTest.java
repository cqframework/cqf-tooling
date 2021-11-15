package org.opencds.cqf.tooling.operation;

import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r5.model.Library;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;

public class RefreshCopyrightsTest {
    private final String ID = "id";
    private final String ENTRY = "entry";
    private final String RESOURCE = "resource";
    private final String RESOURCE_TYPE = "resourceType";
    private final String BUNDLE_TYPE = "Bundle";
    private final String LIB_TYPE = "Library";
    private final String MEASURE_TYPE = "Measure";

    private final static String separator = System.getProperty("file.separator");

    private final String INI_LOC = "testfiles" + separator + "refreshIG" + separator + "ig.ini";

    @Test
    private boolean dataLoaded(List<String> dataDirectories) {
        if (dataDirectories == null || dataDirectories.isEmpty()) {
            System.out.println("Data directories failed to load");
            return false;
        } else {
            return true;
        }
    }
    @Test
    private boolean valuesetCopyrightsRefreshed(List<ValueSet> valueSets) {
        Boolean failed = false;
        for (ValueSet valueSet : valueSets) {
            switch (valueSet.getId()) {
                case "2.16.840.1.113883.3.464.1003.101.12.1001":
                    if (valueSet.getCopyright() != "2021+ Dynamic Content Group (dba Alphora), American Medical Association CPT®, UMLS Metathesaurus® Source Vocabularies and SNOMED CT®") {
                        failed = true;
                        break;
                    }
                case "2.16.840.1.113883.3.464.1003.101.12.1023":
                    if (valueSet.getCopyright() != "2021+ Dynamic Content Group (dba Alphora), American Medical Association CPT®") {
                        failed = true;
                        break;
                    }
                case "2.16.840.1.114222.4.11.3591":
                    if (valueSet.getCopyright() != "2021+ Dynamic Content Group (dba Alphora)") {
                        failed = true;
                        break;
                    }
                default:
                    continue;
            }
            if (failed) {
                System.out.println("Copyright refresh failed for Valueset:" + valueSet.getId());
                return false;
            }
        }
        return true;
    }
    @Test
    private boolean libraryCopyrightsRefreshed(List<Library> libraries) {
        Boolean failed = false;
        for (Library library : libraries) {
            switch (library.getId()) {
                case "2.16.840.1.113883.3.464.1003.101.12.1001":
                    if (library.getCopyright() != "2021+ Dynamic Content Group (dba Alphora), American Medical Association CPT®, UMLS Metathesaurus® Source Vocabularies and SNOMED CT®") {
                        failed = true;
                        break;
                    }
                case "2.16.840.1.113883.3.464.1003.101.12.1023":
                    if (library.getCopyright() != "2021+ Dynamic Content Group (dba Alphora), American Medical Association CPT®") {
                        failed = true;
                        break;
                    }
                case "2.16.840.1.114222.4.11.3591":
                    if (library.getCopyright() != "2021+ Dynamic Content Group (dba Alphora)") {
                        failed = true;
                        break;
                    }
                default:
                    continue;
            }
            if (failed) {
                System.out.println("Copyright refresh failed for Library:" + library.getId());
                return false;
            }
        }
        return true;
    }
}
