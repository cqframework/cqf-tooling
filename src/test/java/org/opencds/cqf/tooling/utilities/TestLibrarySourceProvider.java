package org.opencds.cqf.tooling.utilities;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

import java.io.InputStream;

public class TestLibrarySourceProvider implements LibrarySourceProvider {

    private String relativePath;

    public TestLibrarySourceProvider(String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
        String libraryFileName = String.format("%s%s%s.cql",
                relativePath == null ? "" : (relativePath + "/"),
                libraryIdentifier.getId(),
                libraryIdentifier.getVersion() != null ? ("-" + libraryIdentifier.getVersion()) : "");
        return TestLibrarySourceProvider.class.getResourceAsStream(libraryFileName);
    }
}
