package org.opencds.cqf.tooling.utilities;

import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.opencds.cqf.tooling.cql.exception.CqlTranslatorException;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.processor.CqlProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CQL-specific I/O utilities extracted from IOUtils.
 * Contains CQL translation, dependency resolution, and library-CQL file association methods.
 */
public class CqlIOUtils {

    private CqlIOUtils() {}

    private static final Map<String, CqlTranslator> cachedTranslator = new LinkedHashMap<>();

    public static CqlTranslator translate(File cqlFile, LibraryManager libraryManager) throws CqlTranslatorException {
        String cqlContentPath = cqlFile.getAbsolutePath();
        CqlTranslator translator = cachedTranslator.get(cqlContentPath);
        if (translator != null) {
            return translator;
        }
        try {
            if (!cqlFile.getName().endsWith(".cql")) {
                throw new CqlTranslatorException("cqlContentPath must be a path to a .cql file");
            }

            translator = CqlTranslator.fromFile(cqlFile, libraryManager);

            if (CqlProcessor.hasSevereErrors(translator.getErrors())) {
                throw new CqlTranslatorException(translator.getErrors());
            }

            cachedTranslator.put(cqlContentPath, translator);
            return translator;
        } catch (IOException e) {
            throw new CqlTranslatorException(e);
        }
    }

    public static List<String> getDependencyCqlPaths(String cqlContentPath, Boolean includeVersion) throws CqlTranslatorException {
        List<File> dependencyFiles = getDependencyCqlFiles(cqlContentPath, includeVersion);
        List<String> dependencyPaths = new ArrayList<>();
        for (File file : dependencyFiles) {
            dependencyPaths.add(file.getPath());
        }
        return dependencyPaths;
    }

    public static List<File> getDependencyCqlFiles(String cqlContentPath, Boolean includeVersion) throws CqlTranslatorException {
        File cqlContent = new File(cqlContentPath);
        File cqlContentDir = cqlContent.getParentFile();
        if (!cqlContentDir.isDirectory()) {
            throw new IllegalArgumentException("The specified path to library files is not a directory");
        }

        List<String> dependencyLibraries = ResourceUtils.getIncludedLibraryNames(cqlContentPath, includeVersion);
        File[] allCqlContentFiles = cqlContentDir.listFiles();
        ArrayList<File> dependencyCqlFiles = new ArrayList<>();

        if (allCqlContentFiles != null) {
            if (allCqlContentFiles.length == 1) {
                return new ArrayList<>();
            }
            for (File cqlFile : allCqlContentFiles) {
                if (dependencyLibraries.contains(IOUtils.getIdFromFileName(cqlFile.getName().replace(".cql", "")))) {
                    dependencyCqlFiles.add(cqlFile);
                    dependencyLibraries.remove(IOUtils.getIdFromFileName(cqlFile.getName().replace(".cql", "")));
                }
            }
        }

        if (!dependencyLibraries.isEmpty()) {
            StringBuilder message = new StringBuilder().append(dependencyLibraries.size())
                    .append(" included cql Libraries not found: ");

            for (String includedLibrary : dependencyLibraries) {
                message.append("\r\n").append(includedLibrary).append(" MISSING");
            }
            throw new RuntimeException(message.toString());
        }
        return dependencyCqlFiles;
    }

    public static String getLibraryPathAssociatedWithCqlFileName(String cqlPath, ca.uhn.fhir.context.FhirContext fhirContext) {
        String libraryPath = null;
        String fileName = FilenameUtils.getName(cqlPath);
        String libraryFileName = LibraryProcessor.ResourcePrefix + fileName;
        for (String path : ResourceDiscovery.getLibraryPaths(fhirContext)) {
            if (path.endsWith(libraryFileName.replaceAll(".cql", ".json"))
                    || path.endsWith(libraryFileName.replaceAll(".cql", ".xml"))
                    || path.endsWith(fileName.replaceAll(".cql", ".json"))
                    || path.endsWith(fileName.replaceAll(".cql", ".xml")))
            {
                libraryPath = path;
                break;
            }
        }
        return libraryPath;
    }

    public static void cleanUp() {
        cachedTranslator.clear();
    }
}
