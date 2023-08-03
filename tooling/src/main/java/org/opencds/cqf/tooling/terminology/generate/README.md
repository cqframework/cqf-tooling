# ValueSet Generation Tooling

This operation is designed to generate FHIR ValueSet resources from a JSON configuration file. There is built in 
support for the RxNORM and LOINC code systems using APIs. Other code systems like CPT, SNOMEDCT, etc are resolved 
using a specified FHIR server $expand operation. 

## Configuration

```json
{
  "pathToIgResource": "path to the xml encoded FHIR ImplementationGuide resource within the IG",
  "author": {
    "name": "name of the author or organization responsible for creating the set of ValueSet resources",
    "contactType": "email | phone | url | other",
    "contactValue": "the contact details"
  },
  "codesystems": [
    {
      "name": "name of the code system",
      "url": "canonical code system url",
      "version": "the code system version"
    }
  ],
  "valuesets": [
    {
      "id": "the value set unique id (string) - maps to ValueSet.id",
      "canonical": "the value set canonical url - maps to ValueSet.url",
      "name": "the value set name (computer friendly) - maps to ValueSet.name",
      "title": "the value set title (human friendly) - maps to ValueSet.title",
      "description": "the value set description including why the value set was built, comments about misuse, instructions for clinical use and interpretation, literature references, examples from the paper world, etc - maps to ValueSet.description",
      "purpose": "an explanation of why this value set is needed and why it has been designed as it has - maps to ValueSet.purpose",
      "clinicalFocus": "describes the clinical focus for the value set - maps to ValueSet.extension(http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-clinical-focus)",
      "dataElementScope": "describes the data element scope (i.e. Condition, Medication, etc) for the value set - maps to ValueSet.extension(http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-dataelement-scope)",
      "inclusionCriteria": "describes the inclusion criteria scope for the value set - maps to ValueSet.extension(http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-inclusion-criteria)",
      "exclusionCriteria": "describes the exclusion criteria scope for the value set - maps to ValueSet.extension(http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-exclusion-criteria)",
      "knowledgeCapability": [
        "defines a knowledge capability afforded by this value set: shareable | computable | publishable | executable - maps to ValueSet.extension(http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability)"
      ],
      "knowledgeRepresentationLevel": [
        "defines a knowledge representation level provided by this value set: narrative | semi-structured | structured | executable - maps to ValueSet.extension(http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeRepresentationLevel)"
      ],
      "rulesText": {
        "narrative": "an expression that provides an alternative definition of the content of the value set in some form that is not computable - e.g instructions that could only be followed by a human - maps to ValueSet.extension(http://hl7.org/fhir/StructureDefinition/valueset-rules-text)",
        "workflowXml": "the RxMix workflow library content",
        "input": [
          "the input values needed to run the workflow"
        ],
        "includeFilter": [
          "the string values to include in the result - values that don't match will be excluded"
        ],
        "excludeFilter": [
          "the string values to exclude from the result - values that don't match will be included"
        ],
        "excludeRule": {
        }
      },
      "hierarchy": {
        "query": "an expression that provides an alternative definition of the content of the value set in some form that is not computable - e.g instructions that could only be followed by a human - not currently mapped to ValueSet",
        "parents": [
          "parents of descendants to include - maps to ValueSet.compose.include.filter where property = parent"
        ],
        "excludeParents": [
          "parents of descendants to exclude - maps to ValueSet.compose.exclude.filter where property = parent"
        ],
        "property": [
          {
            "name": "name of the property - maps to ValueSet.compose.include.filter.property",
            "value": "value of the property - maps to ValueSet.compose.include.filter.value"
          }
        ],
        "auth": {
          "user": "username for LOINC account",
          "password": "password for LOINC account"
        }
      },
      "expand": {
        "pathToValueSet": "path to the ValueSet resource file",
        "txServer": {
          "baseUrl": "base url for the fhir terminology server",
          "auth": {
            "user": "username for fhir the terminology server",
            "password": "password for the fhir terminology server"
          },
          "validateFSN": true,
          "apiKey": "the apikey to access the VSAC api for resolving the fully specified name (FSN), required if validateFSN is true"
        }
      }
    }
  ]
}
```