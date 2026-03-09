# Measure Operations

The operations defined in this package provide support for testing FHIR Measure resources.

## ExecuteMeasureTest Operation

This operation evaluates a FHIR Measure against test data, either locally or against a remote FHIR server.

### Arguments:
- -test-path | -tp (required) - Path to the test data bundle
- -content-path | -cp (optional) - Path to the measure content bundle. Required if running locally
- -fhir-version | -fv (optional) - FHIR version
  - Default version: R4
- -fhir-server | -fs (optional) - URL of the FHIR server to use for evaluation
- -encoding | -e (optional) - Desired output encoding for resources
  - Default encoding: json
