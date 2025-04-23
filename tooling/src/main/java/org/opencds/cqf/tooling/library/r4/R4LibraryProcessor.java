package org.opencds.cqf.tooling.library.r4;

import ca.uhn.fhir.context.FhirContext;
import com.google.common.base.Strings;
import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.r4.formats.FormatUtilities;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Resource;
import org.opencds.cqf.tooling.common.r4.SoftwareSystemHelper;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.parameter.RefreshLibraryParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import java.io.File;
import java.util.*;

public class R4LibraryProcessor extends LibraryProcessor {
    private String libraryPath;
    private FhirContext fhirContext;
    private Encoding encoding;
    private static SoftwareSystemHelper softwareSystemHelper;

    private String getLibraryPath(String libraryPath) {
        File f = new File(libraryPath);
        if (!f.exists() && f.getParentFile().isDirectory() && f.getParentFile().exists()) {
            return f.getParentFile().toString();
        }
        return libraryPath;
    }

    /*
        Refresh all library resources in the given libraryPath
        If the path is not specified, or is not a known directory, process
        all known library resources
    */
    protected List<String> refreshLibraries(String libraryPath, Encoding encoding, Boolean shouldApplySoftwareSystemStamp) {
        return refreshLibraries(libraryPath, null, encoding, shouldApplySoftwareSystemStamp);
    }

    /*
        Refresh all library resources in the given libraryPath
        If the path is not specified, or is not a known directory, process
        all known library resources, if no libraryOutputDirectory is specified,
        overwrite all known library resources
    */
    protected List<String> refreshLibraries(String libraryPath, String libraryOutputDirectory, Encoding encoding, Boolean shouldApplySoftwareSystemStamp) {
        File file = libraryPath != null ? new File(libraryPath) : null;
        Map<String, String> fileMap = new HashMap<String, String>();
        List<org.hl7.fhir.r5.model.Library> libraries = new ArrayList<>();

        if (file == null || !file.exists()) {
            for (String path : IOUtils.getLibraryPaths(this.fhirContext)) {
                loadLibrary(fileMap, libraries, new File(path));
            }
        }
        else if (file.isDirectory()) {
            for (File libraryFile : Objects.requireNonNull(file.listFiles())) {
                if(IOUtils.isXMLOrJson(libraryPath, libraryFile.getName())) {
                    loadLibrary(fileMap, libraries, libraryFile);
                }
            }
        }
        else {
            loadLibrary(fileMap, libraries, file);
        }

        List<String> refreshedLibraryNames = new ArrayList<String>();
        List<org.hl7.fhir.r5.model.Library> refreshedLibraries = super.refreshGeneratedContent(libraries);
        VersionConvertor_40_50 versionConvertor_40_50 = new VersionConvertor_40_50(new BaseAdvisor_40_50());
        for (org.hl7.fhir.r5.model.Library refreshedLibrary : refreshedLibraries) {
            Library library = (Library) versionConvertor_40_50.convertResource(refreshedLibrary);
            String filePath = null;
            Encoding fileEncoding = null;
            if (fileMap.containsKey(refreshedLibrary.getId()))
            {
                filePath = fileMap.get(refreshedLibrary.getId());
                fileEncoding = IOUtils.getEncoding(filePath);
            } else {
                filePath = getLibraryPath(libraryPath);
                fileEncoding = encoding;
            }
            if (shouldApplySoftwareSystemStamp) {
                softwareSystemHelper.ensureCQFToolingExtensionAndDevice(library, fhirContext);
            }
            // Issue 96
            // Passing the includeVersion here to handle not using the version number in the filename
            if (new File(filePath).exists()) {
                // TODO: This prevents mangled names from being output
                // It would be nice for the tooling to generate library shells, we have enough information to,
                // but the tooling gets confused about the ID and the filename and what gets written is garbage
                //TODO: needs outputPathParameter
                String outputPath = filePath;
                if (libraryOutputDirectory != null) {
                    File libraryDirectory = new File(libraryOutputDirectory);
                    if (!libraryDirectory.exists()) {
                        //TODO: add logger and log non existant directory for writing
                    } else {
                        outputPath = libraryDirectory.getAbsolutePath();
                    }
                }
                IOUtils.writeResource(library, outputPath, fileEncoding, fhirContext, this.versioned, true);
                IOUtils.updateCachedResource(library, outputPath);

                String refreshedLibraryName;
                if (this.versioned && refreshedLibrary.getVersion() != null) {
                    refreshedLibraryName = refreshedLibrary.getName() + "-" + refreshedLibrary.getVersion();
                } else {
                    refreshedLibraryName = refreshedLibrary.getName();
                }
                refreshedLibraryNames.add(refreshedLibraryName);
            }
        }

        return refreshedLibraryNames;
    }

    private void loadLibrary(Map<String, String> fileMap, List<org.hl7.fhir.r5.model.Library> libraries, File libraryFile) {
        try {
            Resource resource = FormatUtilities.loadFile(libraryFile.getAbsolutePath());
            VersionConvertor_40_50 versionConvertor_40_50 = new VersionConvertor_40_50(new BaseAdvisor_40_50());
            org.hl7.fhir.r5.model.Library library = (org.hl7.fhir.r5.model.Library) versionConvertor_40_50.convertResource(resource);
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
        encoding = params.encoding;
        versioned = params.versioned;

        R4LibraryProcessor.softwareSystemHelper = new SoftwareSystemHelper(rootDir);

        if (!Strings.isNullOrEmpty(params.libraryOutputDirectory)) {
            return refreshLibraries(libraryPath, params.libraryOutputDirectory, encoding, params.shouldApplySoftwareSystemStamp);
        } else {
            return refreshLibraries(libraryPath, encoding, params.shouldApplySoftwareSystemStamp);
        }
    }
}
