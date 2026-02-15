package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.util.BundleBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static Encoding getEncoding(String path) {
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

    public static void ensurePath(String path) {
        File scopeDir = new File(path);
        if (!scopeDir.exists() && !scopeDir.mkdirs()) {
            throw new IllegalArgumentException("Could not create directory: " + path);
        }
    }

    public static boolean isXMLOrJson(String fileDirPath, String libraryName) {
        String fileExtension = libraryName.substring(libraryName.lastIndexOf(".") + 1);
        if (fileExtension.equalsIgnoreCase("xml") ||
                fileExtension.equalsIgnoreCase("json")) {
            return true;
        }
        logger.warn("The file {}{} is not the right type of file.", fileDirPath, libraryName);
        return false;
    }

    public static String getMeasureTestDirectory(String pathString) {
        Path path = Paths.get(pathString);
        String[] testDirs = java.util.stream.StreamSupport.stream(path.spliterator(), false).map(Path::toString).toArray(String[]::new);
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
     * Cleans up cached data in the core IOUtils caches.
     * For a full cleanup including resource discovery caches, use ResourceDiscovery.cleanUp().
     */
    public static void cleanUp() {
        alreadyCopied.clear();
        cachedResources.clear();
        cachedFilePaths.clear();
        cachedDirectoryPaths.clear();
    }
}
