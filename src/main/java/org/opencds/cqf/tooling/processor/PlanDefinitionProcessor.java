package org.opencds.cqf.tooling.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.HttpClientUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class PlanDefinitionProcessor {
    public static final String ResourcePrefix = "plandefinition-";

    public static void bundlePlanDefinitions(ArrayList<String> refreshedLibraryNames, String igPath, Boolean includeDependencies,
            Boolean includeTerminology, Boolean includePatientScenarios, Boolean includeVersion, Boolean cdsHooksIg, FhirContext fhirContext, String fhirUri,
            Encoding encoding) {
        
        HashSet<String> planDefinitionSourcePaths = IOUtils.getPlanDefinitionPaths(fhirContext);

        List<String> planDefinitionPathLibraryNames = new ArrayList<String>();
        for (String planDefinitionSourcePath : planDefinitionSourcePaths) {
            String name = FilenameUtils.getBaseName(planDefinitionSourcePath).replace(PlanDefinitionProcessor.ResourcePrefix, "");

            planDefinitionPathLibraryNames.add(name);
        }

        List<String> bundledPlanDefinitions = new ArrayList<String>();
        for (String refreshedLibraryName : refreshedLibraryNames) {
            try {
                if (!planDefinitionPathLibraryNames.contains(refreshedLibraryName)) {
                    continue;
                }

                Map<String, IAnyResource> resources = new HashMap<String, IAnyResource>();

                String refreshedLibraryFileName = IOUtils.formatFileName(refreshedLibraryName, encoding, fhirContext);
                String librarySourcePath;
                try {
                    librarySourcePath = IOUtils.getLibraryPathAssociatedWithCqlFileName(refreshedLibraryFileName, fhirContext);
                } catch (Exception e) {
                    LogUtils.putException(refreshedLibraryName, e);
                    continue;
                } finally {
                    LogUtils.warn(refreshedLibraryName);
                }
                
                String planDefinitionSourcePath = "";
                for (String path : planDefinitionSourcePaths) {
                    if (FilenameUtils.removeExtension(path).endsWith(refreshedLibraryName))
                    {
                        planDefinitionSourcePath = path;
                        break;
                    }
                }

                Boolean shouldPersist = ResourceUtils.safeAddResource(planDefinitionSourcePath, resources, fhirContext);
                shouldPersist = shouldPersist
                        & ResourceUtils.safeAddResource(librarySourcePath, resources, fhirContext);

                String cqlFileName = IOUtils.formatFileName(refreshedLibraryName, Encoding.CQL, fhirContext);
                List<String> cqlLibrarySourcePaths = IOUtils.getCqlLibraryPaths().stream()
                    .filter(path -> path.endsWith(cqlFileName))
                    .collect(Collectors.toList());
                String cqlLibrarySourcePath = (cqlLibrarySourcePaths.isEmpty()) ? null : cqlLibrarySourcePaths.get(0);
                
                if (includeTerminology) {
                    shouldPersist = shouldPersist
                        & ValueSetsProcessor.bundleValueSets(cqlLibrarySourcePath, igPath, fhirContext, resources, encoding, includeDependencies, includeVersion);
                }

                if (includeDependencies) {
                    shouldPersist = shouldPersist
                        & LibraryProcessor.bundleLibraryDependencies(librarySourcePath, fhirContext, resources, encoding);
                }

                if (includePatientScenarios) {
                    shouldPersist = shouldPersist
                        & TestCaseProcessor.bundleTestCases(igPath, refreshedLibraryName, fhirContext, resources);
                }

                List<String> activityDefinitionPaths =  CDSHooksProcessor.bundleActivityDefinitions(planDefinitionSourcePath, fhirContext, resources, encoding, includeVersion, shouldPersist);

                if (shouldPersist) {
                    String bundleDestPath = FilenameUtils.concat(IGProcessor.getBundlesPath(igPath), refreshedLibraryName);
                    persistBundle(igPath, bundleDestPath, refreshedLibraryName, encoding, fhirContext, new ArrayList<IAnyResource>(resources.values()), fhirUri);
                    bundleFiles(igPath, bundleDestPath, refreshedLibraryName, planDefinitionSourcePath, librarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includePatientScenarios, includeVersion);
                    CDSHooksProcessor.addActivityDefinitionFilesToBundle(igPath, bundleDestPath, refreshedLibraryName, activityDefinitionPaths, fhirContext, encoding);
                    if (cdsHooksIg != null && cdsHooksIg) { 
                        CDSHooksProcessor.addRequestAndResponseFilesToBundle(igPath, bundleDestPath, refreshedLibraryName);
                    }
                    bundledPlanDefinitions.add(refreshedLibraryName);
                }
            } catch (Exception e) {
                LogUtils.putException(refreshedLibraryName, e);
            } finally {
                LogUtils.warn(refreshedLibraryName);
            }
        }
        String message = "\r\n" + bundledPlanDefinitions.size() + " PlanDefinitions successfully bundled:";
        for (String bundledPlanDefinition : bundledPlanDefinitions) {
            message += "\r\n     " + bundledPlanDefinition + " BUNDLED";
        }

        ArrayList<String> failedPlanDefinitions = new ArrayList<>(planDefinitionPathLibraryNames);
        planDefinitionPathLibraryNames.removeAll(bundledPlanDefinitions);
        planDefinitionPathLibraryNames.retainAll(refreshedLibraryNames);
        message += "\r\n" + planDefinitionPathLibraryNames.size() + " PlanDefinitions refreshed, but not bundled (due to issues):";
        for (String notBundled : planDefinitionPathLibraryNames) {
            message += "\r\n     " + notBundled + " REFRESHED";
        }

        failedPlanDefinitions.removeAll(bundledPlanDefinitions);
        failedPlanDefinitions.removeAll(planDefinitionPathLibraryNames);
        message += "\r\n" + failedPlanDefinitions.size() + " PlanDefinitions failed refresh:";
        for (String failed : failedPlanDefinitions) {
            message += "\r\n     " + failed + " FAILED";
        }

        LogUtils.info(message);
    }

    private static void persistBundle(String igPath, String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, List<IAnyResource> resources, String fhirUri) {
        IOUtils.initializeDirectory(bundleDestPath);
        Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext);
        IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);

        if (fhirUri != null && !fhirUri.equals("")) {
            try {
                HttpClientUtils.post(fhirUri, (IAnyResource) bundle, encoding, fhirContext);
            } catch (IOException e) {
                LogUtils.putException(((IAnyResource)bundle).getId(), "Error posting to FHIR Server: " + fhirUri + ".  Bundle not posted.");
                File dir = new File("C:\\src\\GitHub\\logs");
                dir.mkdir();
                IOUtils.writeBundle(bundle, dir.getAbsolutePath(), encoding, fhirContext);
            }
        }
    }

    private static void bundleFiles(String igPath, String bundleDestPath, String libraryName, String resourceFocusSourcePath, String librarySourcePath, FhirContext fhirContext, Encoding encoding, Boolean includeTerminology, Boolean includeDependencies, Boolean includePatientScenarios, Boolean includeVersion) {
        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + IGBundleProcessor.bundleFilesPathElement);
        IOUtils.initializeDirectory(bundleDestFilesPath);

        IOUtils.copyFile(resourceFocusSourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(resourceFocusSourcePath)));
        IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));

        String cqlFileName = IOUtils.formatFileName(libraryName, Encoding.CQL, fhirContext);
        List<String> cqlLibrarySourcePaths = IOUtils.getCqlLibraryPaths().stream()
            .filter(path -> path.endsWith(cqlFileName))
            .collect(Collectors.toList());
        String cqlLibrarySourcePath = (cqlLibrarySourcePaths.isEmpty()) ? null : cqlLibrarySourcePaths.get(0);
        String cqlDestPath = FilenameUtils.concat(bundleDestFilesPath, cqlFileName);
        IOUtils.copyFile(cqlLibrarySourcePath, cqlDestPath);

        if (includeTerminology) {  
            try {     
                Map<String, IAnyResource> valuesets = ResourceUtils.getDepValueSetResources(cqlLibrarySourcePath, igPath, fhirContext, includeDependencies, includeVersion);      
                if (!valuesets.isEmpty()) {
                    Object bundle = BundleUtils.bundleArtifacts(ValueSetsProcessor.getId(libraryName), new ArrayList<IAnyResource>(valuesets.values()), fhirContext);
                    IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);  
                }  
            }  catch (Exception e) {
                LogUtils.putException(libraryName, e.getMessage());
            }       
        }
        
        if (includeDependencies) {
            Map<String, IAnyResource> depLibraries = ResourceUtils.getDepLibraryResources(librarySourcePath, fhirContext, encoding);
            if (!depLibraries.isEmpty()) {
                String depLibrariesID = "library-deps-" + libraryName;
                Object bundle = BundleUtils.bundleArtifacts(depLibrariesID, new ArrayList<IAnyResource>(depLibraries.values()), fhirContext);            
                IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);  
            }        
        }

         if (includePatientScenarios) {
            TestCaseProcessor.bundleTestCaseFiles(igPath, libraryName, bundleDestFilesPath, fhirContext);
        }        
    }
}