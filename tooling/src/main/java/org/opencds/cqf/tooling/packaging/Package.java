package org.opencds.cqf.tooling.packaging;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Package<T extends IBaseResource> {

    private static final Logger logger = LoggerFactory.getLogger(Package.class);

    /*

        CRMI Packaging Guidance

        In general, artifacts such as libraries, measures, and test cases can be packaged as a Bundle of type transaction.
        However, since large artifact packages may span multiple bundles, the type collection MAY be used as well. In that
        case, the bundles SHOULD be processed as a unit (i.e. as a single transaction). The intent of splitting the bundles
        is to allow large packages to be processed, but in the case that they are split, transactional considerations are
        the responsibility of the consumer.

        An artifact bundle contains the artifact as the first entry in the bundle, and optionally the dependencies and
        associated artifacts as subsequent entries as follows:
            1. Artifact: The main artifact resource for the package
            2. Dependencies: Any dependent artifact referenced by the main artifact
            3. Test Cases: Any test cases defined for the artifact

    */

    private T mainArtifact;
    private IBaseResource primaryLibrary;
    private Set<IBaseResource> dependencies;
    private TestPackage<?,?> testPackage;
    private String igRoot;
    private FhirContext fhirContext;
    private boolean includeDependencies;
    private boolean includeTerminology;
    private boolean includeTests;
    private String fhirServerUrl;
    private IGenericClient fhirClient;
    private String bundleOutputPath;

    public Package(String igRoot, FhirContext fhirContext, boolean includeDependencies, boolean includeTerminology, boolean includeTests, String fhirServerUrl) {
        this.igRoot = igRoot;
        this.fhirContext = fhirContext;
        this.includeDependencies = includeDependencies;
        this.includeTerminology = includeTerminology;
        this.includeTests = includeTests;
        this.fhirServerUrl = fhirServerUrl;
        if (this.fhirServerUrl != null) {
            this.fhirClient = this.fhirContext.newRestfulGenericClient(fhirServerUrl);
        }
        this.bundleOutputPath = FilenameUtils.concat(igRoot, "bundles");
    }

    public abstract T resolveMainArtifact();
    public abstract Set<IBaseResource> resolveDependencies(T mainArtifact);
    public abstract TestPackage<?, ?> resolveTests(T mainArtifact);
    public abstract void output();

    public void packageArtifact() {
        this.mainArtifact = resolveMainArtifact();
        this.dependencies = resolveDependencies(this.mainArtifact);
        if (includeTests) {
            this.testPackage = resolveTests(this.mainArtifact);
        }
        output();
    }

    public void resolvePrimaryLibraryDependencies(IBaseResource mainArtifact, FhirContext fhirContext, LinkedHashSet<IBaseResource> dependencies) {
        var primaryLibrary = IOUtils.getLibraryUrlMap(fhirContext).get(
                ResourceUtils.getPrimaryLibraryUrl(mainArtifact, fhirContext));
        if (getPrimaryLibrary() == null) { // we want to save the primary library for the artifact being bundled not dependency libraries
            setPrimaryLibrary(primaryLibrary);
        }

        var missingDependencies = new HashSet<String>();
        if (includeDependencies) {
            dependencies.add(primaryLibrary);
            var dependencyLibraries = ResourceUtils.getDepLibraryResources(
                    primaryLibrary, fhirContext, true, false, missingDependencies);
            dependencies.addAll(dependencyLibraries.values());
        }

        if (includeTerminology) {
            var dependencyValueSets = ResourceUtils.getDepValueSetResources(
                    primaryLibrary, fhirContext, true, missingDependencies);
            dependencies.addAll(dependencyValueSets.values());
        }

        missingDependencies.forEach(missing -> logger.warn("Unable to package dependency: {}", missing));
    }

    public T getMainArtifact() {
        return mainArtifact;
    }

    public void setMainArtifact(T mainArtifact) {
        this.mainArtifact = mainArtifact;
    }

    public IBaseResource getPrimaryLibrary() {
        return primaryLibrary;
    }

    public void setPrimaryLibrary(IBaseResource primaryLibrary) {
        this.primaryLibrary = primaryLibrary;
    }

    public Set<IBaseResource> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<IBaseResource> dependencies) {
        this.dependencies = dependencies;
    }

    public TestPackage<?,?> getTestPackage() {
        return testPackage;
    }

    public void setTests(TestPackage<?,?> testPackage) {
        this.testPackage = testPackage;
    }

    public String getIgRoot() {
        return igRoot;
    }

    public void setIgRoot(String igRoot) {
        this.igRoot = igRoot;
    }

    public FhirContext getFhirContext() {
        return fhirContext;
    }

    public void setFhirContext(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    public boolean isIncludeDependencies() {
        return includeDependencies;
    }

    public void setIncludeDependencies(boolean includeDependencies) {
        this.includeDependencies = includeDependencies;
    }

    public boolean isIncludeTerminology() {
        return includeTerminology;
    }

    public void setIncludeTerminology(boolean includeTerminology) {
        this.includeTerminology = includeTerminology;
    }

    public boolean isIncludeTests() {
        return includeTests;
    }

    public void setIncludeTests(boolean includeTests) {
        this.includeTests = includeTests;
    }

    public String getFhirServerUrl() {
        return fhirServerUrl;
    }

    public void setFhirServerUrl(String fhirServerUrl) {
        this.fhirServerUrl = fhirServerUrl;
    }

    public IGenericClient getFhirClient() {
        return fhirClient;
    }

    public void setFhirClient(IGenericClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    public String getBundleOutputPath() {
        return bundleOutputPath;
    }

    public void setBundleOutputPath(String bundleOutputPath) {
        this.bundleOutputPath = bundleOutputPath;
    }
}
