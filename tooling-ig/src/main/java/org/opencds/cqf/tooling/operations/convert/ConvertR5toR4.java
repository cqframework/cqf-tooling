package org.opencds.cqf.tooling.operations.convert;

import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.util.BundleBuilder;
import jakarta.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Operation(name = "ConvertR5toR4")
public class ConvertR5toR4 implements ExecutableOperation {

    public static final List<BundleTypeEnum> ALLOWED_BUNDLE_TYPES =
            List.of(BundleTypeEnum.COLLECTION, BundleTypeEnum.TRANSACTION);

    public static Boolean isBundleTypeAllowed(String bundleType) {
        if (bundleType == null) {
            return false;
        }
        return ALLOWED_BUNDLE_TYPES.stream().anyMatch(bt -> bt.name().equalsIgnoreCase(bundleType));
    }

    public static List<String> allowedBundleTypes() {
        return ALLOWED_BUNDLE_TYPES.stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private static final Logger logger = LoggerFactory.getLogger(ConvertR5toR4.class);

    @OperationParam(
            alias = {"ptd", "pathtodir"},
            setter = "setPathToDirectory",
            required = true,
            description = "Path to the directory containing the R5 resource files to be converted (required)")
    private String pathToDirectory;

    @OperationParam(
            alias = {"e", "encoding"},
            setter = "setEncoding",
            defaultValue = "json",
            description = "The file format for representing the resulting Bundle { json, xml } (default json)")
    private String encoding = "json";

    @OperationParam(
            alias = {"bid", "bundleid"},
            setter = "setBundleId",
            description = "A valid FHIR ID for the resulting Bundle (default random UUID)")
    private String bundleId = UUID.randomUUID().toString();

    @OperationParam(
            alias = {"bt", "bundletype"},
            setter = "setBundleType",
            defaultValue = "transaction",
            description = "The Bundle type { transaction, collection } (default transaction)")
    private String bundleType = "transaction";

    @OperationParam(
            alias = {"ofn", "outputfilename"},
            setter = "setOutputFileName",
            description = "The output file name (optional, defaults to bundleId)")
    private String outputFileName;

    @OperationParam(
            alias = {"op", "outputpath"},
            setter = "setOutputPath",
            defaultValue = "src/main/resources/org/opencds/cqf/tooling/convert/output",
            description =
                    "The directory path to which the converted Bundle should be written (default src/main/resources/org/opencds/cqf/tooling/convert/output)")
    private String outputPath = "src/main/resources/org/opencds/cqf/tooling/convert/output";

    @Override
    public void execute() {
        validateEncoding();
        validatePathToDirectory();
        validateBundleType();

        var bundleTypeEnum = BundleUtils.getBundleType(this.bundleType);
        if (bundleTypeEnum == null) {
            logger.error("Invalid bundle type: {}", this.bundleType);
        } else {
            var bundle = convertResources(
                    bundleId,
                    bundleTypeEnum,
                    IOUtils.readResources(
                            IOUtils.getFilePaths(pathToDirectory, true), FhirContextCache.getContext("r5")));

            IOUtils.writeResource(
                    bundle,
                    outputPath,
                    IOUtils.Encoding.parse(encoding),
                    FhirContextCache.getContext("r4"),
                    true,
                    outputFileName != null ? outputFileName : bundleId);
        }
    }

    private void validateBundleType() {
        if (bundleType == null) {
            throw new IllegalArgumentException("BundleType cannot be null");
        }

        if (!isBundleTypeAllowed(bundleType)) {
            throw new IllegalArgumentException(String.format(
                    "The bundle type [%s] is invalid. Allowed Types: %s",
                    bundleType, String.join(", ", allowedBundleTypes())));
        }
    }

    private void validateEncoding() {
        if (encoding == null || encoding.isEmpty()) {
            encoding = "json";
        } else {
            if (!encoding.equalsIgnoreCase("xml") && !encoding.equalsIgnoreCase("json")) {
                throw new IllegalArgumentException(
                        String.format("Unsupported encoding: %s. Allowed encodings { json, xml }", encoding));
            }
        }
    }

    private void validatePathToDirectory() {
        if (pathToDirectory == null) {
            throw new IllegalArgumentException(
                    String.format("The path [%s] to the resource directory is required", pathToDirectory));
        }

        var resourceDirectory = new File(pathToDirectory);
        if (!resourceDirectory.isDirectory()) {
            throw new RuntimeException(
                    String.format("The specified path [%s] to resource files is not a directory", pathToDirectory));
        }

        var resources = resourceDirectory.listFiles();
        if (resources == null || resources.length == 0) {
            throw new RuntimeException(
                    String.format("The specified path [%s] to resource files is empty", pathToDirectory));
        }
    }

    private IBaseBundle convertResources(
            String bundleId, BundleTypeEnum type, @Nonnull List<IBaseResource> resourcesToConvert) {
        var convertedResources = new ArrayList<org.hl7.fhir.r4.model.Resource>();
        for (var resource : resourcesToConvert) {
            if (resource instanceof org.hl7.fhir.r5.model.Resource) {
                convertedResources.add(ResourceAndTypeConverter.r5ToR4Resource(resource));
            }
        }

        var context = FhirContextCache.getContext("r4");
        var builder = new BundleBuilder(context);
        if (type == BundleTypeEnum.COLLECTION) {
            convertedResources.forEach(builder::addCollectionEntry);
        } else {
            convertedResources.forEach(builder::addTransactionUpdateEntry);
        }
        var bundle = builder.getBundle();
        bundle.setId(bundleId == null ? UUID.randomUUID().toString() : bundleId);
        return bundle;
    }

    public void setPathToDirectory(String pathToDirectory) {
        this.pathToDirectory = pathToDirectory;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public void setBundleType(String bundleType) {
        this.bundleType = bundleType;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
