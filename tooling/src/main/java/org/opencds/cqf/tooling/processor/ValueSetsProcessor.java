package org.opencds.cqf.tooling.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetsProcessor {
    private static Map<String, IBaseResource> copyToUrls(List<IBaseResource> valueSets, FhirContext fhirContext) {
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

    private static Map<String, IBaseResource> copyToStu3Urls(List<IBaseResource> valueSets, FhirContext fhirContext) {
        Map<String, IBaseResource> valueSetUrls = new HashMap<String, IBaseResource>();
        for (IBaseResource resource : valueSets) {
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

    private static Map<String, IBaseResource> copyToR4Urls(List<IBaseResource> valueSets, FhirContext fhirContext) {
        Map<String, IBaseResource> valueSetUrls = new HashMap<String, IBaseResource>();
        for (IBaseResource resource : valueSets) {
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

    private static Map<String, IBaseResource> cachedValueSets = null;
    public static Map<String, IBaseResource> getCachedValueSets(FhirContext fhirContext) {
        if (cachedValueSets == null) {
            IntitializeCachedValueSets(fhirContext);
        }
        return cachedValueSets;
    }

    private static void IntitializeCachedValueSets(FhirContext fhirContext) {
        List<String> allValueSetPaths = IOUtils.getTerminologyPaths(fhirContext).stream().collect(Collectors.toList());
        List<IBaseResource> allValueSets = IOUtils.readResources(allValueSetPaths, fhirContext); 
            
        cachedValueSets = ValueSetsProcessor.copyToUrls(allValueSets, fhirContext);
    }
    
    public static String getId(String baseId) {
        return "valuesets-" + baseId;
    }

    public static Boolean bundleValueSets(String cqlContentPath, String igPath, FhirContext fhirContext,
            Map<String, IBaseResource> resources, Encoding encoding, Boolean includeDependencies, Boolean includeVersion) throws Exception{
        Boolean shouldPersist = true;

        try {
            Map<String, IBaseResource> dependencies = ResourceUtils.getDepValueSetResources(cqlContentPath, igPath, fhirContext, includeDependencies, includeVersion);

            for (IBaseResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.getIdElement().getIdPart(), resource);
            }
        } catch (Exception e) {
            shouldPersist = false;
            throw e;
//            LogUtils.putException(cqlContentPath, e.getMessage());
        }
        return shouldPersist;
    }
}
