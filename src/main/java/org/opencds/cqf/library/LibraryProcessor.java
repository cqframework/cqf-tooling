package org.opencds.cqf.library;

public class LibraryProcessor {
    public static final String ResourcePrefix = "library-";   
    public static String getId(String baseId) {
        return ResourcePrefix + baseId;
    }
}