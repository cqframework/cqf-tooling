package org.opencds.cqf.processor;

import org.opencds.cqf.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public interface LibraryProcessor {
    public static final String ResourcePrefix = "library-";   
    public static String getId(String baseId) {
        return ResourcePrefix + baseId;
    }

    public Boolean refreshLibraryContent(String cqlContentPath, String libraryPath, FhirContext fhirContext, Encoding encoding, Boolean includeVersion);
}