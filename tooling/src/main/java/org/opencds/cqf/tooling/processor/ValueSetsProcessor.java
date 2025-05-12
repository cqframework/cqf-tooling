package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.cql.exception.CqlTranslatorException;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static void bundleValueSets(String cqlContentPath, String igPath, FhirContext fhirContext,
            Map<String, IBaseResource> resources, Encoding encoding, Boolean includeDependencies, Boolean includeVersion) throws CqlTranslatorException {
            Map<String, IBaseResource> dependencies = ResourceUtils.getDepValueSetResources(cqlContentPath, igPath, fhirContext, includeDependencies, includeVersion);
            for (IBaseResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.fhirType() + '/' + resource.getIdElement().getIdPart(), resource);
            }
    }

    public static void bundleValueSets(IBaseResource resource, FhirContext fhirContext,
           Map<String, IBaseResource> resources, Encoding encoding, Boolean includeDependencies) throws CqlTranslatorException {
        Set<String> missingDependencies = new HashSet<>();
        Map<String, IBaseResource> dependencies = ResourceUtils.getDepValueSetResources(resource, fhirContext, includeDependencies, missingDependencies);
        for (IBaseResource dependency : dependencies.values()) {
            resources.putIfAbsent(resource.fhirType() + '/' + dependency.getIdElement().getIdPart(), resource);
        }
        if (missingDependencies.size() > 0) {
            throw new CqlTranslatorException(missingDependencies.stream().collect(Collectors.toList()), CqlCompilerException.ErrorSeverity.Warning);
        }
    }
}
