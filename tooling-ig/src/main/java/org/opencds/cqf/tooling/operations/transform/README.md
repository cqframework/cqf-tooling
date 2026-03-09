# Transform Operations

The operations defined in this package provide support for transforming FHIR resources using StructureMap and
ConceptMap resources.

## StructureMapping Operation

This operation transforms patient data using FHIR StructureMap resources.

### Arguments:
- -pathtopatientdata | -ptpd (required) - Path to the patient data represented as either a FHIR Bundle resource or as
flat files within a directory
- -pathtostructuremaps | -ptsm (required) - Path to the FHIR StructureMap resource(s) used for the transformation
(can be either a file or directory)
- -packageurl | -purl (required) - Url for the FHIR packages to use for transformation
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
  - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting transformed FHIR resources
{ json, xml }
  - Default encoding: json
- -outputpath | -op (optional) - The directory path to which the transformed FHIR resources should be written
  - Default: replaces existing resources within the IG

## ConceptMapping

Placeholder for ConceptMap-based transformation support. Not yet implemented.
