package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.terminology.EnsureExecutableValueSetOperation;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.Arrays;
import java.util.List;

public class EnsureExecutableValueSetNaiveTest {
    @Test
    public void testEnsureExecutableValueset() {
        FhirContext fhirContext = FhirContext.forR4();
        File output = new File("target/test/resources/org/opencds/cqf/tooling/terminology/output");
        if (null != output.listFiles()){
            output.delete();
        }

        EnsureExecutableValueSetOperation executableValueSetOperation = new EnsureExecutableValueSetOperation();
        executableValueSetOperation.execute(new String[]{
                "-EnsureExecutableValueSet",
                "-vsp=src/test/resources/org/opencds/cqf/tooling/testfiles/refreshIG/input/vocabulary/valueset/manual",
                "-op=target/test/resources/org/opencds/cqf/tooling/terminology/output"
        });

        List<String> valuesetPaths = IOUtils.getFilePaths("target/test/resources/org/opencds/cqf/tooling/terminology/output", false);
        ArrayList<ValueSet> valueSets = new ArrayList<>();
        for (String path : valuesetPaths){
            valueSets.add((ValueSet) IOUtils.readResource(path, fhirContext, true));
        }

        for (ValueSet valueSet : valueSets){
            if (null == valueSet.getExpansion().getParameter().stream().filter(p -> p.getName().equals("naive")).findFirst().orElse(null)){
                throw new IllegalArgumentException("naive parameter not set on " + valueSet.getUrl());
            }
        }
    }
}
