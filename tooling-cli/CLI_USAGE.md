# CQF Tooling CLI Usage Guide

## Overview

The CQF Tooling CLI provides commands for FHIR Implementation Guide authoring, terminology management, CQL library processing, measure testing, and more.

```
java -jar tooling-cli-<version>.jar [COMMAND] [SUBCOMMAND] -- [ARGS]
```

Arguments are passed after a `--` separator in `-key=value` format.

## Commands

### ig — Implementation Guide Operations

#### ig refresh
Refresh an Implementation Guide (recommended).

```bash
java -jar tooling-cli.jar ig refresh -- -ini=path/to/ig.ini -rd=path/to/root -e=json
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-ini` | | Path to the IG ini file |
| `-root-dir` | `-rd` | Root directory of the IG |
| `-encoding` | `-e` | Output encoding: `json` or `xml` |
| `-ip` | `-igPath` | Path to the IG resource |
| `-t` | `-fhir-version` | FHIR version |
| `-elm` | `-includeElm` | Include ELM in output |
| `-d` | `-includeDependencies` | Include dependencies |
| `-p` | `-includePatients` | Include test patients |
| `-ts` | `-includeTerminology` | Include terminology |
| `-ss` | `-stamp` | Stamp software system info |
| `-sp` | `-skipPackages` | Skip NPM packages |
| `-v` | `-versioned` | Use versioned output |

#### ig refresh-legacy
Refresh an IG using the legacy (pre-NewRefreshIG) processor.

```bash
java -jar tooling-cli.jar ig refresh-legacy -- -ini=path/to/ig.ini
```

#### ig scaffold
Scaffold a new Implementation Guide project structure.

```bash
java -jar tooling-cli.jar ig scaffold -- -op=path/to/output
```

#### ig test
Execute tests for an Implementation Guide.

```bash
java -jar tooling-cli.jar ig test -- -ini=path/to/ig.ini -e=json
```

#### ig bundle
Bundle an Implementation Guide's resources.

```bash
java -jar tooling-cli.jar ig bundle -- -ptd=path/to/ig/directory -e=json -v=r4
```

---

### terminology — Terminology and ValueSet Operations

#### terminology vsac-xlsx
Convert a VSAC Excel spreadsheet (.xlsx) to a FHIR ValueSet resource.

```bash
java -jar tooling-cli.jar terminology vsac-xlsx -- \
  -pts=path/to/spreadsheet.xlsx -op=path/to/output -e=json
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-pathtospreadsheet` | `-pts` | Path to the VSAC .xlsx file (required) |
| `-outputpath` | `-op` | Output directory |
| `-encoding` | `-e` | Output encoding: `json` or `xml` |
| `-metasheetnum` | `-msn` | Metadata sheet number |
| `-metanamerow` | `-mnr` | Metadata name row |
| `-metaoidrow` | `-mor` | Metadata OID row |
| `-metastewardrow` | `-msr` | Metadata steward row |
| `-codesheetnum` | `-csn` | Code sheet number |
| `-codelistrow` | `-clr` | Code list starting row |
| `-codecol` | `-cc` | Code column |
| `-descriptioncol` | `-dc` | Description column |
| `-systemnamecol` | `-snc` | System name column |
| `-versioncol` | `-vc` | Version column |
| `-systemoidcol` | `-soc` | System OID column |

#### terminology distributable
Convert a distributable Excel spreadsheet to a ValueSet.

```bash
java -jar tooling-cli.jar terminology distributable -- -pts=path/to/spreadsheet.xlsx
```

#### terminology vsac-multi
Convert a CMS flat multi-ValueSet spreadsheet.

```bash
java -jar tooling-cli.jar terminology vsac-multi -- -pts=path/to/spreadsheet.xlsx -op=path/to/output
```

#### terminology vsac-batch
Batch convert VSAC Excel spreadsheets to ValueSets.

```bash
java -jar tooling-cli.jar terminology vsac-batch -- -pts=path/to/spreadsheet.xlsx -op=path/to/output
```

#### terminology hedis-xlsx
Convert a HEDIS Excel spreadsheet to a ValueSet.

