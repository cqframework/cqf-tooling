package org.opencds.cqf.terminology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetsProcessor {
    private static Map<String, IAnyResource> copyToIDs(List<IAnyResource> valueSets, FhirContext fhirContext) {
        switch (fhirContext.getVersion().getVersion()) {
        case DSTU3:
            return copyToStu3IDs(valueSets, fhirContext);
        case R4:
            return copyToR4IDs(valueSets, fhirContext);
        default:
            throw new IllegalArgumentException(
                    "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    private static Map<String, IAnyResource> copyToStu3IDs(List<IAnyResource> valueSets, FhirContext fhirContext) {
        Map<String, IAnyResource> valueSetIDs = new HashMap<String, IAnyResource>();
        for (IAnyResource resource : valueSets) {
            if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
                valueSetIDs.putIfAbsent(((org.hl7.fhir.dstu3.model.ValueSet)resource).getUrl(), resource);
            } else if (resource instanceof org.hl7.fhir.dstu3.model.Bundle) {
                org.hl7.fhir.dstu3.model.Bundle bundle = (org.hl7.fhir.dstu3.model.Bundle) resource; 
                for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent bundleEntry : bundle.getEntry()) {
                    org.hl7.fhir.dstu3.model.ValueSet valueSet = (org.hl7.fhir.dstu3.model.ValueSet)bundleEntry.getResource();
                    valueSetIDs.putIfAbsent((valueSet).getUrl(), valueSet);
                }
            }
        }
        return valueSetIDs;
    }

    private static Map<String, IAnyResource> copyToR4IDs(List<IAnyResource> valueSets, FhirContext fhirContext) {
        Map<String, IAnyResource> valueSetIDs = new HashMap<String, IAnyResource>();
        for (IAnyResource resource : valueSets) {
            if (resource instanceof org.hl7.fhir.r4.model.ValueSet) {
                valueSetIDs.putIfAbsent(((org.hl7.fhir.r4.model.ValueSet)resource).getUrl(), resource);
            } else if (resource instanceof org.hl7.fhir.r4.model.Bundle) {
                org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) resource; 
                for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent bundleEntry : bundle.getEntry()) {
                    org.hl7.fhir.r4.model.ValueSet valueSet = (org.hl7.fhir.r4.model.ValueSet)bundleEntry.getResource();
                    valueSetIDs.putIfAbsent((valueSet).getUrl(), valueSet);
                }
            }
        }
        return valueSetIDs;
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
            
        cachedValueSets = ValueSetsProcessor.copyToIDs(allValueSets, fhirContext);
    }
    
    public static String getId(String baseId) {
        return "valuesets-" + baseId;
    }
}
