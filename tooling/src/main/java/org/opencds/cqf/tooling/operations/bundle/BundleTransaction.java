package org.opencds.cqf.tooling.operations.bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.LogUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Operation(name = "BundleTransaction")
public class BundleTransaction implements ExecutableOperation {
    @OperationParam(alias = { "ptb", "pathtobundles" }, setter = "setPathToBundles", required = true)
    private String pathToBundles;
    @OperationParam(alias = { "fs", "fhirServer" }, setter = "setFhirServer", required = true)
    private String fhirServer;
    @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4")
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
                    LogUtils.info(IOUtils.encodeResourceAsString(response.get(), IOUtils.Encoding.JSON, fhirContext));
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
