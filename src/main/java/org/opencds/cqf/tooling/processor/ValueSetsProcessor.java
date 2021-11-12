package org.opencds.cqf.tooling.processor;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.formats.FormatUtilities;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.terminology.Copyrights;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetsProcessor extends BaseProcessor {
    private String valueSetPath;
    private FhirContext fhirContext;
    private Encoding encoding;
    private static CqfmSoftwareSystemHelper cqfmHelper;

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
            Map<String, IBaseResource> resources, Encoding encoding, Boolean includeDependencies, Boolean includeVersion) {
        Boolean shouldPersist = true;
        try {
            Map<String, IBaseResource> dependencies = ResourceUtils.getDepValueSetResources(cqlContentPath, igPath, fhirContext, includeDependencies, includeVersion);
            for (IBaseResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.getIdElement().getIdPart(), resource);
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putException(cqlContentPath, e.getMessage());
        }
        return shouldPersist;
    }

    public List<String> refreshValueSetsContent(BaseProcessor parentContext, Encoding outputEncoding, Boolean versioned, FhirContext fhirContext){
        initialize(parentContext);


        this.valueSetPath = FilenameUtils.concat(parentContext.getRootDir(), IGProcessor.valuesetsPathElement);
        this.fhirContext = fhirContext;
        this.encoding = encoding;

        ValueSetsProcessor.cqfmHelper = new CqfmSoftwareSystemHelper(rootDir);

        return refreshValueSets(valueSetPath, encoding);
    }

    protected List<String> refreshValueSets(String valueSetPath, Encoding encoding){
        System.out.println("Refreshing ValueSets...");
        File terminologyPath = valueSetPath != null ? new File(valueSetPath) : null;
        Map<String, String> fileMap = new HashMap<String, String>();
        List<ValueSet> valueSets = new ArrayList<>();

        for (File valueSetDirectory : terminologyPath.listFiles()){
            if (!valueSetDirectory.isHidden() && valueSetDirectory.isDirectory()){
                for (File file : valueSetDirectory.listFiles()){
                    if (!file.isHidden()){
                        loadValueSet(fileMap, valueSets, file);
                    }
                }
            }
        }

//        if (file == null || !file.exists()) {
//            for (String path : IOUtils.getTerminologyPaths(this.fhirContext)) {
//                loadValueSet(fileMap, valueSets, new File(path));
//            }
//        }
//        else if (file.isDirectory()) {
//            for (File valueSetDirectory : file.listFiles()) {
//                if (!valueSetDirectory.isHidden()){
//                    for (File valueSetFile : valueSetDirectory.listFiles()) {
//                        if (!valueSetFile.isHidden()) {
//                            loadValueSet(fileMap, valueSets, valueSetFile);
//                        }
//                    }
//                }
//            }
//        }
//        else {
//            if (!file.isHidden()) {
//                loadValueSet(fileMap, valueSets, file);
//            }
//        }

        List<String> refreshedValueSetNames = new ArrayList<String>();
        List<ValueSet> refreshedValueSets = refreshGeneratedContent(valueSets);
        for (ValueSet refreshedValueSet : refreshedValueSets) {
            String filePath = fileMap.get(refreshedValueSet.getId());
            encoding = IOUtils.getEncoding(filePath);

            IOUtils.writeResource(refreshedValueSet, filePath, encoding, fhirContext);
            IOUtils.updateCachedResource(refreshedValueSet, filePath);
            refreshedValueSetNames.add(refreshedValueSet.getName());
        }

        return refreshedValueSetNames;
    }

    public void loadValueSet(Map<String, String> fileMap, List<ValueSet> valueSets, File valueSetFile) {
        try {
            Resource resource = FormatUtilities.loadFile(valueSetFile.getAbsolutePath());
            ValueSet valueSet = (ValueSet) resource;
            fileMap.put(valueSet.getId(), valueSetFile.getAbsolutePath());
            valueSets.add(valueSet);
        } catch (Exception ex) {
            logMessage(String.format("Error reading valueset: %s. Error: %s", valueSetFile.getAbsolutePath(), ex.getMessage()));
        }
    }

    private List<ValueSet> refreshGeneratedContent( List<ValueSet> valueSets) {
        Copyrights copyrights = new Copyrights();
        for (ValueSet valueSet : valueSets){
            valueSet.setCopyright(copyrights.getCopyrightsText(valueSet));
        }

        return valueSets;
    }

}
