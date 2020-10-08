package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.common.CqfmSoftwareSystem;
import org.w3._1999.xhtml.P;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BundleUtils {

    public static Object bundleArtifacts(String id, List<IBaseResource> resources, FhirContext fhirContext) {
        for (IBaseResource resource : resources) {
            if (resource.getIdElement().getIdPart() == null || resource.getIdElement().getIdPart().equals("")) {
                ResourceUtils.setIgId(id.replace("-bundle", "-" + UUID.randomUUID()), resource, false);
                resource.setId(resource.getClass().getSimpleName() + "/" + resource.getIdElement().getIdPart());
            }
        }
        
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                return bundleStu3Artifacts(id, resources);
            case R4:
                return bundleR4Artifacts(id, resources);
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    public static org.hl7.fhir.dstu3.model.Bundle bundleStu3Artifacts(String id, List<IBaseResource> resources)
    {
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        ResourceUtils.setIgId(id, bundle, false);
        bundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);
        for (IBaseResource resource : resources)
        {
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

    public static org.hl7.fhir.r4.model.Bundle bundleR4Artifacts(String id, List<IBaseResource> resources)
    {
        org.hl7.fhir.r4.model.Bundle bundle = new org.hl7.fhir.r4.model.Bundle();
        ResourceUtils.setIgId(id, bundle, false);
        bundle.setType(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION);
        for (IBaseResource resource : resources)
        {            
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

    public static void postBundle(IOUtils.Encoding encoding, FhirContext fhirContext, String fhirUri, IBaseResource bundle) {
        if (fhirUri != null && !fhirUri.equals("")) {
            try {
                HttpClientUtils.post(fhirUri, bundle, encoding, fhirContext);
            } catch (IOException e) {
                LogUtils.putException(bundle.getIdElement().getIdPart(), "Error posting to FHIR Server: " + fhirUri + ".  Bundle not posted.");
            }
        }
    }

    public static List<Map.Entry<String, IBaseResource>> GetBundlesInDir(String directoryPath, FhirContext fhirContext) {
        return GetBundlesInDir(directoryPath, fhirContext, true);
    }

    public static List<Map.Entry<String, IBaseResource>> GetBundlesInDir(String directoryPath, FhirContext fhirContext, Boolean recursive) {
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("path to directory must be an existing directory.");
        }

        List<String> filePaths = IOUtils.getFilePaths(directoryPath, recursive).stream().filter(x -> !x.endsWith(".cql")).collect(Collectors.toList());

        List<Map.Entry<String, IBaseResource>> bundleMap = new ArrayList<>();
        RuntimeResourceDefinition bundleDefinition = (RuntimeResourceDefinition)ResourceUtils.getResourceDefinition(fhirContext, "Bundle");
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

    public static void stampDstu3BundleEntriesWithSoftwareSystems(org.hl7.fhir.dstu3.model.Bundle bundle, List<CqfmSoftwareSystem> softwareSystems, FhirContext fhirContext, String rootDir) {
        for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry: bundle.getEntry()) {
            org.hl7.fhir.dstu3.model.Resource resource = entry.getResource();
            if ((resource.fhirType().equals("Library")) || ((resource.fhirType().equals("Measure")))) {
                org.opencds.cqf.tooling.common.stu3.CqfmSoftwareSystemHelper cqfmSoftwareSystemHelper = new org.opencds.cqf.tooling.common.stu3.CqfmSoftwareSystemHelper(rootDir);
                cqfmSoftwareSystemHelper.ensureSoftwareSystemExtensionAndDevice((org.hl7.fhir.dstu3.model.DomainResource)resource, softwareSystems, fhirContext);
            }
        }
    }

    public static void stampR4BundleEntriesWithSoftwareSystems(org.hl7.fhir.r4.model.Bundle bundle, List<CqfmSoftwareSystem> softwareSystems, FhirContext fhirContext, String rootDir) {
        for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry: bundle.getEntry()) {
            org.hl7.fhir.r4.model.Resource resource = entry.getResource();
            if ((resource.fhirType().equals("Library")) || ((resource.fhirType().equals("Measure")))) {
                org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper cqfmSoftwareSystemHelper = new org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper(rootDir);
                cqfmSoftwareSystemHelper.ensureSoftwareSystemExtensionAndDevice((org.hl7.fhir.r4.model.DomainResource)resource, softwareSystems, fhirContext);
            }
        }
    }
}
