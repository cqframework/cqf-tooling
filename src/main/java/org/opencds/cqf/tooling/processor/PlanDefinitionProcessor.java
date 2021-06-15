package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.utilities.*;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
/**
 * @author Adam Stevenson
 */
public class PlanDefinitionProcessor {
    public static final String ResourcePrefix = "plandefinition-";
    public static final String PlanDefinitionTestGroupName = "plandefinition";
    private LibraryProcessor libraryProcessor;
    private CDSHooksProcessor cdsHooksProcessor;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public PlanDefinitionProcessor(LibraryProcessor libraryProcessor, CDSHooksProcessor cdsHooksProcessor) {
        this.libraryProcessor = libraryProcessor;
        this.cdsHooksProcessor = cdsHooksProcessor;
    }

    public void bundlePlanDefinitions(ArrayList<String> refreshedLibraryNames, String igPath, Boolean includeDependencies,
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

                Map<String, IBaseResource> resources = new HashMap<String, IBaseResource>();

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
                    boolean result = ValueSetsProcessor.bundleValueSets(cqlLibrarySourcePath, igPath, fhirContext, resources, encoding, includeDependencies, includeVersion);
                    if (shouldPersist && !result) {
                        LogUtils.info("PlanDefinitions will not be bundled because ValueSet bundling failed.");
                    }
                    shouldPersist = shouldPersist & result;
                }

                if (includeDependencies) {
                    boolean result = libraryProcessor.bundleLibraryDependencies(librarySourcePath, fhirContext, resources, encoding, includeVersion);
                    if (shouldPersist && !result) {
                        LogUtils.info(String.format("PlanDefinitions will not be bundled because Library Dependency bundling failed. Library: '%s'", refreshedLibraryName));
                    }
                    shouldPersist = shouldPersist & result;
                }

                if (includePatientScenarios) {
                    boolean result = TestCaseProcessor.bundleTestCases(igPath, PlanDefinitionTestGroupName, refreshedLibraryName, fhirContext, resources);
                    if (shouldPersist && !result) {
                        LogUtils.info("PlanDefinitions will not be bundled because Test Case bundling failed.");
                    }
                    shouldPersist = shouldPersist & result;
                }

                List<String> activityDefinitionPaths =  CDSHooksProcessor.bundleActivityDefinitions(planDefinitionSourcePath, fhirContext, resources, encoding, includeVersion, shouldPersist);

                if (shouldPersist) {
                    String bundleDestPath = FilenameUtils.concat(FilenameUtils.concat(IGProcessor.getBundlesPath(igPath), PlanDefinitionTestGroupName), refreshedLibraryName);
                    persistBundle(igPath, bundleDestPath, refreshedLibraryName, encoding, fhirContext, new ArrayList<IBaseResource>(resources.values()), fhirUri);
                    bundleFiles(igPath, bundleDestPath, refreshedLibraryName, planDefinitionSourcePath, librarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includePatientScenarios, includeVersion);
                    cdsHooksProcessor.addActivityDefinitionFilesToBundle(igPath, bundleDestPath, refreshedLibraryName, activityDefinitionPaths, fhirContext, encoding);
                    if (cdsHooksIg != null && cdsHooksIg) { 
                        cdsHooksProcessor.addRequestAndResponseFilesToBundle(igPath, bundleDestPath, refreshedLibraryName);
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

    private void persistBundle(String igPath, String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, List<IBaseResource> resources, String fhirUri) {
        IOUtils.initializeDirectory(bundleDestPath);
        Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext);
        IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);

        if (fhirUri != null && !fhirUri.equals("")) {
            try {
                HttpClientUtils.post(fhirUri, (IBaseResource) bundle, encoding, fhirContext);
            } catch (IOException e) {
                LogUtils.putException(((IBaseResource)bundle).getIdElement().getIdPart(), "Error posting to FHIR Server: " + fhirUri + ".  Bundle not posted.");
                File dir = new File("C:\\src\\GitHub\\logs");
                dir.mkdir();
                IOUtils.writeBundle(bundle, dir.getAbsolutePath(), encoding, fhirContext);
            }
        }
    }

    private void bundleFiles(String igPath, String bundleDestPath, String libraryName, String resourceFocusSourcePath, String librarySourcePath, FhirContext fhirContext, Encoding encoding, Boolean includeTerminology, Boolean includeDependencies, Boolean includePatientScenarios, Boolean includeVersion) {
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
                Map<String, IBaseResource> valuesets = ResourceUtils.getDepValueSetResources(cqlLibrarySourcePath, igPath, fhirContext, includeDependencies, includeVersion);      
                if (!valuesets.isEmpty()) {
                    Object bundle = BundleUtils.bundleArtifacts(ValueSetsProcessor.getId(libraryName), new ArrayList<IBaseResource>(valuesets.values()), fhirContext);
                    IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);  
                }  
            }  catch (Exception e) {
                LogUtils.putException(libraryName, e.getMessage());
            }       
        }
        
        if (includeDependencies) {
            Map<String, IBaseResource> depLibraries = ResourceUtils.getDepLibraryResources(librarySourcePath, fhirContext, encoding, includeVersion, logger);
            if (!depLibraries.isEmpty()) {
                String depLibrariesID = "library-deps-" + libraryName;
                Object bundle = BundleUtils.bundleArtifacts(depLibrariesID, new ArrayList<IBaseResource>(depLibraries.values()), fhirContext);            
                IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);  
            }        
        }

         if (includePatientScenarios) {
            TestCaseProcessor.bundleTestCaseFiles(igPath, "plandefinition", libraryName, bundleDestFilesPath, fhirContext);
        }        
    }
}