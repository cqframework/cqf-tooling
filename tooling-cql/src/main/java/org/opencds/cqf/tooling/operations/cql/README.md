# CQL Operations

The operations defined in this package provide support for generating CQL content from various source formats.

## GenerateCQLFromDrool Operation

This operation generates CQL (Clinical Quality Language) from Drool-based encoded logic exports. The generated CQL
is mapped to a specified FHIR model version.

### Arguments:
- -inputPath | -ip (required) - Path to the encoded logic export file required for CQL generation
- -outputPath | -op (required) - Path to desired CQL generation output
- -encoding | -e (optional) - Input encoding
  - Default encoding: json
- -fhirVersion | -fv (optional) - FHIR Model Version to map ELM to
  - Default version: 4.0.0
- -type | -t (optional) - ELM granularity option { CONDITION, CONDITIONREL }
  - Default: CONDITION

## VmrToFhir Operation

This operation converts VMR (Virtual Medical Record) data to FHIR format.

### Arguments:
- -inputPath | -ip (required) - Path to the VMR data file
- -outputPath | -op (required) - Path to FHIR data output
- -encoding | -e (optional) - Input encoding
  - Default encoding: xml
- -fhirVersion | -fv (optional) - FHIR Model Version
  - Default version: 4.0.0
