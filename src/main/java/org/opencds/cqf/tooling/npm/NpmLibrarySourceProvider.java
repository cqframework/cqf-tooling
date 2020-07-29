package org.opencds.cqf.tooling.npm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.utilities.cache.NpmPackage;

/**
 * Provides a library source provider that can resolve CQL library source from an Npm package
 */
public class NpmLibrarySourceProvider implements LibrarySourceProvider {

    public NpmLibrarySourceProvider(List<NpmPackage> packages, ILibraryReader reader, IWorkerContext.ILoggingService logger) {
        this.packages = packages;
        this.reader = reader;
        this.logger = logger;
    }

    private List<NpmPackage> packages;
    private ILibraryReader reader;
    private IWorkerContext.ILoggingService logger;

    @Override
    public InputStream getLibrarySource(VersionedIdentifier identifier) {
        // VersionedIdentifier.id: Name of the library
        // VersionedIdentifier.system: Namespace for the library, as a URL
        // VersionedIdentifier.version: Version of the library
        for (NpmPackage p : packages) {
            try {
                InputStream s = p.loadByCanonicalVersion(identifier.getSystem()+"/Library/"+identifier.getId(), identifier.getVersion());
                if (s != null) {
                    Library l = reader.readLibrary(s);
                    for (org.hl7.fhir.r5.model.Attachment a : l.getContent()) {
                        if (a.getContentType() != null && a.getContentType().equals("text/cql")) {
                            return new ByteArrayInputStream(a.getData());
                        }
                    }
                }
            } catch (IOException e) {
                logger.logDebugMessage(IWorkerContext.ILoggingService.LogCategory.PROGRESS, String.format("Exceptions occurred attempting to load npm library source for %s", identifier.toString()));
            }
        }

        return null;
    }
}

