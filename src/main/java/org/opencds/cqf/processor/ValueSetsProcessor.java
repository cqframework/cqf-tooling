package org.opencds.cqf.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.utilities.IOUtils;
import org.opencds.cqf.utilities.IOUtils.Encoding;
import org.opencds.cqf.utilities.LogUtils;
import org.opencds.cqf.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetsProcessor {
    private static Map<String, IAnyResource> copyToUrls(List<IAnyResource> valueSets, FhirContext fhirContext) {
        switch (fhirContext.getVersion().getVersion()) {
        case DSTU3:
            return copyToStu3Urls(valueSets, fhirContext);
        case R4:
            return copyToR4Urls(valueSets, fhirContext);
        default:
            throw new IllegalArgumentException(
                    "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    private static Map<String, IAnyResource> copyToStu3Urls(List<IAnyResource> valueSets, FhirContext fhirContext) {
        Map<String, IAnyResource> valueSetUrls = new HashMap<String, IAnyResource>();
        for (IAnyResource resource : valueSets) {
            if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
                valueSetUrls.putIfAbsent(((org.hl7.fhir.dstu3.model.ValueSet)resource).getUrl(), resource);
            } else if (resource instanceof org.hl7.fhir.dstu3.model.Bundle) {
                org.hl7.fhir.dstu3.model.Bundle bundle = (org.hl7.fhir.dstu3.model.Bundle) resource; 
                for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent bundleEntry : bundle.getEntry()) {
                    org.hl7.fhir.dstu3.model.ValueSet valueSet = (org.hl7.fhir.dstu3.model.ValueSet)bundleEntry.getResource();
                    valueSetUrls.putIfAbsent((valueSet).getUrl(), valueSet);
                }
            }
        }
        return valueSetUrls;
    }

    private static Map<String, IAnyResource> copyToR4Urls(List<IAnyResource> valueSets, FhirContext fhirContext) {
        Map<String, IAnyResource> valueSetUrls = new HashMap<String, IAnyResource>();
        for (IAnyResource resource : valueSets) {
            if (resource instanceof org.hl7.fhir.r4.model.ValueSet) {
                valueSetUrls.putIfAbsent(((org.hl7.fhir.r4.model.ValueSet)resource).getUrl(), resource);
            } else if (resource instanceof org.hl7.fhir.r4.model.Bundle) {
                org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) resource; 
                for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent bundleEntry : bundle.getEntry()) {
                    org.hl7.fhir.r4.model.ValueSet valueSet = (org.hl7.fhir.r4.model.ValueSet)bundleEntry.getResource();
                    valueSetUrls.putIfAbsent((valueSet).getUrl(), valueSet);
                }
            }
        }
        return valueSetUrls;
    }

    private static Map<String, IAnyResource> cachedValueSets = null;
    public static Map<String, IAnyResource> getCachedValueSets(FhirContext fhirContext) {
        if (cachedValueSets == null) {
            IntitializeCachedValueSets(fhirContext);
        }
        return cachedValueSets;
    }

    private static void IntitializeCachedValueSets(FhirContext fhirContext) {
        List<String> allValueSetPaths = IOUtils.getTerminologyPaths(fhirContext).stream().collect(Collectors.toList());
        List<IAnyResource> allValueSets = IOUtils.readResources(allValueSetPaths, fhirContext); 
            
        cachedValueSets = ValueSetsProcessor.copyToUrls(allValueSets, fhirContext);
    }
    
    public static String getId(String baseId) {
        return "valuesets-" + baseId;
    }

    public static Boolean bundleValueSets(String cqlContentPath, String igPath, FhirContext fhirContext,
            Map<String, IAnyResource> resources, Encoding encoding, Boolean includeDependencies, Boolean includeVersion) {
        Boolean shouldPersist = true;
        try {
            Map<String, IAnyResource> dependencies = ResourceUtils.getDepValueSetResources(cqlContentPath, igPath, fhirContext, includeDependencies, includeVersion);
            for (IAnyResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.getId(), resource);
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putException(cqlContentPath, e.getMessage());
        }
        return shouldPersist;
    }
}
