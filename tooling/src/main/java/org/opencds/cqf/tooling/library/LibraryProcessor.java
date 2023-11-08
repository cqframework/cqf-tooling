package org.opencds.cqf.tooling.library;

import ca.uhn.fhir.context.FhirContext;
import com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.opencds.cqf.tooling.common.ThreadUtils;
import org.opencds.cqf.tooling.library.r4.R4LibraryProcessor;
import org.opencds.cqf.tooling.library.stu3.STU3LibraryProcessor;
import org.opencds.cqf.tooling.parameter.RefreshLibraryParameters;
import org.opencds.cqf.tooling.processor.BaseProcessor;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

public class LibraryProcessor extends BaseProcessor {
    private static final Logger logger = LoggerFactory.getLogger(LibraryProcessor.class);
    public static final String ResourcePrefix = "library-";   
    public static String getId(String baseId) {
        return ResourcePrefix + baseId;
    }
    private static Pattern pattern;

    private static Pattern getPattern() {
        if(pattern == null) {
            String regex = "^[a-zA-Z]+[a-zA-Z0-9_\\-\\.]*";
            pattern = Pattern.compile(regex);
        }
        return pattern;
    }

    public static void validateIdAlphaNumeric(String id) {
        if(!getPattern().matcher(id).find()) {
            throw new RuntimeException("The library id format is invalid.");
        }
    }

    public List<String> refreshIgLibraryContent(BaseProcessor parentContext, Encoding outputEncoding, Boolean versioned, FhirContext fhirContext, Boolean shouldApplySoftwareSystemStamp) {
        return refreshIgLibraryContent(parentContext, outputEncoding, null, versioned, fhirContext, shouldApplySoftwareSystemStamp);
    }
    public List<String> refreshIgLibraryContent(BaseProcessor parentContext, Encoding outputEncoding, String libraryOutputDirectory, Boolean versioned, FhirContext fhirContext, Boolean shouldApplySoftwareSystemStamp) {
        logger.info("Refreshing libraries...");
        // ArrayList<String> refreshedLibraryNames = new ArrayList<String>();

        LibraryProcessor libraryProcessor;
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                libraryProcessor = new STU3LibraryProcessor();
                break;
            case R4:
                libraryProcessor = new R4LibraryProcessor();
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        String libraryPath = FilenameUtils.concat(parentContext.getRootDir(), IGProcessor.libraryPathElement);
        RefreshLibraryParameters params = new RefreshLibraryParameters();
        if (Strings.isNullOrEmpty(libraryOutputDirectory)) {
            logger.info("No output directory found for libraries.  Any existing libraries will be overwritten.");
        } else {
            params.libraryOutputDirectory = libraryOutputDirectory;
        }
        params.libraryPath = libraryPath;
        params.parentContext = parentContext;
        params.fhirContext = fhirContext;
        params.encoding = outputEncoding;
        params.versioned = versioned;
        params.shouldApplySoftwareSystemStamp = shouldApplySoftwareSystemStamp;
        return libraryProcessor.refreshLibraryContent(params);
    }

    public Boolean bundleLibraryDependencies(String path, FhirContext fhirContext, Map<String, IBaseResource> resources,
                                             Encoding encoding, boolean versioned) {
        try{
            Queue<Callable<Void>> bundleLibraryDependenciesTasks = bundleLibraryDependenciesTasks(path, fhirContext, resources, encoding, versioned);
            ThreadUtils.executeTasks(bundleLibraryDependenciesTasks);
            return true;
        }catch (Exception e){
            return false;
        }

    }
    public Queue<Callable<Void>> bundleLibraryDependenciesTasks(String path, FhirContext fhirContext, Map<String, IBaseResource> resources,
                                                               Encoding encoding, boolean versioned) {

        Queue<Callable<Void>> returnTasks = new ConcurrentLinkedQueue<>();

        String fileName = FilenameUtils.getName(path);
        boolean prefixed = fileName.toLowerCase().startsWith("library-");
        try {
            Map<String, IBaseResource> dependencies = ResourceUtils.getDepLibraryResources(path, fhirContext, encoding, versioned, logger);
            // String currentResourceID = IOUtils.getTypeQualifiedResourceId(path, fhirContext);
            for (IBaseResource resource : dependencies.values()) {
                returnTasks.add(() -> {
                    resources.putIfAbsent(resource.getIdElement().getIdPart(), resource);

                    // NOTE: Assuming dependency library will be in directory of dependent.
                    String dependencyPath = IOUtils.getResourceFileName(IOUtils.getResourceDirectory(path), resource, encoding, fhirContext, versioned, prefixed);

                    returnTasks.addAll(bundleLibraryDependenciesTasks(dependencyPath, fhirContext, resources, encoding, versioned));

                    //return statement needed for Callable<Void>
                    return null;
                });
            }
        } catch (Exception e) {
            LogUtils.putException(path, e);
            //purposely break addAll:
            return null;
        }
        return returnTasks;
    }

    protected boolean versioned;

