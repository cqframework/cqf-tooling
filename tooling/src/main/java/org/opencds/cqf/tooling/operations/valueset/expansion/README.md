# FhirTxExpansion Operation

The purpose of this operation is to run the $expand operation on a provided ValueSet resource to a provided FHIR 
terminology service and return the expanded ValueSet resource.

TODO - provide authentication support for the provided terminology service

## Arguments:
- -pathtovalueset | -ptvs (required) - The path to the FHIR ValueSet resource(s) to be expanded (this may be a file or directory)
- -fhirserver | -fs - The FHIR server url that performs the $expand operation
    - Default value: http://tx.fhir.org/r4
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
    - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting expanded FHIR ValueSet resource { json, xml }.
    - Default encoding: json
- -outputpath | -op (optional) - The directory path to which the resulting expanded FHIR ValueSet resource should be written.
    - Default output path: src/main/resources/org/opencds/cqf/tooling/terminology/output