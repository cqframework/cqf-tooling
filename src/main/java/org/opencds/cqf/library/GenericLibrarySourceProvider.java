package org.opencds.cqf.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

public class GenericLibrarySourceProvider implements LibrarySourceProvider {

    private String pathToSource;
    private LibraryHashMap libraries = new LibraryHashMap();

    public GenericLibrarySourceProvider(String pathToSource) {
        this.pathToSource = pathToSource;
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier versionedIdentifier) {
        if (libraries.containsKey(versionedIdentifier)) {
            return libraries.get(versionedIdentifier);
        }

        File sourceDir = new File(pathToSource);
        for (File file : sourceDir.listFiles()) {
            if (!file.getName().endsWith(".cql")) continue;
            try (Scanner scanner = new Scanner(new FileInputStream(file))) {
                String cql = scanner.useDelimiter("\\A").next();
                VersionedIdentifier vi = new VersionedIdentifier();
                vi.setId(getIdFromSource(cql));
                vi.setVersion(getVersionFromSource(cql));
                this.libraries.put(vi, new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Error reading " + file.getName());
            }
        }

        if (libraries.containsKey(versionedIdentifier)) {
            return libraries.get(versionedIdentifier);
        }

        throw new IllegalArgumentException("Unable to resolve source for library: " + versionedIdentifier.getId());
    }

    private String getIdFromSource(String cql) {
        if (cql.startsWith("library")) {
            return getNameFromSource(cql);
        }

        throw new RuntimeException("This tool requires cql libraries to include a named/versioned identifier");
    }

    private String getNameFromSource(String cql) {
        return cql.replaceFirst("library ", "").split(" version")[0].replaceAll("\"", "");
    }
    private String getVersionFromSource(String cql) {
        return cql.split("version")[1].split("'")[1];
    }
}
