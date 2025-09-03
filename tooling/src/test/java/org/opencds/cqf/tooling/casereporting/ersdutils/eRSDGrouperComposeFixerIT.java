package org.opencds.cqf.tooling.casereporting.ersdutils;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class eRSDGrouperComposeFixerIT {
    private static final Logger logger = LoggerFactory.getLogger(eRSDGrouperComposeFixerIT.class);

    private Bundle fixComposeInGroupers(String[] params) throws Exception {
        eRSDGrouperComposeFixer eRSDGrouperComposeFixer = new eRSDGrouperComposeFixer();

        return eRSDGrouperComposeFixer.fixBundle(params);
    }

    @Test
    public void testFixComposeInGroupers() throws Exception {

        Bundle fixedBundle = fixComposeInGroupers(new String[] {
            "-e=json",
            "-op=src/test/resources/casereporting/ersdutils/output",
            "-p=src/test/resources/casereporting/ersdutils/eRSDv3_specification_bundle.json"
//            "-p=src/test/resources/casereporting/ersdutils/eRSD-for-grouper-url-fix.json"
        });

//        assertEquals(tesPackage.getEntry().size(), 242);
        assertNotNull(fixedBundle);
    }
}