package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeCompositeDatatypeDefinition;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.util.BundleBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.*;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.Utilities;
import org.opencds.cqf.tooling.cql.exception.CqlTranslatorException;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IOUtils {
    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    public enum Encoding {
        CQL("cql"), JSON("json"), XML("xml"), UNKNOWN("");

        private final String value;

        @Override
        public String toString() {
            return this.value;
        }

        Encoding(String string) {
            this.value = string;
        }

        public static Encoding parse(String value) {
            if (value == null) {
                return UNKNOWN;
            }

            switch (value.trim().toLowerCase()) {
                case "cql":
                    return CQL;
                case "json":
                    return JSON;
                case "xml":
                    return XML;
                default:
                    return UNKNOWN;
            }
        }
    }

    public static List<String> resourceDirectories = new ArrayList<>();

    public static String getIdFromFileName(String fileName) {
        return fileName.replace("_", "-");
    }

    public static byte[] encodeResource(IBaseResource resource, Encoding encoding, FhirContext fhirContext) {
        return encodeResource(resource, encoding, fhirContext, false);
    }

    public static byte[] encodeResource(IBaseResource resource, Encoding encoding, FhirContext fhirContext,
                                        boolean prettyPrintOutput) {
        if (encoding == Encoding.UNKNOWN) {
            return new byte[] { };
        }
        IParser parser = getParser(encoding, fhirContext);
        return parser.setPrettyPrint(prettyPrintOutput).encodeResourceToString(resource).getBytes();
    }

    public static String getFileContent(File file) {
        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException("Error reading file: " + e.getMessage());
        }
    }


    public static String encodeResourceAsString(IBaseResource resource, Encoding encoding, FhirContext fhirContext) {
        if (encoding == Encoding.UNKNOWN) {
            return "";
        }
        IParser parser = getParser(encoding, fhirContext);
        return parser.setPrettyPrint(true).encodeResourceToString(resource);
    }

    // Issue 96 - adding second signature to allow for passing versioned
    public static <T extends IBaseResource> void writeResource(T resource, String path, Encoding encoding,
                                                               FhirContext fhirContext) {
        writeResource(resource, path, encoding, fhirContext, true);
    }

    public static <T extends IBaseResource> void writeResource(T resource, String path, Encoding encoding,
                                                               FhirContext fhirContext, Boolean versioned) {
        writeResource(resource, path, encoding, fhirContext, versioned, null, true);
    }

    public static <T extends IBaseResource> void writeResource(T resource, String path, Encoding encoding,
                                                               FhirContext fhirContext, Boolean versioned,
                                                               String outputFileName) {
        writeResource(resource, path, encoding, fhirContext, versioned, outputFileName, true);
    }

    public static <T extends IBaseResource> void writeResource(T resource, String path, Encoding encoding,
                                                               FhirContext fhirContext, Boolean versioned,
                                                               boolean prettyPrintOutput) {
        writeResource(resource, path, encoding, fhirContext, versioned, null, prettyPrintOutput);
    }

    public static <T extends IBaseResource> void writeResource(T resource, String path, Encoding encoding,
                                                               FhirContext fhirContext, Boolean versioned,
                                                               String outputFileName, boolean prettyPrintOutput) {
        // If the path is to a specific resource file, just re-use that file path/name.
        String outputPath;
        File file = new File(path);
        if (file.isFile()) {
            outputPath = path;
        }
        else {
            ensurePath(path);

            String baseName;
            if (outputFileName == null || outputFileName.isBlank()) {
                baseName = resource.getIdElement().getIdPart();
            } else {
                baseName = outputFileName;
            }

            // Issue 96
            // If includeVersion is false then just use name and not id for the file baseName
//            if (Boolean.FALSE.equals(versioned)) {
            // Assumes that the id will be a string with - separating the version number
            // baseName = baseName.split("-")[0];
//            }
            outputPath = FilenameUtils.concat(path, formatFileName(baseName, encoding, fhirContext));
        }

        try (FileOutputStream writer = new FileOutputStream(outputPath)) {
            writer.write(encodeResource(resource, encoding, fhirContext, prettyPrintOutput));
            writer.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException("Error writing Resource to file: " + e.getMessage());
        }
    }

    public static <T extends IBaseResource> void writeResources(List<T> resources, String path,
                                                                Encoding encoding, FhirContext fhirContext) {
        resources.forEach(resource -> writeResource(resource, path, encoding, fhirContext));
    }

    public static <T extends IBaseResource> void writeResources(Map<String, T> resources, String path, Encoding encoding, FhirContext fhirContext) {
        for (Map.Entry<String, T> set : resources.entrySet()) {
            writeResource(set.getValue(), path, encoding, fhirContext);
        }
    }

    //There's a special operation to write a bundle because I can't find a type that will reference both dstu3 and r4.
    public static void writeBundle(Object bundle, String path, Encoding encoding, FhirContext fhirContext) {
        writeBundle(bundle, path, encoding, fhirContext, null);
    }

    public static void writeBundle(Object bundle, String path, Encoding encoding, FhirContext fhirContext, String outputFileName) {
        writeBundle(bundle, path, encoding, fhirContext, outputFileName, false);
    }

    public static void writeBundle(Object bundle, String path, Encoding encoding, FhirContext fhirContext, boolean prettyPrintOutput) {
        writeBundle(bundle, path, encoding, fhirContext, null, prettyPrintOutput);
    }

    public static void writeBundle(Object bundle, String path, Encoding encoding, FhirContext fhirContext, String outputFileName, boolean prettyPrintOutput) {
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                writeResource(((org.hl7.fhir.dstu3.model.Bundle)bundle), path, encoding, fhirContext, true, outputFileName, prettyPrintOutput);
                break;
            case R4:
                writeResource(((org.hl7.fhir.r4.model.Bundle)bundle), path, encoding, fhirContext, true, outputFileName, prettyPrintOutput);
                break;
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    public static void writeCqlToFile(String cql, String filePath) {
        try (FileOutputStream writer = new FileOutputStream(filePath)) {
            writer.write(cql.getBytes(StandardCharsets.UTF_8));
            writer.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException("Error writing Resource to file: " + e.getMessage());
        }
    }

    private static final Map<String, String> alreadyCopied = new ConcurrentHashMap<>();

    public static void copyFile(String inputPath, String outputPath) {

        if ((inputPath == null || inputPath.isEmpty()) &&
                (outputPath == null || outputPath.isEmpty())) {
            LogUtils.putException("IOUtils.copyFile", new IllegalArgumentException("IOUtils.copyFile: inputPath and outputPath are missing!"));
            return;
        }

        if (inputPath == null || inputPath.isEmpty()) {
            LogUtils.putException("IOUtils.copyFile", new IllegalArgumentException("IOUtils.copyFile: inputPath missing!"));
            return;
        }

        if (outputPath == null || outputPath.isEmpty()) {
            LogUtils.putException("IOUtils.copyFile", new IllegalArgumentException("IOUtils.copyFile: inputPath missing!"));
            return;
        }

        String key = inputPath + ":" + outputPath;
        if (alreadyCopied.containsKey(key)) {
            // File already copied to destination, no need to do anything
            return;
        }

        try {
            Path src = Paths.get(inputPath);
            Path dest = Paths.get(outputPath);
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);

            alreadyCopied.put(key, outputPath);
        } catch (IOException e) {
            logger.error(e.getMessage());
            LogUtils.putException("IOUtils.copyFile(" + inputPath + ", " + outputPath + "): ",
                    new RuntimeException("Error copying file: " + e.getMessage()));
        }
    }


    public static String getTypeQualifiedResourceId(String path, FhirContext fhirContext) {
        IBaseResource resource = readResource(path, fhirContext, true);
        if (resource != null) {
            return resource.getIdElement().getResourceType() + "/" + resource.getIdElement().getIdPart();
        }

        return null;
    }

    public static String getCanonicalResourceVersion(IBaseResource resource, FhirContext fhirContext) {
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                if (resource instanceof org.hl7.fhir.dstu3.model.MetadataResource) {
                    return ((org.hl7.fhir.dstu3.model.MetadataResource)resource).getVersion();
                }
                break;
            case R4:
                if (resource instanceof org.hl7.fhir.r4.model.MetadataResource) {
                    return ((org.hl7.fhir.r4.model.MetadataResource)resource).getVersion();
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        return null;
    }

    public static IBaseResource readResource(String path, FhirContext fhirContext) {
        return readResource(path, fhirContext, false);
    }

    //users should always check for null
    private static final Map<String, IBaseResource> cachedResources = new LinkedHashMap<>();
    public static IBaseResource readResource(String path, FhirContext fhirContext, Boolean safeRead) {
        Encoding encoding = getEncoding(path);
        if (encoding == Encoding.UNKNOWN || encoding == Encoding.CQL) {
            return null;
        }

        IBaseResource resource = cachedResources.get(path);
        if (resource != null) {
            return resource;
        }

        try {
            IParser parser = getParser(encoding, fhirContext);
            File file = new File(path);

            if (file.exists() && file.isDirectory()) {
                throw new IllegalArgumentException(String.format("Cannot read a resource from a directory: %s", path));
            }

            if (Boolean.TRUE.equals(safeRead) && !file.exists()) {
                return null;
            }
            try (FileReader reader = new FileReader(file)) {
                resource = parser.parseResource(reader);
            }
            cachedResources.put(path, resource);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error reading resource from path %s: %s", path, e.getMessage()), e);
        }
        return resource;
    }

    public static void updateCachedResource(IBaseResource updatedResource, String path) {
        cachedResources.computeIfPresent(path, (key, value) -> updatedResource);
    }

    public static List<IBaseResource> readResources(List<String> paths, FhirContext fhirContext) {
        List<IBaseResource> resources = new ArrayList<>();
        for (String path : paths) {
            IBaseResource resource = readResource(path, fhirContext);
            if (resource != null) {
                resources.add(resource);
            }
        }
        return resources;
    }

    public static IBaseResource readJsonResourceIgnoreElements(String path, FhirContext fhirContext, String... elements) {
        Encoding encoding = getEncoding(path);
        if (encoding == Encoding.UNKNOWN || encoding == Encoding.CQL || encoding == Encoding.XML) {
            return null;
        }

        if (cachedResources.containsKey(path)) {
            return cachedResources.get(path);
        }

        IParser parser = getParser(encoding, fhirContext);
        try (FileReader reader = new FileReader(path)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            Arrays.stream(elements).forEach(obj::remove);
            IBaseResource resource = parser.parseResource(obj.toString());
            cachedResources.put(path, resource);
            return resource;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(String.format("Error reading resource from path %s: %s", path, e));
        }
    }

    public static List<IBaseResource> getResourcesInDirectory(String directoryPath, FhirContext fhirContext, Boolean recursive) {
        var resources = new ArrayList<IBaseResource>();
        var fileIterator = FileUtils.iterateFiles(new File(directoryPath), new String[]{ "xml", "json" }, recursive);
        while (fileIterator.hasNext()) {
            var resource = readResource(fileIterator.next().getAbsolutePath(), fhirContext, true);
            if (resource != null) {
                resources.add(resource);
            }
        }
        return resources;
    }

    public static List<IBaseResource> getResourcesOfTypeInDirectory(String directoryPath, FhirContext fhirContext, Class<? extends IBaseResource> clazz, Boolean recursive) {
        var resources = new ArrayList<IBaseResource>();
        var fileIterator = FileUtils.iterateFiles(new File(directoryPath), new String[]{ "xml", "json" }, recursive);
        while (fileIterator.hasNext()) {
            var resource = readResource(fileIterator.next().getAbsolutePath(), fhirContext, true);
            if (resource != null && resource.getClass().isAssignableFrom(clazz)) {
                resources.add(resource);
            }
        }
        return resources;
    }

    public static IBaseBundle bundleResourcesInDirectory(String directoryPath, FhirContext fhirContext, Boolean recursive) {
        BundleBuilder builder = new BundleBuilder(fhirContext);
        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(directoryPath), new String[]{ "xml", "json" }, recursive);
        while (fileIterator.hasNext()) {
            builder.addCollectionEntry(readResource(fileIterator.next().getAbsolutePath(), fhirContext));
        }
        return builder.getBundle();
    }

    public static IBaseBundle bundleResourcesInDirectoryAsTransaction(String directoryPath, FhirContext fhirContext, Boolean recursive) {
        BundleBuilder builder = new BundleBuilder(fhirContext);
        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(directoryPath), new String[]{ "xml", "json" }, recursive);
        while (fileIterator.hasNext()) {
            builder.addTransactionUpdateEntry(readResource(fileIterator.next().getAbsolutePath(), fhirContext));
        }
        return builder.getBundle();
    }

    public static boolean isDirectory(String path) {
        return FileUtils.isDirectory(new File(path));
    }
    private static final Map<String, List<String>> cachedFilePaths = new ConcurrentHashMap<>();

    public static List<String> getFilePaths(String directoryPath, Boolean recursive) {
        List<String> filePaths = new ArrayList<>();
        String key = directoryPath + ":" + recursive;

        if (IOUtils.cachedFilePaths.containsKey(key)) {
            filePaths = IOUtils.cachedFilePaths.get(key);
            return filePaths;
        }
        File inputDir = new File(directoryPath);
        ArrayList<File> files = inputDir.isDirectory()
                ? new ArrayList<>(Arrays.asList(Optional.ofNullable(
                inputDir.listFiles()).orElseThrow(NoSuchElementException::new)))
                : new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                //note: this is not the same as ANDing recursive to isDirectory as that would result in directories
                // being added to the list if the request is not recursive.
                if (Boolean.TRUE.equals(recursive)) {
                    filePaths.addAll(getFilePaths(file.getPath(), true));
                }
            } else {
                filePaths.add(file.getPath());
            }
        }
        cachedFilePaths.put(key, filePaths);
        return filePaths;
    }

    public static String getResourceFileName(String resourcePath, IBaseResource resource, Encoding encoding,
                                             FhirContext fhirContext, boolean versioned, boolean prefixed) {
        String resourceVersion = IOUtils.getCanonicalResourceVersion(resource, fhirContext);
        String filename = resource.getIdElement().getIdPart();
        // Issue 96
        // Handle no version on filename but still in id
        if (!versioned && resourceVersion != null) {
            int index = filename.indexOf(resourceVersion);
            if (index > 0) {
                filename = filename.substring(0, index - 1);
            }
        } else if (versioned && resourceVersion != null) {
            int index = filename.indexOf(resourceVersion);
            if (index < 0) {
                filename = filename + "-" + resourceVersion;
            }
        }

        String resourceType = resource.fhirType().toLowerCase();
        return Paths.get(resourcePath, resourceType, (prefixed ? (resourceType + "-") : "")
                + filename) + getFileExtension(encoding);
    }

    // Returns the parent directory if it is named resources, otherwise, the parent of that
    public static String getResourceDirectory(String path) {
        String result = getParentDirectoryPath(path);
        if (!result.toLowerCase().endsWith("resources")) {
            result = getParentDirectoryPath(result);
        }

        return result;
    }

    public static String getParentDirectoryPath(String path) {
        File file = new File(path);
        return file.getParent();
    }

    private static final Map<String, List<String>> cachedDirectoryPaths = new ConcurrentHashMap<>();

    public static List<String> getDirectoryPaths(String path, Boolean recursive) {
        List<String> directoryPaths = new ArrayList<>();
        String key = path + ":" + recursive;
        if (IOUtils.cachedDirectoryPaths.containsKey(key)) {
            directoryPaths = IOUtils.cachedDirectoryPaths.get(key);
            return directoryPaths;
        }
        List<File> directories;
        File parentDirectory = new File(path);
        try {
            directories = Arrays.asList(Optional.ofNullable(parentDirectory.listFiles())
                    .orElseThrow(NoSuchElementException::new));
        } catch (Exception e) {
//            logger.error("No paths found for the Directory {}:", path);
            return directoryPaths;
        }


        for (File directory : directories) {
            if (directory.isDirectory()) {
                if (Boolean.TRUE.equals(recursive)) {
                    directoryPaths.addAll(getDirectoryPaths(directory.getPath(), recursive));
                }
                directoryPaths.add(directory.getPath());
            }
        }
        cachedDirectoryPaths.put(key, directoryPaths);
        return directoryPaths;
    }

    public static void initializeDirectory(String path) {
        File directory = new File(path);
        if (directory.exists()) {
            try {
                deleteDirectory(path);
            } catch (IOException e) {
                logger.error(e.getMessage());
                throw new RuntimeException("Error deleting directory: " + path + " - " + e.getMessage());
            }
        }

        if (!directory.mkdir()) {
            logger.warn("Unable to initialize directory at {}", path);
        }
    }

    public static void deleteDirectory(String path) throws IOException {
        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                Files.delete(file); // this will work because it's always a File
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir); //this will work because Files in the directory are already deleted
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static Encoding getEncoding(String path)
    {
        return Encoding.parse(FilenameUtils.getExtension(path));
    }

    //users should protect against Encoding.UNKNOWN or Encoding.CQL
    private static IParser getParser(Encoding encoding, FhirContext fhirContext) {
        switch (encoding) {
            case XML:
                return fhirContext.newXmlParser();
            case JSON:
                return fhirContext.newJsonParser();
            default:
                throw new RuntimeException("Unknown encoding type: " + encoding);
        }
    }

    public static Boolean pathEndsWithElement(String igPath, String pathElement) {
        boolean result = false;
        try {
            String baseElement = FilenameUtils.getBaseName(igPath).isEmpty() ? FilenameUtils.getBaseName(FilenameUtils.getFullPathNoEndSeparator(igPath)) : FilenameUtils.getBaseName(igPath);
            result = baseElement.equals(pathElement);
        } catch (Exception ignored) {}
        return result;
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
                if (dependencyLibraries.contains(getIdFromFileName(cqlFile.getName().replace(".cql", "")))) {
                    dependencyCqlFiles.add(cqlFile);
                    dependencyLibraries.remove(getIdFromFileName(cqlFile.getName().replace(".cql", "")));
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

    public static String getCqlString(String cqlContentPath) {
        File cqlFile = new File(cqlContentPath);
        StringBuilder cql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(cqlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                cql.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new IllegalArgumentException("Error reading CQL file: " + cqlFile.getName());
        }
        return cql.toString();
    }

    public static String getFileExtension(Encoding encoding) {
        return "." + encoding.toString();
    }

    public static String formatFileName(String baseName, Encoding encoding, FhirContext fhirContext) {
        //I think this should really just be the version name i.e. DSTU3 or R4
        String igVersionToken;
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                igVersionToken = "FHIR3";
                break;
            case R4:
                igVersionToken = "FHIR4";
                break;
            default:
                igVersionToken = "";
        }
        String result = baseName + getFileExtension(encoding);
        if (encoding == Encoding.CQL) {
            result = result.replace("-" + igVersionToken, "_" + igVersionToken);
        }

        return result;
    }

    public static void putAllInListIfAbsent(List<String> values, List<String> list) {
        for (String value : values) {
            if (!list.contains(value)) {
                list.add(value);
            }
        }
    }

    public static void putInListIfAbsent(String value, List<String> list) {
        if (!list.contains(value)) {
            list.add(value);
        }
    }

    public static String getLibraryPathAssociatedWithCqlFileName(String cqlPath, FhirContext fhirContext) {
        String libraryPath = null;
        String fileName = FilenameUtils.getName(cqlPath);
        String libraryFileName = LibraryProcessor.ResourcePrefix + fileName;
        for (String path : IOUtils.getLibraryPaths(fhirContext)) {
            // NOTE: A bit of a hack, but we need to support both xml and json encodings for existing resources and the long-term strategy is
            // to revisit this and change the approach to use the references rather than file name matching, so this should be good for the near-term.
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

    private static final Set<String> cqlLibraryPaths = new LinkedHashSet<>();
    public static Set<String> getCqlLibraryPaths() {
        if (cqlLibraryPaths.isEmpty()) {
            setupCqlLibraryPaths();
        }
        return cqlLibraryPaths;
    }
    private static void setupCqlLibraryPaths() {
        //need to add an error report for bad resource paths
        for (String dir : resourceDirectories) {
            List<String> filePaths = IOUtils.getFilePaths(dir, true);
            filePaths.stream().filter(path -> path.contains(".cql")).forEach(cqlLibraryPaths::add);
        }
    }

    public static String getCqlLibrarySourcePath(String libraryName, String cqlFileName, List<String> binaryPaths) {
        // Old way, requires the resourcePaths argument to include cql directories, which is wrong
        List<String> cqlLibrarySourcePaths = IOUtils.getCqlLibraryPaths().stream()
                .filter(path -> path.endsWith(cqlFileName))
                .collect(Collectors.toList());
        String cqlLibrarySourcePath = (cqlLibrarySourcePaths.isEmpty()) ? null : cqlLibrarySourcePaths.get(0);

        // Correct way, uses the binaryPaths loaded from the BaseProcessor (passed here because static)
        try {
            if (cqlLibrarySourcePath == null) {
                for (String path : binaryPaths) {
                    File f = new File(Utilities.path(path, cqlFileName));
                    if (f.exists()) {
                        cqlLibrarySourcePath = f.getAbsolutePath();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            LogUtils.putException(libraryName, e);
        }

        return cqlLibrarySourcePath;
    }

    private static final Set<String> terminologyPaths = new LinkedHashSet<>();
    public static Set<String> getTerminologyPaths(FhirContext fhirContext) {
        if (terminologyPaths.isEmpty()) {
            setupTerminologyPaths(fhirContext);
        }
        return terminologyPaths;
    }
    private static void setupTerminologyPaths(FhirContext fhirContext) {
        HashMap<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : resourceDirectories) {
            for (String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    if (path.toLowerCase().contains("valuesets") || path.toLowerCase().contains("valueset")) {
                        logger.error("Error reading in Terminology from path: {} \n {}", path, e.getMessage());
                    }
                }
            }
            //TODO: move these to ResourceUtils
            RuntimeResourceDefinition valuesetDefinition = ResourceUtils.getResourceDefinition(fhirContext, "ValueSet");
            RuntimeCompositeDatatypeDefinition conceptDefinition = (RuntimeCompositeDatatypeDefinition)ResourceUtils.getElementDefinition(fhirContext, "CodeableConcept");
            RuntimeCompositeDatatypeDefinition codingDefinition = (RuntimeCompositeDatatypeDefinition)ResourceUtils.getElementDefinition(fhirContext, "Coding");
            String valuesetClassName = valuesetDefinition.getImplementingClass().getName();
            String conceptClassName = conceptDefinition.getImplementingClass().getName();
            String codingClassName = codingDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->
                            valuesetClassName.equals(entry.getValue().getClass().getName())
                                    || conceptClassName.equals(entry.getValue().getClass().getName())
                                    || codingClassName.equals(entry.getValue().getClass().getName())
                    )
                    .forEach(entry -> terminologyPaths.add(entry.getKey()));
        }
    }

    public static IBaseResource getLibraryByUrl(FhirContext fhirContext, String url) {
        IBaseResource library = getLibraryUrlMap(fhirContext).get(url);
        if (library == null) {
            throw new IllegalArgumentException(String.format("Could not load library with url %s", url));
        }
        return library;
    }

    private static final Set<String> libraryPaths = new LinkedHashSet<>();
    public static Set<String> getLibraryPaths(FhirContext fhirContext) {
        if (libraryPaths.isEmpty()) {
            setupLibraryPaths(fhirContext);
        }
        return libraryPaths;
    }
    private static final Map<String, IBaseResource> libraryUrlMap = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getLibraryUrlMap(FhirContext fhirContext) {
        if (libraryPathMap.isEmpty()) {
            setupLibraryPaths(fhirContext);
        }
//        LogUtils.info(String.format("libraryUrlMap Size: %d", libraryPathMap.size()));
//        for (Map.Entry<String, IBaseResource> e : libraryUrlMap.entrySet()) {
//            LogUtils.info(String.format("libraryUrlMap Entry: %s", e.getKey()));
//        }
        return libraryUrlMap;
    }
    private static final Map<String, String> libraryPathMap = new LinkedHashMap<>();
    public static Map<String, String> getLibraryPathMap(FhirContext fhirContext) {
        if (libraryPathMap.isEmpty()) {
            setupLibraryPaths(fhirContext);
        }
        return libraryPathMap;
    }
    private static final Map<String, IBaseResource> libraries = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getLibraries(FhirContext fhirContext) {
        if (libraries.isEmpty()) {
            setupLibraryPaths(fhirContext);
        }
        return libraries;
    }
    private static void setupLibraryPaths(FhirContext fhirContext) {
        Map<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    IBaseResource resource = IOUtils.readResource(path, fhirContext, true);
                    resources.put(path, resource);
                } catch (Exception e) {
                    if(path.toLowerCase().contains("library")) {
                        logger.error("Error reading in Library from path: {} \n {}", path, e.getMessage());
                    }
                }
            }
            //TODO: move these to ResourceUtils
            RuntimeResourceDefinition libraryDefinition = ResourceUtils.getResourceDefinition(fhirContext, "Library");
            String libraryClassName = libraryDefinition.getImplementingClass().getName();
            // BaseRuntimeChildDefinition urlElement = libraryDefinition.getChildByNameOrThrowDataFormatException("url");
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  libraryClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> {
                        libraryPaths.add(entry.getKey());
                        libraries.put(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                        libraryPathMap.put(entry.getValue().getIdElement().getIdPart(), entry.getKey());
                        String url = ResourceUtils.getUrl(entry.getValue(), fhirContext);
                        var version = ResourceUtils.getVersion(entry.getValue(), fhirContext);
                        if (url != null) {
                            libraryUrlMap.put(ResourceUtils.getUrl(entry.getValue(), fhirContext), entry.getValue());
                            if (version != null) {
                                libraryUrlMap.put(ResourceUtils.getUrl(entry.getValue(), fhirContext) + "|" + version, entry.getValue());
                            }
                        }
                    });
        }
    }

    private static final Set<String> measurePaths = new LinkedHashSet<>();
    public static Set<String> getMeasurePaths(FhirContext fhirContext) {
        if (measurePaths.isEmpty()) {
            setupMeasurePaths(fhirContext);
        }
        return measurePaths;
    }
    private static final Map<String, String> measurePathMap = new LinkedHashMap<>();
    public static Map<String, String> getMeasurePathMap(FhirContext fhirContext) {
        if (measurePathMap.isEmpty()) {
            setupMeasurePaths(fhirContext);
        }
        return measurePathMap;
    }
    private static final Map<String, IBaseResource> measures = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getMeasures(FhirContext fhirContext) {
        if (measures.isEmpty()) {
            setupMeasurePaths(fhirContext);
        }
        return measures;
    }
    private static void setupMeasurePaths(FhirContext fhirContext) {
        Map<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    IBaseResource resource = IOUtils.readResource(path, fhirContext, true);
                    resources.put(path, resource);
                } catch (Exception e) {
                    if(path.toLowerCase().contains("measure")) {
                        logger.error("Error reading in Measure from path: " + path, e);
                    }
                }
            }
            //TODO: move these to ResourceUtils
            RuntimeResourceDefinition measureDefinition = ResourceUtils.getResourceDefinition(fhirContext, "Measure");
            String measureClassName = measureDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  measureClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> {
                        measurePaths.add(entry.getKey());
                        measures.put(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                        measurePathMap.put(entry.getValue().getIdElement().getIdPart(), entry.getKey());
                    });
        }
    }

    private static final Set<String> measureReportPaths = new LinkedHashSet<>();
    public static Set<String> getMeasureReportPaths(FhirContext fhirContext) {
        if (measureReportPaths.isEmpty()) {
            setupMeasureReportPaths(fhirContext);
        }
        return measureReportPaths;
    }
    private static void setupMeasureReportPaths(FhirContext fhirContext) {
        HashMap<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
            //TODO: move these to ResourceUtils
            RuntimeResourceDefinition measureReportDefinition = ResourceUtils.getResourceDefinition(fhirContext, "MeasureReport");
            String measureReportClassName = measureReportDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  measureReportClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> measureReportPaths.add(entry.getKey()));
        }
    }

    private static final Set<String> planDefinitionPaths = new LinkedHashSet<>();
    public static Set<String> getPlanDefinitionPaths(FhirContext fhirContext) {
        if (planDefinitionPaths.isEmpty()) {
            setupPlanDefinitionPaths(fhirContext);
        }
        return planDefinitionPaths;
    }
    private static final Map<String, String> planDefinitionPathMap = new LinkedHashMap<>();
    public static Map<String, String> getPlanDefinitionPathMap(FhirContext fhirContext) {
        if (planDefinitionPathMap.isEmpty()) {
            setupPlanDefinitionPaths(fhirContext);
        }
        return planDefinitionPathMap;
    }
    private static final Map<String, IBaseResource> planDefinitions = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getPlanDefinitions(FhirContext fhirContext) {
        if (planDefinitions.isEmpty()) {
            setupPlanDefinitionPaths(fhirContext);
        }
        return planDefinitions;
    }
    private static void setupPlanDefinitionPaths(FhirContext fhirContext) {
        HashMap<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    logger.error("Error setting PlanDefinition paths while reading resource at: {}. Error: {}", path, e.getMessage());
                }
            }
            RuntimeResourceDefinition planDefinitionDefinition = ResourceUtils.getResourceDefinition(fhirContext, "PlanDefinition");
            String planDefinitionClassName = planDefinitionDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  planDefinitionClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> {
                        planDefinitionPaths.add(entry.getKey());
                        planDefinitions.put(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                        planDefinitionPathMap.put(entry.getValue().getIdElement().getIdPart(), entry.getKey());
                    });
        }
    }

    private static final Set<String> questionnairePaths = new LinkedHashSet<>();
    public static Set<String> getQuestionnairePaths(FhirContext fhirContext) {
        if (questionnairePaths.isEmpty()) {
            setupQuestionnairePaths(fhirContext);
        }
        return questionnairePaths;
    }

    private static final Map<String, String> questionnairePathMap = new LinkedHashMap<>();
    public static Map<String, String> getQuestionnairePathMap(FhirContext fhirContext) {
        if (questionnairePathMap.isEmpty()) {
            setupQuestionnairePaths(fhirContext);
        }
        return questionnairePathMap;
    }

    private static final Map<String, IBaseResource> questionnaires = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getQuestionnaires(FhirContext fhirContext) {
        if (questionnaires.isEmpty()) {
            setupQuestionnairePaths(fhirContext);
        }
        return questionnaires;
    }

    private static void setupQuestionnairePaths(FhirContext fhirContext) {
        HashMap<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    logger.error("Error setting Questionnaire paths while reading resource at: {}. Error: {}", path, e.getMessage());
                }
            }
            RuntimeResourceDefinition questionnaireDefinition = ResourceUtils.getResourceDefinition(fhirContext, "Questionnaire");
            String questionnaireClassName = questionnaireDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  questionnaireClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> {
                        questionnairePaths.add(entry.getKey());
                        questionnaires.put(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                        questionnairePathMap.put(entry.getValue().getIdElement().getIdPart(), entry.getKey());
                    });
        }
    }

    private static final Map<String, String> activityDefinitionPathMap = new LinkedHashMap<>();
    public static Map<String, String> getActivityDefinitionPathMap(FhirContext fhirContext) {
        if (activityDefinitionPathMap.isEmpty()) {
            setupQuestionnairePaths(fhirContext);
        }
        return activityDefinitionPathMap;
    }

    private static final Map<String, IBaseResource> activityDefinitions = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getActivityDefinitions(FhirContext fhirContext) {
        if (activityDefinitions.isEmpty()) {
            setupActivityDefinitionPaths(fhirContext);
        }
        return activityDefinitions;
    }

    private static final Set<String> activityDefinitionPaths = new LinkedHashSet<>();
    public static Set<String> getActivityDefinitionPaths(FhirContext fhirContext) {
        if (activityDefinitionPaths.isEmpty()) {
            logger.info("Reading activitydefinitions");
            setupActivityDefinitionPaths(fhirContext);
        }
        return activityDefinitionPaths;
    }

    private static void setupActivityDefinitionPaths(FhirContext fhirContext) {
        HashMap<String, IBaseResource> resources = new LinkedHashMap<>();
        // BUG: resourceDirectories is being populated with all "per-convention" directories during validation. So,
        // if you have resources in the /tests directory for example, they will be picked up from there, rather than
        // from your resources directories.
        for (String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
            RuntimeResourceDefinition activityDefinitionDefinition = ResourceUtils.getResourceDefinition(fhirContext, "ActivityDefinition");
            String activityDefinitionClassName = activityDefinitionDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  activityDefinitionClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> {
                        activityDefinitionPaths.add(entry.getKey());
                        activityDefinitions.put(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                        activityDefinitionPathMap.put(entry.getValue().getIdElement().getIdPart(), entry.getKey());
                    });
        }
    }

    public static void ensurePath(String path) {
        //Creating a File object
        File scopeDir = new File(path);
        //Creating the directory
        if (!scopeDir.exists() && !scopeDir.mkdirs()) {
            throw new IllegalArgumentException("Could not create directory: " + path);
        }
    }

    private static Set<String> devicePaths;
    public static Set<String> getDevicePaths(FhirContext fhirContext) {
        if (devicePaths == null) {
            setupDevicePaths(fhirContext);
        }
        return devicePaths;
    }

    // TODO: This should not be necessary this is awful... For now it is needed for passing tests in Travis
    public static void clearDevicePaths() {
        devicePaths = null;
    }

    private static void setupDevicePaths(FhirContext fhirContext) {
        devicePaths = new LinkedHashSet <>();
        Map<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    IBaseResource resource = IOUtils.readResource(path, fhirContext, true);
                    if (resource != null) {
                        resources.put(path, resource);
                    }
                } catch (Exception e) {
                    if(path.toLowerCase().contains("device")) {
                        logger.error("Error reading in Device from path: {} \n {}", path, e.getMessage());
                    }
                }
            }
            //TODO: move these to ResourceUtils
            RuntimeResourceDefinition deviceDefinition = ResourceUtils.getResourceDefinition(fhirContext, "Device");
            String deviceClassName = deviceDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  deviceClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> devicePaths.add(entry.getKey()));
        }
    }

    public static boolean isXMLOrJson(String fileDirPath, String libraryName){
        String fileExtension = libraryName.substring(libraryName.lastIndexOf(".") + 1);
        if (fileExtension.equalsIgnoreCase("xml") ||
                fileExtension.equalsIgnoreCase("json")){
            return true;
        }
        logger.warn("The file {}{} is not the right type of file.", fileDirPath, libraryName);
        return false;
    }

    // Assumes the tests are structured as .../input/tests/measure/{MeasureName}/{TestName} and will extract
    // the measure name
    public static String getMeasureTestDirectory(String pathString) {
        Path path = Paths.get(pathString);
        String[] testDirs = StreamSupport.stream(path.spliterator(), false).map(Path::toString).toArray(String[]::new);
        return testDirs[testDirs.length - 2];
    }

    public static String concatFilePath(String basePath, String... pathsToAppend) {
        String filePath = basePath;
        for (String pathToAppend: pathsToAppend) {
            filePath = FilenameUtils.concat(filePath, pathToAppend);
        }
        return filePath;
    }


    /**
     * Cleans up cached data to ensure a clean state for subsequent ci tests.
     * Since all variables are final, we use .clear(). This gives a slight performance
     * boost over removing final keyword and initializing new instances.
     */
    public static void cleanUp(){
        alreadyCopied.clear();
        cachedResources.clear();
        cachedFilePaths.clear();
        cachedDirectoryPaths.clear();
        cachedTranslator.clear();
        cqlLibraryPaths.clear();
        terminologyPaths.clear();
        libraryPaths.clear();
        libraryUrlMap.clear();
        libraryPathMap.clear();
        libraries.clear();
        measurePaths.clear();
        measurePathMap.clear();
        measures.clear();
        measureReportPaths.clear();
        planDefinitionPaths.clear();
        planDefinitionPathMap.clear();
        planDefinitions.clear();
        questionnairePaths.clear();
        questionnairePathMap.clear();
        questionnaires.clear();
        activityDefinitionPaths.clear();
    }
}