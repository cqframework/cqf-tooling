package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.util.BundleBuilder;
import jakarta.annotation.Nonnull;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConvertR5toR4 extends Operation {

    public static final List<BundleTypeEnum> ALLOWED_BUNDLE_TYPES = List.of(
            BundleTypeEnum.COLLECTION,
            BundleTypeEnum.TRANSACTION
    );

    public static Boolean isBundleTypeAllowed(String bundleType) {
        if (bundleType == null) {
            return false;
        }
        return ALLOWED_BUNDLE_TYPES.stream()
                .anyMatch(bt -> bt.name().equalsIgnoreCase(bundleType));
    }

    public static List<String> allowedBundleTypes() {
        return ALLOWED_BUNDLE_TYPES.stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private static final Logger logger = LoggerFactory.getLogger(ConvertR5toR4.class);
    // COMMAND LINE ARGUMENTS - REQUIRED
    private String pathToDirectory;                         // -pathtodir (-ptd)

    // COMMAND LINE ARGUMENTS - OPTIONAL
    private String encoding = "json";                       // -encoding (-e)
    private String bundleId = UUID.randomUUID().toString(); // -bundleid (-bid)
    private String bundleType = "transaction";              // -type (-t)
    private String outputFileName = null;                   // -outputfilename (-ofn)

    private void extractOptionsFromArgs(String[] args) {
        for (String arg : args) {
            if (arg.equals("-ConvertR5toR4")) continue;
            var flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            var flag = flagAndValue[0];
            var value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "bundleid":
                case "bid":
                    bundleId = value;
                    break;
                case "bundletype":
                case "bt":
                    bundleType = value;
                    break;
                case "encoding":
                case "e":
                    encoding = value.toLowerCase();
                    break;
                case "outputfilename":
                case "ofn":
                    outputFileName = value;
                    break;
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break;
                case "pathtodir":
                case "ptd":
                    pathToDirectory = value;
                    break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
    }

    private void validateBundleType() {
        if (bundleType == null) {
            throw new IllegalArgumentException("BundleType cannot be null");
        }

        if (!isBundleTypeAllowed(bundleType)) {
            throw new IllegalArgumentException(String.format("The bundle type [%s] is invalid. Allowed Types: %s", bundleType, String.join(", ",allowedBundleTypes())));
        }
    }

    private void validateEncoding() {
        if (encoding == null || encoding.isEmpty()) {
            encoding = "json";
        } else {
            if (!encoding.equalsIgnoreCase("xml") && !encoding.equalsIgnoreCase("json")) {
                throw new IllegalArgumentException(String.format("Unsupported encoding: %s. Allowed encodings { json, xml }", encoding));
            }
        }
    }

    private void validatePathToDirectory() {
        if (pathToDirectory == null) {
            throw new IllegalArgumentException(String.format("The path [%s] to the resource directory is required", pathToDirectory));
        }

        var resourceDirectory = new File(pathToDirectory);
        if (!resourceDirectory.isDirectory()) {
            throw new RuntimeException(String.format("The specified path [%s] to resource files is not a directory", pathToDirectory));
        }

        var resources = resourceDirectory.listFiles();
        if (resources == null || resources.length == 0) {
            throw new RuntimeException(String.format("The specified path [%s] to resource files is empty", pathToDirectory));
        }
    }

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/convert/output"); // default

        extractOptionsFromArgs(args);
        validateEncoding();
        validatePathToDirectory();
        validateBundleType();

        var bundleType = BundleUtils.getBundleType(this.bundleType);
        if (bundleType == null) {
            logger.error("Invalid bundle type: {}", this.bundleType);
        }
        else {
            var bundle = convertResources(
                    bundleId,
                    bundleType,
                    IOUtils.readResources(
                            IOUtils.getFilePaths(pathToDirectory, true),
                            FhirContextCache.getContext("r5")));

            IOUtils.writeResource(
                    bundle,
                    getOutputPath(),
                    IOUtils.Encoding.parse(encoding),
                    FhirContextCache.getContext("r4"),
                    true,
                    outputFileName != null ? outputFileName : bundleId);
        }
    }

    private IBaseBundle convertResources(String bundleId, BundleTypeEnum type,
                                         @Nonnull List<IBaseResource> resourcesToConvert) {
      var convertedResources = new ArrayList<org.hl7.fhir.r4.model.Resource>();
      for (var resource : resourcesToConvert){
        if (resource instanceof org.hl7.fhir.r5.model.Resource) {
            convertedResources.add(ResourceAndTypeConverter.r5ToR4Resource(resource));
        }
      }

      var context = FhirContextCache.getContext("r4");
      var builder = new BundleBuilder(context);
      if (type == BundleTypeEnum.COLLECTION) {
         convertedResources.forEach(builder::addCollectionEntry);
      }
      else {
         convertedResources.forEach(builder::addTransactionUpdateEntry);
      }
      var bundle = builder.getBundle();
      bundle.setId(bundleId == null ? UUID.randomUUID().toString() : bundleId);
      return bundle;
    }
}
