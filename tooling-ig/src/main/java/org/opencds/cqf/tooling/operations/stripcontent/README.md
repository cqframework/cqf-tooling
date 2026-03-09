# Strip Content Operations

The operations defined in this package provide support for stripping generated content from FHIR resources.

## StripGeneratedContent Operation

This operation removes generated content (narratives, ELM, data requirements, etc.) from FHIR resources. This is
useful when preparing resources for source control to reduce diff noise from generated content.

### Arguments:
- -pathtores | -ptr (required) - Path to the directory containing the resources to strip
- -version | -v (optional) - FHIR version { dstu3, r4, r5 }
- -outputpath | -op (optional) - Path to directory where stripped content will be written
  - Default: overwrites original files
- -cql (optional) - Path to directory where CQL content should be exported
