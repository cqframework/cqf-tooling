# CqlToLibrary Operation

This operation transforms all CQL content in the 'pathtocqlcontent' directory or file into FHIR Library resources. 
The resulting Library resource(s) will contain the following elements if applicable:

- Library.id - combined named identifier and version associated with the CQL library
- Library.name - named identifier associated with the CQL library
- Library.version - version associated with the CQL library
- Library.experimental - defaulted to true
- Library.status - defaulted to 'active'
- Library.type - defaulted to 'logic-library'
- Library.relatedArtifact - contains the Library dependencies (terminology and other Libraries)
- Library.parameter - contains the parameters defined in the CQL library
- Library.dataRequirement - contains the data requirements for the CQL library
- Library.content - contains the base64 encoded CQL and ELM content for the CQL library

## Arguments:
- -pathtocqlcontent | -ptcql (required) - Path to the directory or file containing the CQL content to be transformed 
into FHIR Library resources
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
    - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting FHIR Library { json, xml }
    - Default encoding: json
- -outputpath | -op (optional) - The directory path to which the generated FHIR Library resources should be written
  - Default output path: src/main/resources/org/opencds/cqf/tooling/library/output