package org.opencds.cqf.tooling.packaging.r4;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.packaging.TestPackage;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.CanonicalUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PackagePlanDefinition extends org.opencds.cqf.tooling.packaging.Package<PlanDefinition> {

    private static final Logger logger = LoggerFactory.getLogger(PackagePlanDefinition.class);
    private final String planDefinitionFilePath;

    public PackagePlanDefinition(String igRoot, FhirContext fhirContext, String planDefinitionFilePath, boolean includeDependencies, boolean includeTerminology, boolean includeTests, String fhirServerUrl) {
        super(igRoot, fhirContext, includeDependencies, includeTerminology, includeTests, fhirServerUrl);
        this.planDefinitionFilePath = planDefinitionFilePath;
        // If the package operation is run separately from the refresh operation, we need to initialize the directory paths
        if (IOUtils.resourceDirectories.isEmpty()) {
            // TODO: this should be smarter... ideally the IGRepository interface should be leveraged for data retrieval
            var parent = Paths.get(planDefinitionFilePath).getParent().toString();
            while (!parent.endsWith("resources")) {
                parent = Paths.get(parent).getParent().toString();
            }
            IOUtils.resourceDirectories.add(parent);
            var input = Paths.get(parent).getParent();
            IOUtils.resourceDirectories.add(input.resolve("vocabulary").toString());
        }
    }

    @Override
    public PlanDefinition resolveMainArtifact() {
        var mainArtifactCandidate = IOUtils.readResource(planDefinitionFilePath, getFhirContext());
        if (mainArtifactCandidate instanceof PlanDefinition) {
            return (PlanDefinition) mainArtifactCandidate;
        }
        logger.warn("Expected artifact of type PlanDefinition, found {}", mainArtifactCandidate.fhirType());
        throw new IllegalArgumentException(
                "Expected artifact of type PlanDefinition, found " + mainArtifactCandidate.fhirType());
    }

    @Override
    public Set<IBaseResource> resolveDependencies(PlanDefinition mainArtifact) {
        var dependencies = new LinkedHashSet<IBaseResource>();
        if (mainArtifact.hasLibrary()) {
            resolvePrimaryLibraryDependencies(mainArtifact, getFhirContext(), dependencies);
        }

        /*
            NOTE:
            A PlanDefinition can reference ActivityDefinition and other PlanDefinition resources in the actions/sub-actions.
            These should be included, along with their dependencies, in the resulting Bundle
        */
        if (mainArtifact.hasAction()) {
            var definitionRefs = new ArrayList<String>();
            getDefinitionReferences(mainArtifact.getAction(), definitionRefs);
            var planDefinitions = IOUtils.getPlanDefinitions(getFhirContext());
            var activityDefinitions = IOUtils.getActivityDefinitions(getFhirContext());
            for (var definitionRef : definitionRefs) {
                var id = CanonicalUtils.getId(definitionRef);
                if (planDefinitions.containsKey(id)) {
                    dependencies.add(planDefinitions.get(id));
                    dependencies.addAll(resolveDependencies((PlanDefinition) planDefinitions.get(id)));
                } else if (activityDefinitions.containsKey(id)) {
                    dependencies.add(activityDefinitions.get(id));
                    if (((ActivityDefinition) activityDefinitions.get(id)).hasLibrary()) {
                        resolvePrimaryLibraryDependencies(activityDefinitions.get(id), getFhirContext(), dependencies);
                    }
                }
            }
        }

        return dependencies;
    }

    @Override
    public TestPackage<Group, Bundle> resolveTests(PlanDefinition mainArtifact) {
        return null;
    }

    /*
        Output format:
            - bundles (dir at IG root if no output directory is provided)
                - plandefinitionID (dir)
                    - plandefinitionID-files (dir)
                        - ActivityDefinitions (file(s))
                        - Dependency Libraries (Bundle)
                        - Dependency ValueSets (Bundle)
                        - PlanDefinition FHIR Resource (file - the PlanDefinition being bundled)
                        - CQL (file - the primary library referenced in the PlanDefinition)
                        - Library FHIR Resource (file - the primary library referenced in the PlanDefinition)
                    - planDefinitionID-bundle (file - contains the PlanDefinition and all dependencies)
    */
    @Override
    public void output() {
        var mainArtifact = (PlanDefinition) getMainArtifact();
        var dependencies = getDependencies();
        var testPackage = getTestPackage();

        var planDefinitionId = mainArtifact.getIdElement().getIdPart();
        logger.info("Packaging PlanDefinition {}...", planDefinitionId);
        var planDefinitionOutputPath = IOUtils.concatFilePath(getBundleOutputPath(),
                "plandefinition", planDefinitionId);
        IOUtils.initializeDirectory(planDefinitionOutputPath);

        var planDefinitionFilesOutputPath = IOUtils.concatFilePath(planDefinitionOutputPath,
                planDefinitionId + "-files");
        IOUtils.initializeDirectory(planDefinitionFilesOutputPath);
        IOUtils.writeResource(mainArtifact, planDefinitionFilesOutputPath, IOUtils.Encoding.JSON, getFhirContext());
        IOUtils.writeResource(getPrimaryLibrary(), planDefinitionFilesOutputPath, IOUtils.Encoding.JSON, getFhirContext());

        // Is this correct? Do we just exclude the dependencies from the bundle?
        if (isIncludeDependencies()) {
            logger.info("Packaging Dependencies...");
            var libraryDependencyBundleId = "library-deps-" + planDefinitionId + "-bundle";
            IOUtils.writeBundle(createDependencyLibraryBundle(libraryDependencyBundleId, dependencies),
                    planDefinitionFilesOutputPath, IOUtils.Encoding.JSON, getFhirContext(), libraryDependencyBundleId);
        }

        // Is this correct? Do we just exclude the dependencies from the bundle?
        if (isIncludeTerminology()) {
            logger.info("Packaging Terminology...");
            var valueSetDependencyBundleId = "valuesets-" + planDefinitionId + "-bundle";
            IOUtils.writeBundle(createDependencyValueSetBundle(valueSetDependencyBundleId, dependencies),
                    planDefinitionFilesOutputPath, IOUtils.Encoding.JSON, getFhirContext(), valueSetDependencyBundleId);
        }

        IOUtils.writeResources(getActivityDefinitions(dependencies), planDefinitionFilesOutputPath,
                IOUtils.Encoding.JSON, getFhirContext());
        var cqlFileOutputPath = IOUtils.concatFilePath(planDefinitionFilesOutputPath,
                ((Library) getPrimaryLibrary()).getIdPart() + ".cql");
        IOUtils.writeCqlToFile(ResourceUtils.getCqlFromR4Library((Library) getPrimaryLibrary()), cqlFileOutputPath);

        if (isIncludeTests() && testPackage != null) {
            logger.info("Packaging {} Tests...", testPackage.getTests().size());
            if (testPackage.getGroup() != null) {
                IOUtils.writeResource(testPackage.getGroup(), planDefinitionFilesOutputPath, IOUtils.Encoding.JSON, getFhirContext(),
                        true, "Group-" + testPackage.getGroup().getIdElement().getIdPart());
            }
            testPackage.getTests().forEach(
                    test -> {
                        dependencies.addAll(BundleUtils.getR4ResourcesFromBundle((Bundle) test));
                        IOUtils.writeBundle(test, planDefinitionFilesOutputPath, IOUtils.Encoding.JSON,
                                getFhirContext(), test.getIdElement().getIdPart());
                    }
            );
        }

        dependencies.add(mainArtifact);
        var packageBundle = createArtifactPackageBundle(planDefinitionId, dependencies);
        IOUtils.writeBundle(packageBundle, planDefinitionOutputPath,
                IOUtils.Encoding.JSON, getFhirContext(), planDefinitionId + "-bundle");

        if (getFhirClient() != null) {
            logger.info("Loading package to FHIR Server: {}", getFhirServerUrl());
            try {
                getFhirClient().transaction().withBundle(packageBundle).execute();
            } catch (Exception e) {
                logger.warn("Error loading package: {}", e.getMessage());
            }
        }

        logger.info("Finished Packaging PlanDefinition {}...", planDefinitionId);
    }

    private void getDefinitionReferences(List<PlanDefinition.PlanDefinitionActionComponent> actions, List<String> references) {
        for (var action : actions) {
            if (action.hasDefinition() && action.getDefinition() instanceof CanonicalType) {
                // Assuming either a PlanDefinition or ActivityDefinition - others will be ignored during resolution
                references.add(action.getDefinitionCanonicalType().getValue());
            }
            if (action.hasAction()) {
                getDefinitionReferences(action.getAction(), references);
            }
        }
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

    private List<IBaseResource> getActivityDefinitions(Set<IBaseResource> dependencies) {
        return dependencies.stream().filter(dependency -> dependency instanceof ActivityDefinition).collect(Collectors.toList());
    }
}
