package org.opencds.cqf.tooling.operations.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import jakarta.annotation.Nonnull;

@Operation(name = "BundleTransaction")
public class BundleTransaction implements ExecutableOperation {
    private static final Logger logger = LoggerFactory.getLogger(BundleTransaction.class);
    @OperationParam(alias = { "ptb", "pathtobundles" }, setter = "setPathToBundles", required = true,
            description = "Path to the bundles to load into the FHIR server (required)")
    private String pathToBundles;
    @OperationParam(alias = { "fs", "fhirServer" }, setter = "setFhirServer", required = true,
            description = "The FHIR server where the $transaction operation is executed (required)")
    private String fhirServer;
    @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
            description = "FHIR version { stu3, r4, r5 } (default r4)")
    private String version;

    @Override
    public void execute() {
        FhirContext context = FhirContextCache.getContext(version);
        List<IBaseBundle> bundles = IOUtils.readResources(
                IOUtils.getFilePaths(pathToBundles, true), context)
                .stream().filter(IBaseBundle.class::isInstance)
                .map(IBaseBundle.class::cast).collect(Collectors.toList());
        bundleTransaction(bundles, context, fhirServer);
    }

    public static List<IBaseBundle> bundleTransaction(@Nonnull List<IBaseBundle> bundles,
                                                      @Nonnull FhirContext fhirContext,
                                                      @Nonnull String fhirServerUri) {
        IGenericClient client = fhirContext.newRestfulGenericClient(fhirServerUri);
        AtomicReference<IBaseBundle> response = new AtomicReference<>();
        List<IBaseBundle> responseBundles = new ArrayList<>();
        bundles.forEach(
                bundle -> {
                    response.set(client.transaction().withBundle(bundle).execute());
                    responseBundles.add(response.get());
                    logger.info(IOUtils.encodeResourceAsString(response.get(), IOUtils.Encoding.JSON, fhirContext));
                }
        );
        return responseBundles;
    }

    public String getPathToBundles() {
        return pathToBundles;
    }

    public void setPathToBundles(String pathToBundles) {
        this.pathToBundles = pathToBundles;
    }

    public String getFhirServer() {
        return fhirServer;
    }

    public void setFhirServer(String fhirServer) {
        this.fhirServer = fhirServer;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
