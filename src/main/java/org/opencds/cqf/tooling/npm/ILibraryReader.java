package org.opencds.cqf.tooling.npm;

import java.io.IOException;
import java.io.InputStream;

import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r5.model.Library;
/**
 * @author Adam Stevenson
 */
public interface ILibraryReader {
    public Library readLibrary(InputStream stream) throws FHIRFormatError, IOException;
}
