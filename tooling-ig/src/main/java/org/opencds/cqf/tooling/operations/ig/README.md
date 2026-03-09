# IG Operations

The operations defined in this package provide support for building, refreshing, testing, and scaffolding
FHIR Implementation Guide (IG) projects.

## RefreshIG Operation

This operation refreshes all generated content (Libraries, Measures, PlanDefinitions) within an IG project. It
orchestrates CQL compilation, Library refresh, Measure refresh, and PlanDefinition refresh. This is the primary
operation used during IG development.

### Arguments:
Arguments are parsed from the IG ini file and project structure via RefreshIGArgumentProcessor.

- -ini (required) - Path to the IG ini file
- -root-dir | -rd (optional) - Root directory of the IG
- -ig-path | -ip (optional) - Path to the IG, relative to the root directory
- -encoding | -e (optional) - Output encoding { json, xml }
  - Default encoding: json
- -versioned | -v (optional) - If omitted resources must be uniquely named
  - Default: false
- -stamp | -ss (optional) - Whether refreshed resources should be stamped with the cqf-tooling stamp
  - Default: true
- -timestamp | -ts (optional) - Whether refreshed Bundles should attach timestamp of creation
  - Default: false
- -libraryOutput | -lop (optional) - If omitted, the libraries will overwrite any existing libraries
- -measureOutput | -mop (optional) - If omitted, the measures will overwrite any existing measures

## RefreshIGLegacy Operation

Legacy version of the RefreshIG operation with additional packaging and server upload capabilities. Provides
fine-grained control over what content is included in packages (ELM, dependencies, terminology, patient scenarios).

### Arguments:
- -ini (required) - Path to the IG ini file
- -root-dir | -rd (optional) - Root directory of the IG
- -ig-path | -ip (optional) - Path to the IG, relative to the root directory
- -encoding | -e (optional) - Output encoding { json, xml }
  - Default encoding: json
- -skip-packages | -s (optional) - Whether to skip package building
  - Default: false
- -include-elm | -elm (optional) - Whether to produce and package ELM
  - Default: false
- -include-dependencies | -d (optional) - Whether to package dependent CQL libraries
  - Default: false
- -include-terminology | -t (optional) - Whether to package terminology
  - Default: false
- -include-patients | -p (optional) - Whether to package patient scenario information
  - Default: false
- -versioned | -v (optional) - If omitted resources must be uniquely named
  - Default: false
- -updated-version | -uv (optional) - Version for the new libraries
- -fhir-uri | -fs (optional) - FHIR server to load the final bundle to
- -measure-to-refresh-path | -mtrp (optional) - Path to Measure to refresh
- -resourcepath | -rp (optional) - Resource directories, relative to the root directory (use multiple times)
- -librarypath | -lp (optional) - Single path for library resources, relative to root directory
- -libraryOutput | -lop (optional) - If omitted, the libraries will overwrite any existing libraries
- -measureOutput | -mop (optional) - If omitted, the measures will overwrite any existing measures
- -stamp | -ss (optional) - Whether to apply the cqf-tooling stamp
  - Default: true
- -timestamp | -ts (optional) - Whether to attach a timestamp to Bundles
  - Default: false
- -include-errors | -x (optional) - Include complete list of errors upon failure
  - Default: false
- -popDataRequirements | -pldr (optional) - Include population-level data requirements in measures
  - Default: false

## Scaffold Operation

This operation scaffolds an IG project by creating the directory structure and initial resources needed
for a new IG.

### Arguments:
- -ig-path | -ip (required) - Root directory of the IG
- -ig-version | -iv (optional) - The desired FHIR version
- -encoding | -e (optional) - Output encoding { json, xml }
  - Default encoding: json
- -resource-name | -rn (optional) - Names of resources that should be created (use multiple times)
- -software (optional) - Software systems (format: Name=Version, use multiple times)

## TestIG Operation

This operation runs test cases against a FHIR server for an IG project.

### Arguments:
- -testsPath | -tcp (required) - Path to the directory containing test cases
- -fhir-uri | -fs (required) - URI of the FHIR server to test on
- -ini (optional) - IG ini file
- -root-dir (optional) - Root directory of the IG
- -ig-path | -ip (optional) - Path to the IG, relative to the root directory
- -fhir-version | -fv (optional) - FHIR version

## BundleIG Operation

This operation bundles the contents of an IG project into FHIR Bundle resources suitable for loading into a
FHIR server.

### Arguments:
- -pathtoig | -ptig (required) - Path to ImplementationGuide project root (where ig.json is stored)
- -encoding | -e (optional) - Preferred encoding { json, xml }
  - Default encoding: json
- -outputpath | -op (optional) - Path to directory where Bundles will be written
  - Default output path: src/main/resources/org/opencds/cqf/tooling/igtools/output
