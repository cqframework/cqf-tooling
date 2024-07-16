# ValueSet Generation Tooling

This operation is designed to generate FHIR ValueSet resources from a JSON configuration file. There is built-in
support for the RxNORM and LOINC code systems using existing APIs. Other code systems like CPT, SNOMEDCT, etc are 
resolved using a specified FHIR server $expand operation.

This operation uses the LoincHierarchy, RxMixWorkflow, and FhirTxExpansion operations in concert with the supplied 
configuration file to generate the ValueSet resource. Additionally, the ResolveTerminologyFSN operation may be needed 
to provide fully specified names for the codes included within the returned ValueSet resources. Note that the 
ResolveTerminologyFSN is an expensive operation and should only be used when necessary.

Note that this operation is a prototype. Any constructive feedback would be greatly appreciated.

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
      "version": "the code system version - NOTE: these versions will be used for rulesText (RxNorm) and hierarchy (LOINC) value set generation, but no validation will be performed - use with caution. By default, the latest version will be used for the rulesText (RxNorm) and hierarchy (LOINC) code systems"
    }
  ],
  "valuesets": [
    {
      "id": "the value set unique id (string) - maps to ValueSet.id - required",
      "canonical": "the value set canonical url - maps to ValueSet.url - required",
      "version": "business version of the value set - maps to ValueSet.version",
      "name": "the value set name (computer friendly) - maps to ValueSet.name",
      "title": "the value set title (human friendly) - maps to ValueSet.title",
      "status": "the status (depicting the lifecycle) of the value set: draft | active | retired | unknown (default is draft) - maps to ValueSet.status",
      "experimental": true,
      "date": "the date when the value set was created or revised (default value is today) - maps to ValueSet.date",
      "publisher": "name of the organization or individual responsible for creating the value set - maps to ValueSet.publisher",
      "description": "the value set description including why the value set was built, comments about misuse, instructions for clinical use and interpretation, literature references, examples from the paper world, etc - maps to ValueSet.description",
      "jurisdiction": [
        {
          "coding": {
            "code": "legal or geographic region code in which this value set is intended to be used - maps to ValueSet.jurisdiction.coding.code",
            "system": "legal or geographic region code system in which this value set is intended to be used - maps to ValueSet.jurisdiction.coding.system",
            "display": "a representation of the meaning of the code in the system - maps to ValueSet.jurisdiction.coding.display"
          },
          "text": "a human language representation of the egal or geographic region in which this value set is intended to be used - maps to ValueSet.jurisdiction.text"
        }
      ],
      "purpose": "an explanation of why this value set is needed and why it has been designed as it has - maps to ValueSet.purpose",
      "copyright": "copyright statement relating to the value set and/or its contents - maps to ValueSet.copyright",
      "profiles": [
        "Canonical profile URL - maps to ValueSet.meta.profile"
      ],
      "clinicalFocus": "describes the clinical focus for the value set - maps to ValueSet.extension(http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-clinical-focus)",
      "dataElementScope": "describes the data element scope (i.e. Condition, Medication, etc) for the value set - maps to ValueSet.extension(http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-dataelement-scope)",
      "inclusionCriteria": "describes the inclusion criteria scope for the value set - maps to ValueSet.extension(http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-inclusion-criteria)",
      "exclusionCriteria": "describes the exclusion criteria scope for the value set - maps to ValueSet.extension(http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-exclusion-criteria)",
      "usageWarning": "an extra warning about the correct use of the value set - maps to ValueSet.extension(http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-usageWarning)",
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
          // same as rulesText
        }
      },
      "hierarchy": {
        "query": "an expression that provides an alternative definition of the content of the value set in some form that is not computable - e.g instructions that could only be followed by a human - not currently mapped to ValueSet",
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
          }
        }
      }
    }
  ]
}
```

## Arguments:
- -pathtoconfig | -ptc (required) - The path to the JSON configuration file.
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
    - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting FHIR ValueSet resources { json, xml }.
    - Default encoding: json
- -outputpath | -op (optional) - The directory path to which the resulting FHIR ValueSet resources should be written.
    - Default output path: src/main/resources/org/opencds/cqf/tooling/terminology/output