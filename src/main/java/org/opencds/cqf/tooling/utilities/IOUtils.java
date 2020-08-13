package org.opencds.cqf.tooling.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.tooling.processor.LibraryProcessor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeCompositeDatatypeDefinition;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.parser.IParser;

public class IOUtils 
{        
    public enum Encoding 
    { 
        CQL("cql"), JSON("json"), XML("xml"), UNKNOWN(""); 
  
        private String string; 
    
        public String toString() 
        { 
            return this.string; 
        } 
    
        private Encoding(String string) 
        { 
            this.string = string; 
        }

        public static Encoding parse(String value) {
            switch (value) {
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

    public static ArrayList<String> resourceDirectories = new ArrayList<String>();

    public static String getIdFromFileName(String fileName) {
        return fileName.replaceAll("_", "-");
    }

    public static byte[] parseResource(IAnyResource resource, Encoding encoding, FhirContext fhirContext) 
    {
        if (encoding == Encoding.UNKNOWN) {
            return new byte[] { };
        }
        IParser parser = getParser(encoding, fhirContext);    
        return parser.setPrettyPrint(true).encodeResourceToString(resource).getBytes();
    }

    public static String parseResourceAsString(IAnyResource resource, Encoding encoding, FhirContext fhirContext) 
    {
        if (encoding == Encoding.UNKNOWN) {
            return "";
        }
        IParser parser = getParser(encoding, fhirContext);  
        return parser.setPrettyPrint(true).encodeResourceToString(resource).toString();
    }

    public static <T extends IAnyResource> void writeResource(T resource, String path, Encoding encoding, FhirContext fhirContext) 
    {
        // If the path is to a specific resource file, just re-use that file path/name.
        String outputPath = null;
        File file = new File(path);
        if (file.isFile()) {
            outputPath = path;
        }
        else {
            outputPath = FilenameUtils.concat(path, formatFileName(resource.getIdElement().getIdPart(), encoding, fhirContext));
        }

        try (FileOutputStream writer = new FileOutputStream(outputPath))
        {
            writer.write(parseResource(resource, encoding, fhirContext));
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error writing Resource to file: " + e.getMessage());
        }
    }

    public static <T extends IAnyResource> void writeResources(Map<String, T> resources, String path, Encoding encoding, FhirContext fhirContext)
    {        
        for (Map.Entry<String, T> set : resources.entrySet())
        {
            writeResource(set.getValue(), path, encoding, fhirContext);
        }
    }

    //There's a special operation to write a bundle because I can't find a type that will reference both dstu3 and r4.
    public static void writeBundle(Object bundle, String path, Encoding encoding, FhirContext fhirContext) {
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                writeResource(((org.hl7.fhir.dstu3.model.Bundle)bundle), path, encoding, fhirContext);
                break;
            case R4:
                writeResource(((org.hl7.fhir.r4.model.Bundle)bundle), path, encoding, fhirContext);
                break;
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    public static void copyFile(String inputPath, String outputPath) {
        try  {
            Path src = Paths.get(inputPath);
            Path dest = Paths.get(outputPath);
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error copying file: " + e.getMessage());
        }
    }

    public static String getTypeQualifiedResourceId(String path, FhirContext fhirContext) {
        IAnyResource resource = readResource(path, fhirContext, true);
        if (resource != null) {
            return resource.getIdElement().getResourceType() + "/" + resource.getIdElement().getIdPart();
        }

        return null;
    }

    public static String getCanonicalResourceVersion(IAnyResource resource, FhirContext fhirContext) {
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

    public static String getCanonicalResourceVersion(String path, FhirContext fhirContext) {
        IAnyResource resource = readResource(path, fhirContext, true);
        return getCanonicalResourceVersion(path, fhirContext);
    }

    public static IAnyResource readResource(String path, FhirContext fhirContext) {
        return readResource(path, fhirContext, false);
    }
    
    //users should always check for null
    private static Map<String, IAnyResource> cachedResources = new HashMap<String, IAnyResource>();
    public static IAnyResource readResource(String path, FhirContext fhirContext, Boolean safeRead) 
    {        
        Encoding encoding = getEncoding(path);
        if (encoding == Encoding.UNKNOWN || encoding == Encoding.CQL) {
            return null;
        }

        IAnyResource resource = cachedResources.get(path);     
        if (resource != null) {
            return resource;
        } 

        try
        {
            IParser parser = getParser(encoding, fhirContext);
            File file = new File(path);

            if (!file.exists()) {
                String[] paths = file.getParent().split("\\\\");
                file = new File(Paths.get(file.getParent(), paths[paths.length - 1] + "-" + file.getName()).toString());
            }

            if (safeRead) {
                if (!file.exists()) {
                    return null;
                }
            }
            resource = (IAnyResource)parser.parseResource(new FileReader(file));
            cachedResources.put(path, resource);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
        return resource;
    }

    public static List<IAnyResource> readResources(List<String> paths, FhirContext fhirContext) 
    {
        List<IAnyResource> resources = new ArrayList<>();
        for (String path : paths)
        {
            IAnyResource resource = readResource(path, fhirContext);
            if (resource != null) {
                resources.add(resource);
            }
        }
        return resources;
    }

    public static List<String> getFilePaths(String directoryPath, Boolean recursive)
    {
        List<String> filePaths = new ArrayList<String>();
        File inputDir = new File(directoryPath);
        ArrayList<File> files = inputDir.isDirectory() ? new ArrayList<File>(Arrays.asList(Optional.ofNullable(inputDir.listFiles()).<NoSuchElementException>orElseThrow(() -> new NoSuchElementException()))) : new ArrayList<File>();
       
        for (File file : files) {
            if (file.isDirectory()) {
                //note: this is not the same as anding recursive to isDirectory as that would result in directories being added to the list if the request is not recursive.
                if (recursive) {
                    filePaths.addAll(getFilePaths(file.getPath(), recursive));
                }
            }
            else {
               filePaths.add(file.getPath());
            }
        }
        return filePaths;
    }

    public static String getResourceFileName(String resourcePath, IAnyResource resource, Encoding encoding, FhirContext fhirContext, boolean versioned) {
        String resourceVersion = IOUtils.getCanonicalResourceVersion(resource, fhirContext);
        String result = Paths.get(resourcePath, resource.getIdElement().getResourceType(),
                resource.getIdElement().getIdPart() + ((versioned && resourceVersion != null && !(resource.getIdElement().getIdPart().endsWith(resourceVersion))) ? ("-" + resourceVersion) : ""))
                + getFileExtension(encoding);
        return result;
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

    public static List<String> getDirectoryPaths(String path, Boolean recursive)
    {
        List<String> directoryPaths = new ArrayList<String>();
        List<File> directories = new ArrayList<File>();
        File parentDirectory = new File(path);
        try {
            directories = Arrays.asList(Optional.ofNullable(parentDirectory.listFiles()).<NoSuchElementException>orElseThrow(() -> new NoSuchElementException()));
        } catch (Exception e) {
            System.out.println("No paths found for the Directory " + path + ":");
            return directoryPaths;
        }
        
       
        for (File directory : directories) {
            if (directory.isDirectory()) {
                if (recursive) {
                    directoryPaths.addAll(getDirectoryPaths(directory.getPath(), recursive));
                }
                directoryPaths.add(directory.getPath());
            }
        }
        return directoryPaths;
    }

    public static void initializeDirectory(String path) {
        File directory = new File(path);
        if (directory.exists()) {
            try {
                deleteDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error deleting directory: " + path + " - " + e.getMessage());
            }
        }
        directory.mkdir();
    }

    public static void deleteDirectory(String path) throws IOException {
        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
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

    //users should protect against Encoding.UNKNOWN or Enconding.CQL
    private static IParser getParser(Encoding encoding, FhirContext fhirContext) 
    {
        switch (encoding) {
            case XML: 
                return fhirContext.newXmlParser();
            case JSON:
                return fhirContext.newJsonParser();
            default: 
                throw new RuntimeException("Unknown encoding type: " + encoding.toString());
        }
    }

    public static Boolean pathEndsWithElement(String igPath, String pathElement)
    {
        Boolean result = false;
        try
        {
            String baseElement = FilenameUtils.getBaseName(igPath).equals("") ? FilenameUtils.getBaseName(FilenameUtils.getFullPathNoEndSeparator(igPath)) : FilenameUtils.getBaseName(igPath);
            result = baseElement.equals(pathElement);
        }
        catch (Exception e) {}
        return result;
    }

    public static List<String> getDependencyCqlPaths(String cqlContentPath, Boolean includeVersion) throws Exception {
        ArrayList<File> DependencyFiles = getDependencyCqlFiles(cqlContentPath, includeVersion);
        ArrayList<String> DependencyPaths = new ArrayList<String>();
        for (File file : DependencyFiles) {
            DependencyPaths.add(file.getPath().toString());
        }
        return DependencyPaths;
    }

    public static ArrayList<File> getDependencyCqlFiles(String cqlContentPath, Boolean includeVersion) throws Exception {
        File cqlContent = new File(cqlContentPath);
        File cqlContentDir = cqlContent.getParentFile();
        if (!cqlContentDir.isDirectory()) {
            throw new IllegalArgumentException("The specified path to library files is not a directory");
        }
        ArrayList<String> dependencyLibraries = ResourceUtils.getIncludedLibraryNames(cqlContentPath, includeVersion);
        File[] allCqlContentFiles = cqlContentDir.listFiles();
        if (allCqlContentFiles.length == 1) {
            return new ArrayList<File>();
        }
        ArrayList<File> dependencyCqlFiles = new ArrayList<>();
        for (File cqlFile : allCqlContentFiles) {
            if (dependencyLibraries.contains(getIdFromFileName(cqlFile.getName().replace(".cql", "")))) {
                dependencyCqlFiles.add(cqlFile);
                dependencyLibraries.remove(getIdFromFileName(cqlFile.getName().replace(".cql", "")));
            }  
        }

        if (dependencyLibraries.size() != 0) {
            String message = (dependencyLibraries.size()) + " included cql Libraries not found: ";
            
            for (String includedLibrary : dependencyLibraries) {
              message += "\r\n" + includedLibrary + " MISSING";
            }        
            throw new Exception(message);
          }
        return dependencyCqlFiles;
    } 
  
    private static Map<String, CqlTranslator> cachedTranslator = new HashMap<String, CqlTranslator>();
    public static CqlTranslator translate(String cqlContentPath, ModelManager modelManager, LibraryManager libraryManager) {
        CqlTranslator translator = cachedTranslator.get(cqlContentPath);
        if (translator != null) {
            return translator;
        }
        try {
          File cqlFile = new File(cqlContentPath);
          if(!cqlFile.getName().endsWith(".cql")) {
            throw new IllegalArgumentException("cqlContentPath must be a path to a .cql file");
          }
          
            ArrayList<CqlTranslator.Options> options = new ArrayList<>();
            options.add(CqlTranslator.Options.EnableDateRangeOptimization);
  
            translator =
                    CqlTranslator.fromFile(
                            cqlFile,
                            modelManager,
                            libraryManager,
                            options.toArray(new CqlTranslator.Options[0])
                    );
  
            if (translator.getErrors().size() > 0) {
                //System.err.println("Translation failed due to errors:");
                ArrayList<String> errors = new ArrayList<>();
                for (CqlTranslatorException error : translator.getErrors()) {
                    TrackBack tb = error.getLocator();
                    String lines = tb == null ? "[n/a]" : String.format("[%d:%d, %d:%d]",
                            tb.getStartLine(), tb.getStartChar(), tb.getEndLine(), tb.getEndChar());
                    //System.err.printf("%s %s%n", lines, error.getMessage());
                    errors.add(lines + error.getMessage());
                }
                throw new IllegalArgumentException(errors.toString());
            }
            cachedTranslator.put(cqlContentPath, translator);
            return translator;
        } catch (IOException e) {
            //e.printStackTrace();
            //throw new IllegalArgumentException("Error encountered during CQL translation: " + e.getMessage());
            throw new IllegalArgumentException("Error encountered during CQL translation");
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
            e.printStackTrace();
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
        result = result.replace("-" + igVersionToken, "_" + igVersionToken);
        return result;
    }    

    public static List<String> putAllInListIfAbsent(List<String> values, List<String> list)
    {
        for (String value : values) {
            if (!list.contains(value)) {
                list.add(value);
            }
        }
        return list;
    }

    public static List<String> putInListIfAbsent(String value, List<String> list)
    {
        if (!list.contains(value)) {
            list.add(value);
        }
        return list;
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

    private static HashSet<String> cqlLibraryPaths = new HashSet<String>();
    public static HashSet<String> getCqlLibraryPaths() {
        if (cqlLibraryPaths.isEmpty()) {
            setupCqlLibraryPaths();
        }
        return cqlLibraryPaths;
    }
    private static void setupCqlLibraryPaths() {  
        //need to add a error report for bad resource paths
        for(String dir : resourceDirectories) {
            List<String> filePaths = IOUtils.getFilePaths(dir, true);
            filePaths.stream().filter(path -> path.contains(".cql")).forEach(path -> cqlLibraryPaths.add(path));
        }
    }

    private static HashSet<String> terminologyPaths = new HashSet<String>();
    public static HashSet<String> getTerminologyPaths(FhirContext fhirContext) {
        if (terminologyPaths.isEmpty()) {
            setupTerminologyPaths(fhirContext);
        }
        return terminologyPaths;
    }
    private static void setupTerminologyPaths(FhirContext fhirContext) {
        HashMap<String, IAnyResource> resources = new HashMap<String, IAnyResource>();
        for(String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true))
            {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    if (path.toLowerCase().contains("valuesets") || path.toLowerCase().contains("valueset")) {
                        System.out.println("Error reading in Terminology from path: " + path + "\n" + e);
                    }
                }
            }
            //TODO: move these to ResourceUtils
            RuntimeResourceDefinition valuesetDefinition = (RuntimeResourceDefinition)ResourceUtils.getResourceDefinition(fhirContext, "ValueSet");
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

    private static HashSet<String> libraryPaths = new HashSet<String>();
    public static HashSet<String> getLibraryPaths(FhirContext fhirContext) {
        if (libraryPaths.isEmpty()) {
            setupLibraryPaths(fhirContext);
        }
        return libraryPaths;
    }
    private static void setupLibraryPaths(FhirContext fhirContext) {
        HashMap<String, IAnyResource> resources = new HashMap<String, IAnyResource>();
        for(String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true))
            {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    if(path.toLowerCase().contains("library")) {
                        System.out.println("Error reading in Library from path: " + path + "\n" + e);
                    }
                }
            }
            //TODO: move these to ResourceUtils
            RuntimeResourceDefinition libraryDefinition = (RuntimeResourceDefinition)ResourceUtils.getResourceDefinition(fhirContext, "Library");
            String libraryClassName = libraryDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry ->  libraryClassName.equals(entry.getValue().getClass().getName()))
                .forEach(entry -> libraryPaths.add(entry.getKey()));
        }
    }

    private static HashSet<String> measurePaths = new HashSet<String>();
    public static HashSet<String> getMeasurePaths(FhirContext fhirContext) {
        if (measurePaths.isEmpty()) {
            setupMeasurePaths(fhirContext);
        }
        return measurePaths;
    }
    private static void setupMeasurePaths(FhirContext fhirContext) {
        HashMap<String, IAnyResource> resources = new HashMap<String, IAnyResource>();
        for(String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true))
            {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    if(path.toLowerCase().contains("measure")) {
                        System.out.println("Error reading in Measure from path: " + path + "\n" + e);
                    }
                }
            }
            //TODO: move these to ResourceUtils
            RuntimeResourceDefinition measureDefinition = (RuntimeResourceDefinition)ResourceUtils.getResourceDefinition(fhirContext, "Measure");
            String measureClassName = measureDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry ->  measureClassName.equals(entry.getValue().getClass().getName()))
                .forEach(entry -> measurePaths.add(entry.getKey()));
        }
    }

