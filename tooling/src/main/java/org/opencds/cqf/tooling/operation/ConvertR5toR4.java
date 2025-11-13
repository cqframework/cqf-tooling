package org.opencds.cqf.tooling.operation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.util.BundleBuilder;
import jakarta.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ConvertR5toR4 extends Operation {

    public enum AllowedBundleType {
        COLLECTION(BundleTypeEnum.COLLECTION),
        TRANSACTION(BundleTypeEnum.TRANSACTION);

        private final BundleTypeEnum reference;
        AllowedBundleType(BundleTypeEnum reference) {
            this.reference = reference;
        }


    }

    public static Boolean isBundleTypeAllowed(String bundleType) {
        return Arrays.stream(AllowedBundleType.values())
                .anyMatch(e -> e.name().equalsIgnoreCase(bundleType));
    }

    public static List<String> allowedBundleTypes() {
        return Arrays.stream(AllowedBundleType.values())
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
    private String bundleType = "collection";               // -type (-t)
    private String outputFileName = null;                   // -outputfilename (-ofn)

    private void extractOptionsFromArgs(String[] args) {
        for (String arg : args) {
            if (arg.equals("-ConvertR5toR4")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

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

        File resourceDirectory = new File(pathToDirectory);
        if (!resourceDirectory.isDirectory()) {
            throw new RuntimeException(String.format("The specified path [%s] to resource files is not a directory", pathToDirectory));
        }

        File[] resources = resourceDirectory.listFiles();
        if (resources == null) {
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

        BundleTypeEnum bundleType = BundleUtils.getBundleType(this.bundleType);
        if (bundleType == null) {
            logger.error("Invalid bundle type: {}", this.bundleType);
        }
        else {
            IBaseBundle bundle = convertResources(
                    bundleId,
                    bundleType,
                    IOUtils.readResources(
                            IOUtils.getFilePaths(pathToDirectory, true),
                            FhirContextCache.getContext("r5")));

            IOUtils.writeResource(
                    bundle,
                    getOutputPath() == null ? pathToDirectory : getOutputPath(),
                    IOUtils.Encoding.parse(encoding),
                    FhirContextCache.getContext("r4"),
                    true,
                    outputFileName != null ? outputFileName : bundleId);
        }
    }

    private IBaseBundle convertResources(String bundleId, BundleTypeEnum type,
                                         @Nonnull List<IBaseResource> resourcesToConvert) {
      VersionConvertor_40_50 versionConverter4050 = new VersionConvertor_40_50(new BaseAdvisor_40_50(true));

      List<org.hl7.fhir.r4.model.Resource> convertedResources = new ArrayList<>();
      for (IBaseResource resource: resourcesToConvert){
        if (resource instanceof org.hl7.fhir.r5.model.Resource) {
            convertedResources.add(versionConverter4050.convertResource((org.hl7.fhir.r5.model.Resource)resource));
        }
      }

      FhirContext context = FhirContextCache.getContext("r4");
      BundleBuilder builder = new BundleBuilder(context);
      if (type == BundleTypeEnum.COLLECTION) {
         builder.setType(type.getCode());
         convertedResources.forEach(builder::addCollectionEntry);
      }
      else {
         builder.setType("transaction");
         convertedResources.forEach(builder::addTransactionUpdateEntry);
      }
      IBaseBundle bundle = builder.getBundle();
      bundle.setId(bundleId == null ? UUID.randomUUID().toString() : bundleId);
      return bundle;
    }
}