    /*
    Refreshes generated content in the given library.
    The name element of the library resource is used to find the cql file (filename = <name>.cql)
    The CqlProcessor is used to get the CqlSourceFileInformation
    Sets
        * cqlContent
        * elmXmlContent
        * elmJsonContent
        * dataRequirements
        * relatedArtifacts
        * parameters

     Does not set publisher-level information (id, name, url, version, publisher, contact, jurisdiction)
     Does not generate narrative
     */
    protected Library refreshGeneratedContent(Library sourceLibrary) {
        String libraryName = sourceLibrary.getName();
        if (versioned) {
            libraryName += "-" + sourceLibrary.getVersion();
        }
        String fileName = libraryName + ".cql";
        Attachment attachment = null;
        try {
            attachment = loadFile(fileName);
        } catch (IOException e) {
            logMessage(String.format("Error loading CQL source for library %s", libraryName));
            e.printStackTrace();
        }

        if (attachment != null) {
            sourceLibrary.getContent().clear();
            sourceLibrary.getContent().add(attachment);
            setLibraryType(sourceLibrary);
            CqlProcessor.CqlSourceFileInformation info = getCqlProcessor().getFileInformation(attachment.getUrl());
            attachment.setUrlElement(null);
            if (info != null) {
                //f.getErrors().addAll(info.getErrors());
                if (info.getElm() != null) {
                    sourceLibrary.addContent().setContentType("application/elm+xml").setData(info.getElm());
                }
                if (info.getJsonElm() != null) {
                    sourceLibrary.addContent().setContentType("application/elm+json").setData(info.getJsonElm());
                }
                sourceLibrary.getDataRequirement().clear();
                sourceLibrary.getDataRequirement().addAll(info.getDataRequirements());
                sourceLibrary.getRelatedArtifact().removeIf(n -> n.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON);
                sourceLibrary.getRelatedArtifact().addAll(info.getRelatedArtifacts());
                sourceLibrary.getParameter().clear();
                sourceLibrary.getParameter().addAll(info.getParameters());
            } else {
                logMessage(String.format("No cql info found for ", fileName));
                //f.getErrors().add(new ValidationMessage(ValidationMessage.Source.Publisher, ValidationMessage.IssueType.NOTFOUND, "Library", "No cql info found for "+f.getName(), ValidationMessage.IssueSeverity.ERROR));
            }
        }

        return sourceLibrary;
    }

    protected List<Library> refreshGeneratedContent(List<Library> sourceLibraries) {
        return internalRefreshGeneratedContent(sourceLibraries);
    }

    public List<Library> refreshGeneratedContent(String cqlDirectoryPath, String fhirVersion) {
        List<String> result = new ArrayList<String>();
        File input = new File(cqlDirectoryPath);
        if (input.exists() && input.isDirectory()) {
            result.add(input.getAbsolutePath());
        }
        setBinaryPaths(result);

        List<Library> libraries = new ArrayList<Library>();
        return internalRefreshGeneratedContent(libraries);
    }

    private void setLibraryType(Library library) {
        library.setType(new CodeableConcept().addCoding(
                new Coding().setCode("logic-library")
                        .setSystem("http://terminology.hl7.org/CodeSystem/library-type")));
    }

    private List<Library> internalRefreshGeneratedContent(List<Library> sourceLibraries) {
        getCqlProcessor().execute();

        // For each CQL file, ensure that there is a Library resource with a matching name and version
        for (CqlProcessor.CqlSourceFileInformation fileInfo : getCqlProcessor().getAllFileInformation()) {
            if (fileInfo.getIdentifier() != null && fileInfo.getIdentifier().getId() != null && !fileInfo.getIdentifier().getId().equals("")) {
                Library existingLibrary = null;
                for (Library sourceLibrary : sourceLibraries) {
                    if (fileInfo.getIdentifier().getId().equals(sourceLibrary.getName())
                            && (fileInfo.getIdentifier().getVersion() == null || fileInfo.getIdentifier().getVersion().equals(sourceLibrary.getVersion()))
                    ) {
                        existingLibrary = sourceLibrary;
                        break;
                    }
                }

                if (existingLibrary == null) {
                    Library newLibrary = new Library();
                    newLibrary.setName(fileInfo.getIdentifier().getId());
                    newLibrary.setVersion(fileInfo.getIdentifier().getVersion());
                    newLibrary.setUrl(String.format("%s/Library/%s", (newLibrary.getName().equals("FHIRHelpers") ? "http://hl7.org/fhir" : canonicalBase), fileInfo.getIdentifier().getId()));
                    newLibrary.setId(newLibrary.getName() + (versioned ? "-" + newLibrary.getVersion() : ""));
                    setLibraryType(newLibrary);
                    validateIdAlphaNumeric(newLibrary.getId());
                    List<Attachment> attachments = new ArrayList<Attachment>();
                    Attachment attachment = new Attachment();
                    attachment.setContentType("application/elm+xml");
                    attachment.setData(fileInfo.getElm());
                    attachments.add(attachment);
                    newLibrary.setContent(attachments);
                    sourceLibraries.add(newLibrary);
                }
            }
        }

        List<Library> resources = new ArrayList<Library>();
        for (Library library : sourceLibraries) {
            resources.add(refreshGeneratedContent(library));
        }
        return resources;
    }

    private Attachment loadFile(String fn) throws IOException {
        for (String dir : getBinaryPaths()) {
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

    public List<String> refreshLibraryContent(RefreshLibraryParameters params) {
        return new ArrayList<String>();
    }
}