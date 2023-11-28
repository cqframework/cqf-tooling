# Bundle Operations

The operations defined in this package provide support for composing, decomposing, and loading FHIR Bundle resources.

## BundleResources Operation

This operation consolidates all resources from files in the 'pathtodirectory' directory into a single FHIR Bundle with 
an ID that is the value specified in the 'bunldeid' argument and outputs that generated Bundle in file format of the 
type specified by the 'encoding' argument to the 'outputpath' directory.

### Arguments:
- -pathtodirectory | -ptd (required) - Path to the directory containing the resource files to be consolidated into 
the new bundle
- -outputpath | -op (optional) - The directory path to which the generated Bundle file should be written
  - Default output path: src/main/resources/org/opencds/cqf/tooling/bundle/output
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
  - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting Bundle { json, xml }
  - Default encoding: json
- -type | -t (optional) - The Bundle type as defined in the FHIR specification for the Bundle.type element
  - Default type: transaction
- -bundleid | -bid (optional) - A valid FHIR ID to be used as the ID for the resulting FHIR Bundle. The Publisher 
validation for a Bundle requires a Bundle to have an ID. If no ID is provided, the output Bundle will not have an ID value.

## BundleToResources Operation

This operation decomposes a Bundle entry into separate FHIR resource files.

### Arguments:
- -pathtobundle | -ptb (required) - Path to the bundle to decompose
- -outputpath | -op (optional) - The directory path to which the resource files should be written
  - Default output path: src/main/resources/org/opencds/cqf/tooling/bundle/output
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
  - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting resources { json, xml }
  - Default encoding: json

## BundleTransaction Operation

This operation performs the $transaction operation for a directory of FHIR Bundle resources on a specified FHIR server.
TODO: add authentication args

### Arguments:
- -pathtobundles | -ptb (required) - Path to the bundles to load into the FHIR server
- -fhirserver | -fs (required) - The FHIR server where the $transaction operation is executed 
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
  - Default version: r4

## BundlesToBundle Operation

TODO