```bash
java -jar tooling-cli.jar terminology hedis-xlsx -- -pts=path/to/spreadsheet.xlsx -op=path/to/output
```

#### terminology xlsx
Convert a generic Excel spreadsheet to a FHIR ValueSet. Highly configurable.

```bash
java -jar tooling-cli.jar terminology xlsx -- -pts=path/to/spreadsheet.xlsx -op=path/to/output -e=json
```

#### terminology template
Generate ValueSets from a template spreadsheet.

```bash
java -jar tooling-cli.jar terminology template -- \
  -pts=path/to/template.xlsx -op=path/to/output -opp=prefix -opv=r4
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-pathtospreadsheet` | `-pts` | Path to the template .xlsx file |
| `-outputpath` | `-op` | Output directory |
| `-outputprefix` | `-opp` | Prefix for output files |
| `-outputversion` | `-opv` | FHIR version for output (default: R4) |
| `-encoding` | `-e` | Output encoding |

#### terminology ensure-executable
Ensure a ValueSet has an expansion. Can also infer a compose from an expansion (computable mode).

```bash
# Ensure executable (generate expansion from compose)
java -jar tooling-cli.jar terminology ensure-executable -- \
  -vsp=path/to/valuesets -op=path/to/output -cpg -f

# Ensure computable (infer compose from expansion)
java -jar tooling-cli.jar terminology ensure-executable -- \
  -vsp=path/to/valuesets -op=path/to/output -cpg -f -sv
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-valuesetpath` | `-vsp` | Path to ValueSet file(s) |
| `-outputpath` | `-op` | Output directory |
| `-declarecpg` | `-cpg` | Mark with CPG profile indicators |
| `-force` | `-f` | Recompute even if expansion/compose exists |
| `-skipversion` | `-sv` | Skip code system versions in inferred compose |

#### terminology to-json-db
Convert ValueSets to JSON database format.

```bash
java -jar tooling-cli.jar terminology to-json-db -- -pts=path/to/valuesets
```

#### terminology jurisdictions
Convert an RCKMS jurisdictions spreadsheet to a FHIR CodeSystem.

```bash
java -jar tooling-cli.jar terminology jurisdictions -- \
  -pts=path/to/rckms-jurisdictions.xlsx -op=path/to/output
```

#### terminology spreadsheet-to-cql
Convert spreadsheet rows to CQL expressions.

```bash
java -jar tooling-cli.jar terminology spreadsheet-to-cql -- -pts=path/to/spreadsheet.xlsx -op=path/to/output
```

#### terminology validate
Validate ValueSets and CodeSystems from a spreadsheet.

```bash
java -jar tooling-cli.jar terminology validate -- -pts=path/to/spreadsheet.xlsx
```

---

### library — CQL Library Operations

#### library generate-r4
Generate FHIR R4 Library resources from CQL files.

```bash
java -jar tooling-cli.jar library generate-r4 -- \
  -ptld=path/to/cql/directory -op=path/to/output -e=json
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-pathtolibrarydirectory` | `-ptld` | Path to CQL library directory (required) |
| `-outputpath` | `-op` | Output directory |
| `-encoding` | `-e` | Output encoding: `json` or `xml` |

#### library generate-stu3
Generate FHIR STU3 Library resources from CQL files.

```bash
java -jar tooling-cli.jar library generate-stu3 -- \
  -ptld=path/to/cql/directory -op=path/to/output -e=json
```

#### library refresh
Refresh a Library resource with updated CQL content.

```bash
java -jar tooling-cli.jar library refresh -- \
  -ini=path/to/ig.ini -fv=fhir4 -lp=path/to/Library.json -ss=false
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-ini` | | Path to IG ini file |
| `-fv` | `-fhir-version` | FHIR version (`fhir3` or `fhir4`) |
| `-lp` | `-libraryPath` | Path to the Library resource |
| `-ss` | `-stamp` | Stamp software system info |

---

### measure — Measure Operations

#### measure refresh-r4
Refresh R4 Measure resources with updated data requirements.

