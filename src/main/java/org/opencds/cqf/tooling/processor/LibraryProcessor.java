package org.opencds.cqf.tooling.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.Resource;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.Library;
import org.hl7.fhir.r5.model.Attachment;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.RelatedArtifact;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.opencds.cqf.tooling.npm.LibraryLoader;
import org.opencds.cqf.tooling.npm.NpmPackageManager;
import org.opencds.cqf.tooling.parameter.RefreshLibraryParameters;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class LibraryProcessor extends BaseProcessor {
    public static final String ResourcePrefix = "library-";   
    public static String getId(String baseId) {
        return ResourcePrefix + baseId;
    }

    public static Boolean bundleLibraryDependencies(String path, FhirContext fhirContext, Map<String, IAnyResource> resources,
            Encoding encoding) {
        Boolean shouldPersist = true;
        try {
            Map<String, IAnyResource> dependencies = ResourceUtils.getDepLibraryResources(path, fhirContext, encoding);
            String currentResourceID = FilenameUtils.getBaseName(path);
            for (IAnyResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.getId(), resource);

                // NOTE: Assuming dependency library will be in directory of dependent.
                String dependencyPath = path.replace(currentResourceID, resource.getId().replace("Library/", ""));
                bundleLibraryDependencies(dependencyPath, fhirContext, resources, encoding);
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putException(path, e);
        }
        return shouldPersist;
    }

    private UcumService ucumService;
    private List<String> binaryPaths;
    private CqlProcessor cqlProcessor;

    protected Resource refreshGeneratedContent(Library sourceLibrary) {
        // Find the text/cql content element
        // Pull the file name from the id element that starts with "ig-loader-"
        // Get the source file information from the CqlProcessor (loadFile)
        // If loadFile returns an Attachment, set the Attachment of the library to the result of loadFile
        // Then look up the sourceFileInformation and process it:
        return null;
    }

    public List<Resource> refreshGeneratedContent(List<Library> sourceLibraries) throws IOException {
        binaryPaths = extractBinaryPaths(sourceIg);
        LibraryLoader reader = new LibraryLoader(fhirVersion);
        try {
            ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
        } catch (UcumException e) {
            System.err.println("Could not create UCUM validation service:");
            e.printStackTrace();
        }
        cqlProcessor = new CqlProcessor(packageManager.getNpmList(), binaryPaths, reader, this, ucumService,
                packageId, canonicalBase);

        cqlProcessor.execute();

        List<Resource> resources = new ArrayList<Resource>();
        for (Library library : sourceLibraries) {
            resources.add(refreshGeneratedContent(library));
        }
        return resources;
    }

    private List<String> extractBinaryPaths(ImplementationGuide sourceIg) throws IOException {
        List<String> result = new ArrayList<String>();

        for (ImplementationGuide.ImplementationGuideDefinitionParameterComponent p : sourceIg.getDefinition().getParameter()) {
            // documentation for this list: https://confluence.hl7.org/display/FHIR/Implementation+Guide+Parameters
            if (p.getCode().equals("path-binary")) {
                result.add(Utilities.path(rootDir, p.getValue()));
            }
        }

        return result;
    }

    private Attachment loadFile(String fn) throws FileNotFoundException, IOException {
        for (String dir : binaryPaths) {
            File f = new File(Utilities.path(dir, fn));
            if (f.exists()) {
                Attachment att = new Attachment();
                att.setContentType("text/cql");
                att.setData(TextFile.fileToBytes(f));
                att.setUrl(f.getAbsolutePath());
                return att;
            }
        }
        return null;
    }

/*
    private void performLibraryCQLProcessing(FetchedFile f, org.hl7.fhir.r5.model.Library lib, Attachment attachment) {
        CqlProcessor.CqlSourceFileInformation info = cqlProcessor.getFileInformation(attachment.getUrl());
        if (info != null) {
            f.getErrors().addAll(info.getErrors());
            lib.addContent().setContentType("application/elm+xml").setData(info.getElm());
            if (info.getJsonElm() != null) {
                lib.addContent().setContentType("application/elm+json").setData(info.getJsonElm());
            }
            lib.getDataRequirement().clear();
            lib.getDataRequirement().addAll(info.getDataRequirements());
            lib.getRelatedArtifact().removeIf(n -> n.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON);
            lib.getRelatedArtifact().addAll(info.getRelatedArtifacts());
            lib.getParameter().clear();
            lib.getParameter().addAll(info.getParameters());
        } else {
            f.getErrors().add(new ValidationMessage(ValidationMessage.Source.Publisher, ValidationMessage.IssueType.NOTFOUND, "Library", "No cql info found for "+f.getName(), ValidationMessage.IssueSeverity.ERROR));
        }

    }
 */

    public Boolean refreshLibraryContent(RefreshLibraryParameters params) {
        return false;
    }
}