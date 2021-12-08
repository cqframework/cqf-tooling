package org.opencds.cqf.tooling.acceleratorkit;

import org.opencds.cqf.tooling.utilities.ModelCanonicalAtlasCreator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class StructureDefinitionElementBindingVisitorTest {
    public static final String separator = System.getProperty("file.separator");

    private CanonicalResourceAtlas atlas;

    @Test
    public void testGettingBindingObjects () {
        String inputPath = System.getenv ("PWD") + "/src/test/resources/org/opencds/cqf/tooling/operation/profiles/FHIR-Spec";
        String resourcePaths = "4.0.1;US-Core/3.1.0;QI-Core/4.0.0";
        String modelName = "QI-Core";
        String modelVersion = "4.0.0";
        CanonicalResourceAtlas canonicalResourceAtlas = ModelCanonicalAtlasCreator.createMainCanonicalAtlas (resourcePaths, modelName, modelVersion, inputPath);
        CanonicalResourceAtlas canonicalResourceDependenciesAtlas = ModelCanonicalAtlasCreator.createDependenciesCanonicalAtlas (resourcePaths, modelName, modelVersion, inputPath);

        StructureDefinitionElementBindingVisitor sdbv = new StructureDefinitionElementBindingVisitor (canonicalResourceAtlas, canonicalResourceDependenciesAtlas);
        Map <String, StructureDefinitionBindingObject> bindingObjects = sdbv.visitCanonicalAtlasStructureDefinitions();
        System.out.println ("binding definitions found: " + bindingObjects.size());
        Assert.assertTrue(!bindingObjects.isEmpty());
    }
}