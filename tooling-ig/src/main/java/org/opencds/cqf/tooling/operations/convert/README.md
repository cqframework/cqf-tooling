# Convert Operations

The operations defined in this package provide support for converting FHIR resources between versions.

## ConvertR5toR4 Operation

This operation converts R5 FHIR resources in a directory to R4 and bundles the converted resources into a single
FHIR Bundle.

### Arguments:
- -pathtodir | -ptd (required) - Path to the directory containing the R5 resource files to be converted
- -encoding | -e (optional) - The file format for representing the resulting Bundle { json, xml }
  - Default encoding: json
- -bundleid | -bid (optional) - A valid FHIR ID for the resulting Bundle
  - Default: random UUID
- -bundletype | -bt (optional) - The Bundle type { transaction, collection }
  - Default type: transaction
- -outputfilename | -ofn (optional) - The output file name
  - Default: bundleId
- -outputpath | -op (optional) - The directory path to which the converted Bundle should be written
  - Default output path: src/main/resources/org/opencds/cqf/tooling/convert/output
