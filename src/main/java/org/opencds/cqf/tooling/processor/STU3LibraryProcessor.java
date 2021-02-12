package org.opencds.cqf.tooling.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.convertors.VersionConvertor_30_50;
import org.hl7.fhir.dstu3.model.Library;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.hl7.fhir.dstu3.model.Resource;
import org.opencds.cqf.tooling.common.stu3.CqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.parameter.RefreshLibraryParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class STU3LibraryProcessor extends LibraryProcessor {
    private String libraryPath;
    private FhirContext fhirContext;
    private static CqfmSoftwareSystemHelper cqfmHelper;

    /*
    Refresh all library resources in the given libraryPath
     */
    protected List<String> refreshLibraries(String libraryPath) {
        File file = new File(libraryPath);
        Map<String, String> fileMap = new HashMap<String, String>();
        List<org.hl7.fhir.r5.model.Library> libraries = new ArrayList<>();
        if (file.isDirectory()) {
            for (File libraryFile : file.listFiles()) {
                loadLibrary(fileMap, libraries, libraryFile);
            }
        }
        else {
            loadLibrary(fileMap, libraries, file);
        }

        List<String> refreshedLibraryNames = new ArrayList<String>();
        List<org.hl7.fhir.r5.model.Library> refreshedLibraries = super.refreshGeneratedContent(libraries);
        for (org.hl7.fhir.r5.model.Library refreshedLibrary : refreshedLibraries) {
            String filePath = fileMap.get(refreshedLibrary.getId());
            org.hl7.fhir.dstu3.model.Library library = (org.hl7.fhir.dstu3.model.Library) VersionConvertor_30_50.convertResource(refreshedLibrary, false);

            cleanseRelatedArtifactReferences(library);

            cqfmHelper.ensureCQFToolingExtensionAndDevice(library, fhirContext);
            IOUtils.writeResource(library, filePath, IOUtils.getEncoding(filePath), fhirContext);
            String refreshedLibraryName;
            if (this.versioned && refreshedLibrary.getVersion() != null) {
                refreshedLibraryName = refreshedLibrary.getName() + "-" + refreshedLibrary.getVersion();
            } else {
                refreshedLibraryName = refreshedLibrary.getName();
            }
            refreshedLibraryNames.add(refreshedLibraryName);
        }

        return refreshedLibraryNames;
    }

    private void cleanseRelatedArtifactReferences(Library library) {
        List<String> unresolvableCodeSystems = Arrays.asList("http://loinc.org", "http://snomed.info/sct");
        List<RelatedArtifact> relatedArtifacts = library.getRelatedArtifact();
        relatedArtifacts.removeIf(ra -> ra.hasResource() && ra.getResource().hasReference() && unresolvableCodeSystems.contains(ra.getResource().getReference()));

        for (RelatedArtifact relatedArtifact : relatedArtifacts) {
            if ((relatedArtifact.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON) && relatedArtifact.hasResource()) {
                String resourceReference = relatedArtifact.getResource().getReference();
                resourceReference = resourceReference.replace("_", "-");
                if (resourceReference.contains("Library/")) {
                    resourceReference = resourceReference.substring(resourceReference.lastIndexOf("Library/"));
                }
      
                if (resourceReference.contains("|")) {
                    if (this.versioned) {
                        String curatedResourceReference = resourceReference.replace("|", "-");
                        relatedArtifact.getResource().setReference(curatedResourceReference);
                    }
                    else {
                        String curatedResourceReference = resourceReference.substring(0, resourceReference.indexOf("|"));
                        relatedArtifact.getResource().setReference(curatedResourceReference);
                    }

                }
            }
        }
    }

    private void loadLibrary(Map<String, String> fileMap, List<org.hl7.fhir.r5.model.Library> libraries, File libraryFile) {
        try {
            Resource resource = (Resource) IOUtils.readResource(libraryFile.getAbsolutePath(), fhirContext);
            org.hl7.fhir.r5.model.Library library = (org.hl7.fhir.r5.model.Library) VersionConvertor_30_50.convertResource(resource, false);
            fileMap.put(library.getId(), libraryFile.getAbsolutePath());
            libraries.add(library);
        } catch (Exception ex) {
            logMessage(String.format("Error reading library: %s. Error: %s", libraryFile.getAbsolutePath(), ex.getMessage()));
        }
    }

    @Override
    public List<String> refreshLibraryContent(RefreshLibraryParameters params) {
        if (params.parentContext != null) {
            initialize(params.parentContext);
        }
        else {
            initializeFromIni(params.ini);
        }

        libraryPath = params.libraryPath;
        fhirContext = params.fhirContext;
        versioned = params.versioned;

        STU3LibraryProcessor.cqfmHelper = new CqfmSoftwareSystemHelper(rootDir);

        return refreshLibraries(libraryPath);
    }
}
