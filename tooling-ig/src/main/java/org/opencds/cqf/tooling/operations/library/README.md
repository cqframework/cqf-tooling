# Library Operations

The operations defined in this package provide support for refreshing FHIR Library resources.

## RefreshLibrary Operation

This operation refreshes a FHIR Library resource content (CQL and ELM), data requirements, related artifacts, and
parameters (in and out).

### Arguments:
- -pathtolibrary | -ptl (required) - Path to the FHIR Library resource to refresh
- -pathtocql | -ptcql (required) - Path to the CQL content referenced or depended on by the FHIR Library resource
to refresh
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
  - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting FHIR Library { json, xml }
  - Default encoding: json
- -outputpath | -op (optional) - The directory path to which the generated FHIR Library resources should be written
  - Default output path: same as -pathtolibrary (-ptl)
