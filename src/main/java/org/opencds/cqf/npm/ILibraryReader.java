package org.opencds.cqf.npm;

import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r5.model.Library;

import java.io.IOException;
import java.io.InputStream;

public interface ILibraryReader {
    public Library readLibrary(InputStream stream) throws FHIRFormatError, IOException;
}