```bash
java -jar tooling-cli.jar measure refresh-r4 -- \
  -ptm=path/to/measures -ptl=path/to/libraries -o=path/to/output -e=json
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-pathToMeasures` | `-ptm` | Path to Measure resources |
| `-pathToLibraries` | `-ptl` | Path to Library resources |
| `-output` | `-o` | Output directory |
| `-encoding` | `-e` | Output encoding |
| `-stamp` | `-ss` | Stamp software system info |

#### measure refresh-stu3
Refresh STU3 Measure resources.

```bash
java -jar tooling-cli.jar measure refresh-stu3 -- \
  -ptm=path/to/measures -ptl=path/to/libraries -o=path/to/output
```

#### measure test
Execute a Measure test case against a FHIR server.

```bash
java -jar tooling-cli.jar measure test -- \
  -test-path=path/to/test-bundle.json \
  -content-path=path/to/content-bundle.json \
  -fhir-server=http://localhost:8080/fhir
```

---

### bundle — FHIR Bundle Operations

#### bundle resources
Consolidate resources from a directory into a single FHIR Bundle.

```bash
java -jar tooling-cli.jar bundle resources -- \
  -ptd=path/to/resources -op=path/to/output -v=r4 -e=json -bid=my-bundle-id
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-pathtodirectory` | `-ptd` | Directory containing resource files (required) |
| `-outputpath` | `-op` | Output directory |
| `-version` | `-v` | FHIR version: `dstu2`, `stu3`, `r4` (default: `stu3`) |
| `-encoding` | `-e` | Output encoding: `json` or `xml` (default: `json`) |
| `-bundleid` | `-bid` | FHIR ID for the resulting Bundle |

#### bundle to-resources
Decompose a Bundle into individual resource files.

```bash
java -jar tooling-cli.jar bundle to-resources -- \
  -p=path/to/bundle.json -e=json -op=path/to/output
```

#### bundle to-transaction
Convert a collection Bundle to a transaction Bundle.

```bash
java -jar tooling-cli.jar bundle to-transaction -- \
  -p=path/to/bundle.json -e=json -op=path/to/output
```

#### bundle post
POST all Bundles in a directory to a FHIR server.

```bash
java -jar tooling-cli.jar bundle post -- -ptd=path/to/bundles -fs=http://localhost:8080/fhir
```

#### bundle publish
Publish a Bundle.

```bash
java -jar tooling-cli.jar bundle publish -- -p=path/to/bundle.json
```

---

### acceleratorkit — WHO Accelerator Kit Operations

#### acceleratorkit process
Process a WHO Accelerator Kit data dictionary into profiles, questionnaires, PlanDefinitions, and libraries.

```bash
java -jar tooling-cli.jar acceleratorkit process -- \
  -pts=ANC-Primary-Data-Dictionary.xlsx \
  -dep="ANC Reg,Quick Check,Profile,S&F,PE,Tests,C&T" \
  -op=path/to/output -e=json
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-pathtospreadsheet` | `-pts` | Path to the data dictionary .xlsx file |
| `-dataelementpages` | `-dep` | Comma-separated sheet names to process |
| `-outputpath` | `-op` | Output directory |
| `-encoding` | `-e` | Output encoding |

#### acceleratorkit decision-tables
Process WHO Accelerator Kit decision tables into PlanDefinition resources.

```bash
java -jar tooling-cli.jar acceleratorkit decision-tables -- \
  -pts=ANC-Decision-Logic.xlsx \
  -dtp="ANC.DT.01 Danger signs,ANC.DT.02 Check symptoms" \
  -op=path/to/output -e=json
```

---

### casereporting — Case Reporting Operations

#### casereporting transform-ersd
Transform a US eCR eRSD version 1 bundle to eRSD version 2.

```bash
java -jar tooling-cli.jar casereporting transform-ersd -- \
  -ptb=path/to/ersd-v1-bundle.json -op=path/to/output -e=json
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-pathtobundle` | `-ptb` | Path to the eRSD v1 bundle |
| `-outputpath` | `-op` | Output directory |
| `-pathtoplandefinition` | `-ptpd` | Optional replacement PlanDefinition |
| `-encoding` | `-e` | Output encoding (`json`, `xml`, or both) |

