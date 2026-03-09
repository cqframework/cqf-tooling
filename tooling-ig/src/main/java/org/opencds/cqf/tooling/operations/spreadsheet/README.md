# Spreadsheet Operations

The operations defined in this package provide support for exporting FHIR profile information to spreadsheet format.

## ProfilesToSpreadsheet Operation

This operation exports FHIR profile StructureDefinitions to an Excel spreadsheet (.xlsx), documenting the elements,
cardinality, types, and bindings defined in the profiles.

### Arguments:
- -inputpath | -ip (required) - Path to spec files directory
- -outputpath | -op (required) - Directory to save output to; the file name is the modelName + modelVersion + .xlsx
- -resourcepaths | -rp (required) - Path to the individual specs and versions to use
(e.g. '4.0.1;US-Core/3.1.0;QI-Core/4.1.0')
- -modelname | -mn (required) - Name of the model to parse
- -modelversion | -mv (required) - Version of the model to parse
- -snapshotonly | -sp (optional) - Flag to determine if the differential should be traversed; false means traverse
the differential
  - Default: true

## QICoreElementsToSpreadsheet Operation

This operation exports QICore profile elements to an Excel spreadsheet (.xlsx). Functions similarly to
ProfilesToSpreadsheet but with QICore-specific processing.

### Arguments:
- -inputpath | -ip (required) - Path to spec files directory
- -outputpath | -op (required) - Directory to save output to; the file name is the modelName + modelVersion + .xlsx
- -resourcepaths | -rp (required) - Path to the individual specs and versions to use
(e.g. '4.0.1;US-Core/3.1.0;QI-Core/4.1.0')
- -modelname | -mn (required) - Name of the model to parse
- -modelversion | -mv (required) - Version of the model to parse
- -snapshotonly | -sp (optional) - Flag to determine if the differential should be traversed; false means traverse
the differential
  - Default: true
