package org.opencds.cqf.library;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class GenericLibrarySourceProvider implements LibrarySourceProvider {

    private String pathToSource;

    public GenericLibrarySourceProvider(String pathToSource) {
        this.pathToSource = pathToSource;
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier versionedIdentifier) {
        File sourceDir = new File(pathToSource);
        for (File file : sourceDir.listFiles()) {
            if (file.getName().contains(versionedIdentifier.getId()) || file.getName().startsWith(versionedIdentifier.getId())) {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Error reading " + file.getName());
                }
            }
        }
        throw new IllegalArgumentException("Unable to resolve source for library: " + versionedIdentifier.getId());
    }
}