#### casereporting generate-package
Generate a TES package from an input Bundle.

```bash
java -jar tooling-cli.jar casereporting generate-package -- \
  -ptib=path/to/input-bundle.json -op=path/to/output -e=json \
  -v=1.0.0 -rl="Release Label"
```

| Argument | Alias | Description |
|----------|-------|-------------|
| `-pathtoinputbundle` | `-ptib` | Path to the source bundle |
| `-outputpath` | `-op` | Output directory |
| `-encoding` | `-e` | Output encoding |
| `-v` | | Version for generated artifacts |
| `-rl` | | Release label extension value |
| `-ptcgw` | | Path to condition grouper workbook |
| `-ptccvs` | | Path to RCKMS Condition Code ValueSet |
| `-wcg` | | Write condition grouper ValueSets to individual files |

---

### cql — CQL Generation Operations

#### cql from-drool
Generate CQL ELM libraries from Drool rule definitions.

```bash
java -jar tooling-cli.jar cql from-drool -- \
  -ifp=path/to/input.json -op=path/to/output -fv=4.0.0 -t=CONDITION
```

#### cql from-vmr
Transform vMR data to FHIR format.

```bash
java -jar tooling-cli.jar cql from-vmr -- \
  -ifp=path/to/vmr/directory -op=path/to/output.xml -e=xml
```

---

### convert — FHIR Version and Model Conversion

#### convert r5-to-r4
Convert R5 FHIR resources to R4 format.

```bash
java -jar tooling-cli.jar convert r5-to-r4 -- -ptd=path/to/r5/resources -op=path/to/output
```

#### convert qdm-to-qicore
Generate QDM to QiCore mapping HTML pages.

```bash
java -jar tooling-cli.jar convert qdm-to-qicore -- path/to/output
```

#### convert qicore-quick
Generate QiCore QUICK pages from QiCore output.

```bash
java -jar tooling-cli.jar convert qicore-quick -- path/to/qicore/output path/to/quick/output
```

---

### utility — Miscellaneous Operations

#### utility dateroller
Roll test data dates to a current date range.

```bash
java -jar tooling-cli.jar utility dateroller -- -ptd=path/to/test/data -v=r4
```

#### utility mat-extract
Extract resources and CQL from a MAT export Bundle.

```bash
java -jar tooling-cli.jar utility mat-extract -- path/to/mat-bundle.json -v=r4
```

| Argument | Description |
|----------|-------------|
| First positional arg | Path to the MAT export Bundle |
| `-v` | FHIR version: `stu3` or `r4` (default: `r4`) |
| `-dir` | Process input path as a directory of files |

#### utility modelinfo-generate
Generate ModelInfo from StructureDefinitions.

```bash
java -jar tooling-cli.jar utility modelinfo-generate -- -ip=path/to/structuredefs -op=path/to/output
```

#### utility strip-content
Strip generated narrative and other generated content from resources.

```bash
java -jar tooling-cli.jar utility strip-content -- -ptd=path/to/resources -v=r4
```

#### utility postman
Generate a Postman collection from measure transaction Bundles.

```bash
java -jar tooling-cli.jar utility postman -- \
  -ptbd=path/to/bundle/dir -op=path/to/output -v=r4 \
  -host=cqm-sandbox.alphora.com -path=cqf-ruler-r4/fhir/ \
  -protocol=https -name="My Collection"
```

#### utility profiles-to-spreadsheet
Export FHIR profiles to spreadsheet format.

```bash
java -jar tooling-cli.jar utility profiles-to-spreadsheet -- -ptd=path/to/profiles -op=path/to/output
```

#### utility qicore-elements
Export QICore elements to spreadsheet format.

```bash
java -jar tooling-cli.jar utility qicore-elements -- -ptd=path/to/qicore -op=path/to/output
```

---

## Getting Help

```bash
# Top-level help
java -jar tooling-cli.jar --help

# Help for a command group
java -jar tooling-cli.jar ig --help

# Help for a specific command
java -jar tooling-cli.jar ig refresh --help

# Version info
java -jar tooling-cli.jar --version
```
