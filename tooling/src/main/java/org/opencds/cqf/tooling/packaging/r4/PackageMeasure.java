package org.opencds.cqf.tooling.packaging.r4;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.packaging.Package;
import org.opencds.cqf.tooling.packaging.TestPackage;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PackageMeasure extends Package<Measure> {
    private static final Logger logger = LoggerFactory.getLogger(PackageMeasure.class);
    private final String measureFilePath;

    public PackageMeasure(String igRoot, FhirContext fhirContext, String measureFilePath, boolean includeDependencies, boolean includeTerminology, boolean includeTests, String fhirServerUrl) {
        super(igRoot, fhirContext, includeDependencies, includeTerminology, includeTests, fhirServerUrl);
        this.measureFilePath = measureFilePath;
        // If the package operation is run separately from the refresh operation, we need to initialize the directory paths
        if (IOUtils.resourceDirectories.isEmpty()) {
            // TODO: this should be smarter... ideally the IGRepository interface should be leveraged for data retrieval
            var parent = Paths.get(measureFilePath).getParent().toString();
            while (!parent.endsWith("resources")) {
                parent = Paths.get(parent).getParent().toString();
            }
            IOUtils.resourceDirectories.add(parent);
            var input = Paths.get(parent).getParent();
            IOUtils.resourceDirectories.add(input.resolve("vocabulary").toString());
        }
    }

    @Override
    public Measure resolveMainArtifact() {
        var mainArtifactCandidate = IOUtils.readResource(measureFilePath, getFhirContext());
        if (mainArtifactCandidate instanceof Measure) {
            return (Measure) mainArtifactCandidate;
        }
        logger.warn("Expected artifact of type Measure, found {}", mainArtifactCandidate.fhirType());
        throw new IllegalArgumentException(
                "Expected artifact of type Measure, found " + mainArtifactCandidate.fhirType());
    }

    @Override
    public Set<IBaseResource> resolveDependencies(Measure mainArtifact) {
        var dependencies = new LinkedHashSet<IBaseResource>();
        if (mainArtifact.hasLibrary()) {
            resolvePrimaryLibraryDependencies(mainArtifact, getFhirContext(), dependencies);
        }
        return dependencies;
    }

    @Override
    public TestPackage<Group, Bundle> resolveTests(Measure mainArtifact) {
        var testPackage = new TestPackage<Group, Bundle>();
        var tests = new HashSet<Bundle>();
        var testsPath = IOUtils.concatFilePath(getIgRoot(), "input", "tests");
        var candidateDirectories = IOUtils.getDirectoryPaths(testsPath, true);
        for (var candidateDirectory : candidateDirectories) {
            if (candidateDirectory.endsWith(mainArtifact.getIdElement().getIdPart())) {
                // Is there a Group resource?
                var groups = IOUtils.getResourcesOfTypeInDirectory(candidateDirectory, getFhirContext(), Group.class, true);
                if (!groups.isEmpty()) {
                    // TODO: What if more than 1 group?
                    testPackage.setGroup((Group) groups.get(0));
                }
                for (var testDirectory : IOUtils.getDirectoryPaths(candidateDirectory, true)) {
                    var testBundle = IOUtils.bundleResourcesInDirectoryAsTransaction(testDirectory, getFhirContext(), true);
                    testBundle.setId("tests-" + FilenameUtils.getName(testDirectory) + "-bundle");
                    tests.add((Bundle) testBundle);
                }
            }
        }
        testPackage.setTests(tests);
        return testPackage;
    }

    /*
        Output format:
            - bundles (dir at IG root if no output directory is provided)
                - measureID (dir)
                    - measureID-files (dir)
                        - Dependency Libraries (Bundle)
                        - Dependency ValueSets (Bundle)
                        - Measure FHIR Resource (file - the Measure being bundled)
                        - CQL (file - the primary library referenced in the Measure)
                        - Library FHIR Resource (file - the primary library referenced in the Measure)
                    - measureID-bundle (file - contains the Measure and all dependencies)
    */
    @Override
    public void output() {
        var mainArtifact = (Measure) getMainArtifact();
        var dependencies = getDependencies();
        var testPackage = getTestPackage();

        var measureId = mainArtifact.getIdElement().getIdPart();
        logger.info("Packaging Measure {}...", measureId);
        var measureOutputPath = IOUtils.concatFilePath(getBundleOutputPath(),
                "measure", measureId);
        IOUtils.initializeDirectory(measureOutputPath);

        var measureFilesOutputPath = IOUtils.concatFilePath(measureOutputPath,
                measureId + "-files");
        IOUtils.initializeDirectory(measureFilesOutputPath);
        IOUtils.writeResource(mainArtifact, measureFilesOutputPath, IOUtils.Encoding.JSON, getFhirContext());
        IOUtils.writeResource(getPrimaryLibrary(), measureFilesOutputPath, IOUtils.Encoding.JSON, getFhirContext());

        // TODO: Is this correct? Do we just exclude the dependencies from the bundle?
        if (isIncludeDependencies()) {
            logger.info("Packaging Dependencies...");
            var libraryDependencyBundleId = "library-deps-" + measureId + "-bundle";
            IOUtils.writeBundle(createDependencyLibraryBundle(libraryDependencyBundleId, dependencies),
                    measureFilesOutputPath, IOUtils.Encoding.JSON, getFhirContext(), libraryDependencyBundleId);
        }

        // TODO: Is this correct? Do we just exclude the dependencies from the bundle?
        if (isIncludeTerminology()) {
            logger.info("Packaging Terminology...");
            var valueSetDependencyBundleId = "valuesets-" + measureId + "-bundle";
            IOUtils.writeBundle(createDependencyValueSetBundle(valueSetDependencyBundleId, dependencies),
                    measureFilesOutputPath, IOUtils.Encoding.JSON, getFhirContext(), valueSetDependencyBundleId);
        }

        var cqlFileOutputPath = IOUtils.concatFilePath(measureFilesOutputPath,
                ((Library) getPrimaryLibrary()).getIdPart() + ".cql");
        IOUtils.writeCqlToFile(ResourceUtils.getCqlFromR4Library((Library) getPrimaryLibrary()), cqlFileOutputPath);

        if (isIncludeTests() && testPackage != null) {
            logger.info("Packaging {} Tests...", testPackage.getTests().size());
            testPackage.getTests().forEach(
                    test -> {
                        dependencies.addAll(BundleUtils.getR4ResourcesFromBundle((Bundle) test));
                        IOUtils.writeBundle(test, measureFilesOutputPath, IOUtils.Encoding.JSON,
                                getFhirContext(), test.getIdElement().getIdPart());
                    }
            );
            if (testPackage.getGroup() != null) {
                dependencies.add(testPackage.getGroup());
                IOUtils.writeResource(testPackage.getGroup(), measureFilesOutputPath, IOUtils.Encoding.JSON, getFhirContext(),
                        true, "Group-" + testPackage.getGroup().getIdElement().getIdPart());
            }
        }

        dependencies.add(mainArtifact);
        var packageBundle = createArtifactPackageBundle(measureId, dependencies);
        IOUtils.writeBundle(packageBundle, measureOutputPath,
                IOUtils.Encoding.JSON, getFhirContext(), measureId + "-bundle");

        if (getFhirClient() != null) {
            logger.info("Loading package to FHIR Server: {}", getFhirServerUrl());
            try {
                getFhirClient().transaction().withBundle(packageBundle).execute();
            } catch (Exception e) {
                logger.warn("Error loading package: {}", e.getMessage());
            }
        }

        logger.info("Finished Packaging Measure {}...", measureId);
    }

    private Bundle createArtifactPackageBundle(String id, Set<IBaseResource> resources) {
        return BundleUtils.bundleR4Artifacts(id, new ArrayList<>(resources), null, true);
    }

    private Bundle createDependencyLibraryBundle(String id, Set<IBaseResource> dependencies) {
        var libraries = dependencies.stream().filter(
                dependency -> dependency instanceof Library).collect(Collectors.toList());
        return BundleUtils.bundleR4Artifacts(id, libraries, null, true);
    }

    private Bundle createDependencyValueSetBundle(String id, Set<IBaseResource> dependencies) {
        var valueSets = dependencies.stream().filter(dependency -> dependency instanceof ValueSet).collect(Collectors.toList());
        return BundleUtils.bundleR4Artifacts(id, valueSets, null, true);
    }
}
