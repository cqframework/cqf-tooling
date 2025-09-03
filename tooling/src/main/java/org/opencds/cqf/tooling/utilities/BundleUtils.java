package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.util.BundleBuilder;
import org.cqframework.fhir.utilities.exception.IGInitializationException;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.opencds.cqf.tooling.common.SoftwareSystem;
import org.opencds.cqf.tooling.common.r4.SoftwareSystemHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BundleUtils {

    private BundleUtils() {}

    public static BundleTypeEnum getBundleType(String bundleTypeName) {
        for (BundleTypeEnum bundleTypeEnum : BundleTypeEnum.values()) {
            if (bundleTypeEnum.getCode().equalsIgnoreCase(bundleTypeName)) {
                return bundleTypeEnum;
            }
        }
        return null;
    }

    @SafeVarargs
    public static Object bundleArtifacts(String id, List<IBaseResource> resources, FhirContext fhirContext, Boolean addBundleTimestamp, List<Object>... identifiers) {
        for (IBaseResource resource : resources) {
            if (resource.getIdElement().getIdPart() == null || resource.getIdElement().getIdPart().isEmpty()) {
                ResourceUtils.setIgId(id.replace("-bundle", "-" + UUID.randomUUID()), resource, false);
                resource.setId(resource.getClass().getSimpleName() + "/" + resource.getIdElement().getIdPart());
            }
        }

        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                return bundleStu3Artifacts(id, resources);
            case R4:
                if (identifiers != null && identifiers.length > 0) {
                    return bundleR4Artifacts(id, resources, identifiers[0], addBundleTimestamp);
                }
                return bundleR4Artifacts(id, resources, null, addBundleTimestamp);
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }



    public static org.hl7.fhir.dstu3.model.Bundle bundleStu3Artifacts(String id, List<IBaseResource> resources) {
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        ResourceUtils.setIgId(id, bundle, false);
        bundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);
        for (IBaseResource resource : resources) {
            bundle.addEntry(
                    new org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent()
                            .setResource((org.hl7.fhir.dstu3.model.Resource) resource)
                            .setRequest(
                                    new org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent()
                                            .setMethod(org.hl7.fhir.dstu3.model.Bundle.HTTPVerb.PUT)
                                            .setUrl(((org.hl7.fhir.dstu3.model.Resource) resource).getId())
                            )
            );
        }
        return bundle;
    }

    public static org.hl7.fhir.r4.model.Bundle bundleR4Artifacts(String id, List<IBaseResource> resources, List<Object> identifiers, Boolean addBundleTimestamp) {
        org.hl7.fhir.r4.model.Bundle bundle = new org.hl7.fhir.r4.model.Bundle();
        ResourceUtils.setIgId(id, bundle, false);
        bundle.setType(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION);
        if (Boolean.TRUE.equals(addBundleTimestamp)) {
            bundle.setTimestamp((new Date()));
        }
        if (identifiers != null && !identifiers.isEmpty()) {
            org.hl7.fhir.r4.model.Identifier identifier = (org.hl7.fhir.r4.model.Identifier) identifiers.get(0);
            if (identifier.hasValue()) {
                identifier.setValue(identifier.getValue() + "-bundle");
            }
            bundle.setIdentifier(identifier);
        }

        for (IBaseResource resource : resources) {
            String resourceRef = (resource.getIdElement().getResourceType() == null) ? resource.fhirType() + "/" + resource.getIdElement().getIdPart() : resource.getIdElement().getValueAsString();
            bundle.addEntry(
                    new org.hl7.fhir.r4.model.Bundle.BundleEntryComponent()
                            .setResource((org.hl7.fhir.r4.model.Resource) resource)
                            .setRequest(
                                    new org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent()
                                            .setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.PUT)
                                            .setUrl(resourceRef)
                            )
            );
        }
        return bundle;
    }

    public static List<Map.Entry<String, IBaseResource>> getBundlesInDir(String directoryPath, FhirContext fhirContext) {
        return getBundlesInDir(directoryPath, fhirContext, true);
    }

    public static List<Map.Entry<String, IBaseResource>> getBundlesInDir(String directoryPath, FhirContext fhirContext, Boolean recursive) {
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("path to directory must be an existing directory.");
        }

        List<String> filePaths = IOUtils.getFilePaths(directoryPath, recursive).stream().filter(x -> !x.endsWith(".cql")).collect(Collectors.toList());

        List<Map.Entry<String, IBaseResource>> bundleMap = new ArrayList<>();
        RuntimeResourceDefinition bundleDefinition = ResourceUtils.getResourceDefinition(fhirContext, "Bundle");
        String bundleClassName = bundleDefinition.getImplementingClass().getName();
        for (String path : filePaths) {
            IBaseResource resource = IOUtils.readResource(path, fhirContext);
            if (resource != null) {
                if (bundleClassName.equals(resource.getClass().getName())) {
                    Map.Entry<String, IBaseResource> bundleEntry = new AbstractMap.SimpleEntry<>(path, resource);
                    bundleMap.add(bundleEntry);
                }
            }
        }

        return bundleMap;
