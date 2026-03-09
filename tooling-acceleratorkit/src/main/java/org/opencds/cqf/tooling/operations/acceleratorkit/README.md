# Accelerator Kit Operations

The operations defined in this package provide support for processing WHO SMART Guidelines Accelerator Kit
content from spreadsheet-based authoring formats into FHIR resources.

## ProcessAcceleratorKit Operation

This operation processes a WHO SMART Guidelines Accelerator Kit spreadsheet and generates the corresponding
FHIR resources including PlanDefinitions, ActivityDefinitions, Questionnaires, StructureDefinitions, and
associated CQL libraries.

### Arguments:
- -pathtospreadsheet | -pts (required) - Path to the Accelerator Kit spreadsheet
- -encoding | -e (optional) - Output encoding { json, xml }
  - Default encoding: json
- -scopes | -s (optional) - Processing scopes to limit which content is generated
- -dataelementpages | -dep (optional) - Comma-separated list of the names of pages in the workbook to be processed
- -testcases | -tc (optional) - Path to a spreadsheet containing test case data
- -outputpath | -op (optional) - The directory path to which the generated resources should be written
  - Default output path: src/main/resources/org/opencds/cqf/tooling/acceleratorkit/output

## ProcessDecisionTables Operation

This operation processes decision table pages from a WHO SMART Guidelines Accelerator Kit spreadsheet and generates
the corresponding PlanDefinition and CQL resources.

### Arguments:
- -pathtospreadsheet | -pts (required) - Path to the Accelerator Kit spreadsheet
- -encoding | -e (optional) - Output encoding { json, xml }
  - Default encoding: json
- -decisiontablepages | -dtp (optional) - Comma-separated list of the names of decision table pages in the workbook
to be processed
- -decisiontablepageprefix | -dtpf (optional) - All pages with a name starting with this prefix will be processed
- -outputpath | -op (optional) - The directory path to which the generated resources should be written
  - Default output path: src/main/resources/org/opencds/cqf/tooling/acceleratorkit/output
