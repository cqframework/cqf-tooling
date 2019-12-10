package org.opencds.cqf.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.hl7.fhir.instance.model.api.IAnyResource;

import ca.uhn.fhir.context.FhirContext;
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

    public static byte[] parseResource(IAnyResource resource, Encoding encoding, FhirContext fhirContext) 
    {
        if (encoding == Encoding.UNKNOWN) {
            return new byte[] { };
        }
        IParser parser = getParser(encoding, fhirContext);      
        return parser.setPrettyPrint(true).encodeResourceToString(resource).getBytes();
    }

    public static <T extends IAnyResource> void writeResource(T resource, String path, Encoding encoding, FhirContext fhirContext) 
    {        
        try (FileOutputStream writer = new FileOutputStream(FilenameUtils.concat(path, resource.getId() + "." + encoding.toString())))
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

    public static IAnyResource readResource(String path, FhirContext fhirContext) {
        return readResource(path, fhirContext, false);
    }
    
    //users should always check for null
    public static IAnyResource readResource(String path, FhirContext fhirContext, Boolean safeRead) 
    {        
        Encoding encoding = getEncoding(path);
        if (encoding == Encoding.UNKNOWN) {
            return null;
        }

        IAnyResource resource = null;      
        try
        {
            IParser parser = getParser(encoding, fhirContext);
            File file = new File(path);
            if (safeRead) {
                if (!file.exists()) {
                    return null;
                }
            }
            resource = (IAnyResource)parser.parseResource(new FileReader(file));
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
        ArrayList<File> files = new ArrayList<>(Arrays.asList(Optional.ofNullable(inputDir.listFiles()).orElseThrow()));
       
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

    public static String getParentDirectoryPath(String path) {
        File file = new File(path);
        return file.getParent().toString();
    }

    public static List<String> getDirectoryPaths(String path, Boolean recursive)
    {
        List<String> directoryPaths = new ArrayList<String>();
        File parentDirectory = new File(path);
        ArrayList<File> directories = new ArrayList<>(Arrays.asList(Optional.ofNullable(parentDirectory.listFiles()).orElseThrow()));
       
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

    //users should protect against Encoding.UNKNOWN
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

    public static Boolean pathIncludesElement(String igPath, String pathElement)
    {
        Boolean result = false;
        try
        {
            result = FilenameUtils.getName(igPath).equals(pathElement);
        }
        catch (Exception e) {}
        return result;
    }

    public static List<String> getDependencyCqlPaths(String cqlContentPath) {
        ArrayList<File> DependencyFiles = getDependencyCqlFiles(cqlContentPath);
        ArrayList<String> DependencyPaths = new ArrayList<String>();
        for (File file : DependencyFiles) {
            DependencyPaths.add(file.getPath().toString());
        }
        return DependencyPaths;
    }

    public static ArrayList<File> getDependencyCqlFiles(String cqlContentPath) {
        File cqlContent = new File(cqlContentPath);
        File cqlContentDir = cqlContent.getParentFile();
        if (!cqlContentDir.isDirectory()) {
            throw new IllegalArgumentException("The specified path to library files is not a directory");
        }
        ArrayList<String> dependencyLibraries = ResourceUtils.getIncludedLibraryNames(cqlContentPath);
        File[] allCqlContentFiles = cqlContentDir.listFiles();
        if (allCqlContentFiles.length == 1) {
            return new ArrayList<File>();
        }
        ArrayList<File> dependencyCqlFiles = new ArrayList<>();
        dependencyCqlFiles.add(cqlContent);
        for (File cqlFile : allCqlContentFiles) {
            if (dependencyLibraries.contains(cqlFile.getName().replace(".cql", ""))) {
                dependencyCqlFiles.add(cqlFile);
            }  
        }
        return dependencyCqlFiles;
    }

    public static List<String> getDependencyValueSetPaths(String cqlContentPath, String valueSetDirPath)
            throws FileNotFoundException {
        ArrayList<File> DependencyFiles = getDependencyValueSetFiles(cqlContentPath, valueSetDirPath);
        ArrayList<String> DependencyPaths = new ArrayList<String>();
        for (File file : DependencyFiles) {
            DependencyPaths.add(file.getPath().toString());
        }
        return DependencyPaths;
    }

    public static ArrayList<File> getDependencyValueSetFiles(String cqlContentPath, String valueSetDirPath)
            throws FileNotFoundException {
        File valueSetDir = new File(valueSetDirPath);
        if (!valueSetDir.isDirectory()) {
            throw new IllegalArgumentException("The specified path to valueset files is not a directory");
        }
        ArrayList<String> dependencyValueSets = ResourceUtils.getDependencyValueSetNames(cqlContentPath);
        File[] allValueSetFiles = valueSetDir.listFiles();
        if (allValueSetFiles.length == 0) {
            throw new FileNotFoundException("did not find any files in valueset directory");
        }
        ArrayList<File> dependencyValueSetFiles = new ArrayList<>();
        for (File cqlFile : allValueSetFiles) {
            if (dependencyValueSets.contains(cqlFile.getName().replace(".json", ""))) {
                dependencyValueSetFiles.add(cqlFile);
            }
        }
        return dependencyValueSetFiles;
    }

    public static CqlTranslator translate(String cqlContentPath, ModelManager modelManager, LibraryManager libraryManager) {
        try {
          File cqlFile = new File(cqlContentPath);
          if(!cqlFile.getName().endsWith(".cql")) {
            throw new IllegalArgumentException("cqlContentPath must be a path to a .cql file");
          }
            ArrayList<CqlTranslator.Options> options = new ArrayList<>();
            options.add(CqlTranslator.Options.EnableDateRangeOptimization);
  
            CqlTranslator translator =
                    CqlTranslator.fromFile(
                            cqlFile,
                            modelManager,
                            libraryManager,
                            options.toArray(new CqlTranslator.Options[0])
                    );
  
            if (translator.getErrors().size() > 0) {
                System.err.println("Translation failed due to errors:");
                ArrayList<String> errors = new ArrayList<>();
                for (CqlTranslatorException error : translator.getErrors()) {
                    TrackBack tb = error.getLocator();
                    String lines = tb == null ? "[n/a]" : String.format("[%d:%d, %d:%d]",
                            tb.getStartLine(), tb.getStartChar(), tb.getEndLine(), tb.getEndChar());
                    System.err.printf("%s %s%n", lines, error.getMessage());
                    errors.add(lines + error.getMessage());
                }
                throw new IllegalArgumentException(errors.toString());
            }
  
            return translator;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error encountered during CQL translation: " + e.getMessage());
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

    public static String getFileName(String baseName, Encoding encoding) {
        return baseName + getFileExtension(encoding);
    }
}