//        List<IBaseResource> resources = IOUtils.readResources(filePaths, fhirContext);
//
//        return resources.stream()
//            .filter(entry -> entry != null)
//            .filter(entry ->  bundleClassName.equals(entry.getClass().getName()))
//            .collect(Collectors.toList());
    }

    public static void stampDstu3BundleEntriesWithSoftwareSystems(org.hl7.fhir.dstu3.model.Bundle bundle, List<SoftwareSystem> softwareSystems, FhirContext fhirContext, String rootDir) {
        for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            org.hl7.fhir.dstu3.model.Resource resource = entry.getResource();
            if ((resource.fhirType().equals("Library")) || (resource.fhirType().equals("Measure"))) {
                org.opencds.cqf.tooling.common.stu3.SoftwareSystemHelper softwareSystemHelper = new org.opencds.cqf.tooling.common.stu3.SoftwareSystemHelper(rootDir);
                softwareSystemHelper.ensureSoftwareSystemExtensionAndDevice((org.hl7.fhir.dstu3.model.DomainResource) resource, softwareSystems, fhirContext);
            }
        }
    }

    public static void stampR4BundleEntriesWithSoftwareSystems(org.hl7.fhir.r4.model.Bundle bundle, List<SoftwareSystem> softwareSystems, FhirContext fhirContext, String rootDir) {
        for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            org.hl7.fhir.r4.model.Resource resource = entry.getResource();
            if ((resource.fhirType().equals("Library")) || ((resource.fhirType().equals("Measure")))) {
                SoftwareSystemHelper softwareSystemHelper = new SoftwareSystemHelper(rootDir);
                softwareSystemHelper.ensureSoftwareSystemExtensionAndDevice((org.hl7.fhir.r4.model.DomainResource) resource, softwareSystems, fhirContext);
            }
        }
    }

    public static final String separator = System.getProperty("file.separator");
    public static Set<String> extractStu3Resources(org.hl7.fhir.dstu3.model.Bundle bundle, String encoding, String outputPath, boolean suppressNarrative) {
        Set<String> extractedResources = new HashSet<>();

        FhirContext context = FhirContext.forDstu3Cached();
        for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            org.hl7.fhir.dstu3.model.Resource entryResource = entry.getResource();
            if (entryResource != null) {
                if (entryResource.fhirType().equals("Measure") && suppressNarrative) {
                    ((org.hl7.fhir.dstu3.model.Measure) entryResource).setText(null);
                }

                String resourceFileLocation = outputPath + separator +
                        entryResource.getIdElement().getResourceType() + "-" + entryResource.getIdElement().getIdPart() +
                        "." + encoding;
                extractedResources.add(resourceFileLocation);

                ResourceUtils.outputResource(entryResource, encoding, context, outputPath);
            }
        }

        return extractedResources;
    }

    public static Set<String> extractR4Resources(org.hl7.fhir.r4.model.Bundle bundle, String encoding, String outputPath, boolean suppressNarrative) {
        Set<String> extractedResources = new HashSet<>();

        FhirContext context = FhirContext.forR4Cached();
        for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            org.hl7.fhir.r4.model.Resource entryResource = entry.getResource();
            if (entryResource != null) {
                if (entryResource.fhirType().equals("Measure") && suppressNarrative) {
                    ((org.hl7.fhir.r4.model.Measure) entryResource).setText(null);
                }
                String resourceFileLocation = outputPath + separator +
                        entryResource.getIdElement().getResourceType() + "-" + entryResource.getIdElement().getIdPart() +
                        "." + encoding;
                extractedResources.add(resourceFileLocation);

                ResourceUtils.outputResource(entryResource, encoding, context, outputPath);
            }
        }

        return extractedResources;
    }

    public static Set<String> extractResources(Object bundle, String encoding, String outputDir, boolean suppressNarrative, String version) {
        Set<String> extractedResources = new HashSet<>();
        if (version.equals("stu3") && bundle instanceof org.hl7.fhir.dstu3.model.Bundle) {
             extractedResources = new HashSet<>(BundleUtils.extractStu3Resources((org.hl7.fhir.dstu3.model.Bundle) bundle, encoding, outputDir, suppressNarrative));
        } else if (version.equals("r4") && bundle instanceof org.hl7.fhir.r4.model.Bundle) {
            extractedResources = new HashSet<>(BundleUtils.extractR4Resources((org.hl7.fhir.r4.model.Bundle) bundle, encoding, outputDir, suppressNarrative));
        }else{
            throw new IllegalArgumentException("Invalid bundle/version: " + bundle + "/" + version);
        }
        return extractedResources;
    }

    public static List<Resource> getR4ResourcesFromBundle(Bundle bundle){
        ArrayList <Resource> resourceArrayList = new ArrayList<>();
        for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            org.hl7.fhir.r4.model.Resource entryResource = entry.getResource();
            // TODO: How to handle nested bundles? Recursively or skip? Skipping for now...
            if (entryResource != null && !(entryResource instanceof Bundle)) {
                resourceArrayList.add(entryResource);
            }
        }
        return resourceArrayList;
    }

    public static List<org.hl7.fhir.dstu3.model.Resource> getStu3ResourcesFromBundle(org.hl7.fhir.dstu3.model.Bundle bundle){
        ArrayList <org.hl7.fhir.dstu3.model.Resource> resourceArrayList = new ArrayList<>();
        for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            org.hl7.fhir.dstu3.model.Resource entryResource = entry.getResource();
            if (entryResource != null) {
                resourceArrayList.add(entryResource);
            }
        }
        return resourceArrayList;
    }

    public static IBaseBundle getBundleOfResourceTypeFromDirectory(String directoryPath, FhirContext fhirContext, Class<? extends IBaseResource> clazz) {
        BundleBuilder builder = new BundleBuilder(fhirContext);
        try (Stream<Path> walk = Files.walk(Paths.get(directoryPath), 1)) {
            walk.filter(p -> !Files.isDirectory(p)).forEach(
                    file -> {
                        IBaseResource resource = IOUtils.readResource(file.toString(), fhirContext);
                        if (resource != null && clazz.isAssignableFrom(resource.getClass())) {
                            builder.addCollectionEntry(resource);
                        }
                    }
            );
        } catch (IOException ioe) {
            throw new IGInitializationException("Error reading resources from path: " + directoryPath, ioe);
        }
        return builder.getBundle();
    }

    public static boolean resourceIsABundle(IBaseResource resource) {
        return (
                (resource instanceof org.hl7.fhir.dstu3.model.Bundle)
//                      uncomment when R5 processing is accounted for
//                      || (resource instanceof org.hl7.fhir.r5.model.Bundle)
                        || (resource instanceof org.hl7.fhir.r4.model.Bundle)
        );
    }

    public static boolean resourceIsTransactionBundle(IBaseResource inputResource) {
        if (inputResource == null) return false;

        if (inputResource instanceof org.hl7.fhir.dstu3.model.Bundle) {
            return ((org.hl7.fhir.dstu3.model.Bundle) inputResource).getType().equals(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);

        } else if (inputResource instanceof org.hl7.fhir.r4.model.Bundle) {
            return ((org.hl7.fhir.r4.model.Bundle) inputResource).getType().equals(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION);
        }
        return false;

    }
}