    private static HashSet<String> measureReportPaths = new HashSet<String>();
    public static HashSet<String> getMeasureReportPaths(FhirContext fhirContext) {
        if (measureReportPaths.isEmpty()) {
            setupMeasureReportPaths(fhirContext);
        }
        return measureReportPaths;
    }
    private static void setupMeasureReportPaths(FhirContext fhirContext) {
        HashMap<String, IAnyResource> resources = new HashMap<String, IAnyResource>();
        for(String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true))
            {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
            //TODO: move these to ResourceUtils
            RuntimeResourceDefinition measureReportDefinition = (RuntimeResourceDefinition)ResourceUtils.getResourceDefinition(fhirContext, "MeasureReport");
            String measureReportClassName = measureReportDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry ->  measureReportClassName.equals(entry.getValue().getClass().getName()))
                .forEach(entry -> measureReportPaths.add(entry.getKey()));
        }
    }

	private static HashSet<String> planDefinitionPaths = new HashSet<String>();
    public static HashSet<String> getPlanDefinitionPaths(FhirContext fhirContext) {
        if (planDefinitionPaths.isEmpty()) {
            setupPlanDefinitionPaths(fhirContext);
        }
        return planDefinitionPaths;
    }
    private static void setupPlanDefinitionPaths(FhirContext fhirContext) {
        HashMap<String, IAnyResource> resources = new HashMap<String, IAnyResource>();
        for(String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true))
            {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
            RuntimeResourceDefinition planDefinitionDefinition = (RuntimeResourceDefinition)ResourceUtils.getResourceDefinition(fhirContext, "PlanDefinition");
            String planDefinitionClassName = planDefinitionDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry ->  planDefinitionClassName.equals(entry.getValue().getClass().getName()))
                .forEach(entry -> planDefinitionPaths.add(entry.getKey()));
        }
    }

    private static HashSet<String> activityDefinitionPaths = new HashSet<String>();
    public static HashSet<String> getActivityDefinitionPaths(FhirContext fhirContext) {
        if (activityDefinitionPaths.isEmpty()) {
            System.out.println("Reading activitydefinitions");
            setupActivityDefinitionPaths(fhirContext);
        }
        return activityDefinitionPaths;
    }
    private static void setupActivityDefinitionPaths(FhirContext fhirContext) {
        HashMap<String, IAnyResource> resources = new HashMap<String, IAnyResource>();
        // BUG: resourceDirectories is being populated with all "per-convention" directories during validation. So,
        // if you have resources in the /tests directory for example, they will be picked up from there, rather than
        // from your resources directories.
        for(String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true))
            {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
            RuntimeResourceDefinition activityDefinitionDefinition = (RuntimeResourceDefinition)ResourceUtils.getResourceDefinition(fhirContext, "ActivityDefinition");
            String activityDefinitionClassName = activityDefinitionDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry ->  activityDefinitionClassName.equals(entry.getValue().getClass().getName()))
                .forEach(entry -> activityDefinitionPaths.add(entry.getKey()));
        }
    }

    private static HashSet<String> devicePaths = new HashSet<String>();
    public static HashSet<String> getDevicePaths(FhirContext fhirContext) {
        if (devicePaths.isEmpty()) {
            setupDevicePaths(fhirContext);
        }
        return devicePaths;
    }
    private static void setupDevicePaths(FhirContext fhirContext) {
        HashMap<String, IAnyResource> resources = new HashMap<String, IAnyResource>();
        for(String dir : resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true))
            {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    if(path.toLowerCase().contains("device")) {
                        System.out.println("Error reading in Device from path: " + path + "\n" + e);
                    }
                }
            }
            //TODO: move these to ResourceUtils
            RuntimeResourceDefinition deviceDefinition = (RuntimeResourceDefinition)ResourceUtils.getResourceDefinition(fhirContext, "Device");
            String deviceClassName = deviceDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  deviceClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> devicePaths.add(entry.getKey()));
        }
    }
}
