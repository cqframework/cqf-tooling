# DAK Processor HOW-TO

## column 

### Data element

#### "data element id"
Internal Column name "DataElementID", retrieve by getDataElementID

used in: 
 - DictionaryElement createDataElement as id
 - addInputOptionToParentElement as optionId
 


#### "[anc] activity id" OR "activity id"
Internal Column name "ActivityID"

used in:
 - String getMasterDataType
 - DictionaryElement createDataElement
 - processDataElementPage

Expected structure {Code} {Display}

The code is used to bundle dataElement together in a StructureDefinition

example "EmCare.B2. Determine Type of Visit"
----
#### "core, fp, sti" OR "scope"
Internal Column name "Scope"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement

Must match with the scope provided when running the DAK to be taken into account (if scope provided in DAK)

 Expected structure {scope}

 Example "EmCare"
        
#### "context"
Internal Column name "Context"

used in:
 - processDataElementPage 
 - DictionaryElement createDataElement

 IF Encounter, it will create CQL definition in the library {Scope}Contact{DataElements}
        
#### "selector"
Internal Column name "Selector"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement

#### "in new dd"
Internal Column name "InNewDD
used in: 
 - processDataElementPage

will exclude row if not null not equal to 1 or 'ST'

### "infoIcon"
(cannot be use not in processDataElementPag)

used in: 
 - DictionaryElement createDataElement


### "relevance"

used in: 
 - DictionaryElement createDataElement

#### "due"
Internal Column name "Due"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement

#### "description and definition" OR "description"
Internal Column name "Description"

used in: 
 - processDataElementPage  
 - DictionaryElement createDataElement

#### "data element label"
Internal Column name "DataElementLabel"
Internal Column name "Name"
Internal Column name "Label"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement
 
#### "data element name"
Internal Column name "DataElementName"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement
#### "notes"
Internal Column name "Notes"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement
#### "data type"
Internal Column name "Type"

used in: 
 - processDataElementPage 

#### "multiple choice" OR "multiple choice type" OR "multiple choice (if applicable)" OR "multiple choice type ?(if applicable)"

Internal Column name "MultipleChoiceType"

Possible value for Dataelement: 'Input Option' or 'Data Element'(default)

'Input Option' will add an input option to the parent (line above)

'Data Element' will create a new data element

used in: 
 - processDataElementPage 
#### "input options"
Internal Column name "Choices"

used in: 
 - processDataElementPage 
#### "calculation"
Internal Column name "Calculation"

used in: 
 - processDataElementPage  
 - DictionaryElement createDataElement

#### "validation required"
Internal Column name "Constraint"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement
#### "required"
Internal Column name "Required"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement

#### "editable"
Internal Column name "Editable"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement
#### "custom profile id"
Internal Column name "CustomProfileId"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement via getFhirElementPath

#### "binding or custom value set name or reference"
Internal Column name "CustomValueSetName"

for "inputOption"
if empty, the parent name is used 
if doesn't end with "Choices" it will be added

for "Dataelement"
define the value set, if not set will use the DE label
getChoices() is used to get the choices

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement via getFhirElementPath

#### "binding strength"
Internal Column name "BindingStrength"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement via getFhirElementPath

#### "ucum"
Internal Column name "UnitOfMeasure"

used in: 
 - processDataElementPage  
 - DictionaryElement createDataElement via getFhirElementPath
#### "extension needed"
Internal Column name "ExtensionNeeded"

used in: 
 - processDataElementPage  
 - DictionaryElement createDataElement via getFhirElementPath
### fhir resource details

#### "master data element path"
Internal Column name "MasterDataElementPath"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement via getFhirElementPath

#### "hl7 fhir r4 - resource"
Internal Column name "FhirR4Resource"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement via getFhirElementPath

#### "hl7 fhir r4 - resource type"
Internal Column name "FhirR4ResourceType"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement via getFhirElementPath



#### "hl7 fhir r4 - base profile"
Internal Column name "FhirR4BaseProfile"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement via getFhirElementPath

#### "hl7 fhir r4 - version number"
Internal Column name "FhirR4VersionNumber"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement via getFhirElementPath

#### "hl7 fhir r4 - additional fhir mapping details"
Internal Column name "FhirR4AdditionalFHIRMappingDetails"

used in: 
 - processDataElementPage 
 - DictionaryElement createDataElement via getFhirElementPath


### terminology

#### "fhir code system"
Internal Column name "FhirCodeSystem"

used in: 
 - processDataElementPage 
#### "hl7 fhir r4 code"
Internal Column name "FhirR4Code"

used in: 
 - processDataElementPage 
#### "hl7 fhir r4 code display"
Internal Column name "FhirR4CodeDisplay"

used in: 
 - processDataElementPage 
#### "hl7 fhir r4 code definition"
Internal Column name "FhirR4CodeDefinition"

used in: 
 - processDataElementPage 
#### "icd-10-who" OR "icd-10 code"OR "icd-10?code"
Internal Column name "ICD-10"

used in: 
 - processDataElementPage 
#### "icd-10?comments / considerations"
Internal Column name "ICD-10Comments"

used in: 
 - processDataElementPage 
#### "icf?code"
Internal Column name "ICF"

used in: 
 - processDataElementPage 
#### "icf?comments / considerations"
Internal Column name "ICFComments"

used in: 
 - processDataElementPage 
#### "ichi?code" OR#### "ichi (beta 3)?code"
Internal Column name "ICHI"

used in: 
 - processDataElementPage 
#### "ichi?comments / considerations"
Internal Column name "ICHIComments"

used in: 
 - processDataElementPage 
#### "snomed-ct" OR "snomed-ct code" OR "snomed ct" OR "snomed ct?code" OR "snomed ct international version?code"
Internal Column name "SNOMED-CT"

used in: 
 - processDataElementPage 
#### "snomed ct international version?comments / considerations"
Internal Column name "SNOMEDComments"

used in: 
 - processDataElementPage 
#### "loinc" OR "loinc code" OR "loinc version 2.68?code"
Internal Column name "LOINC"

used in: 
 - processDataElementPage 
#### "loinc version 2.68?comments / considerations"
Internal Column name "LOINCComments"

used in: 
 - processDataElementPage 
#### "rxnorm" OR "rxnorm code" OR  "rxnorm?code"
Internal Column name "RxNorm"

used in: 
 - processDataElementPage 
#### "rxnorm?comments / considerations"
Internal Column name "RXNormComments"

used in: 
 - processDataElementPage 
#### "icd-11" OR "icd-11 code" OR "icd-11?code"
Internal Column name "ICD-11"

used in: 
 - processDataElementPage 
#### "icd-11?comments / considerations"
Internal Column name "ICD-11Comments"

used in: 
 - processDataElementPage 
#### "ciel"
Internal Column name "CIEL"

used in: 
 - processDataElementPage 
#### "openmrs entity parent"
Internal Column name "OpenMRSEntityParent"

used in: 
 - processDataElementPage 
#### "openmrs entity"
Internal Column name "OpenMRSEntity"

used in: 
 - processDataElementPage 
#### "openmrs entity id"
Internal Column name "OpenMRSEntityId"

used in: 
 - processDataElementPage 
#### "cpt" OR "cpt code" OR "cpt?code"
Internal Column name "CPT"

used in: 
 - processDataElementPage 
#### "cpt?comments / considerations"
Internal Column name "CPTComments"

used in: 
 - processDataElementPage 
#### "hcpcs" OR "hcpcs code" OR  "hcpcs?code" OR "hcpcs level ii code" OR "hcpcs?level ii code"
Internal Column name "HCPCS"

used in: 
 - processDataElementPage 
#### "hcpcs?comments / considerations"
Internal Column name "HCPCSComments"

used in: 
 - processDataElementPage 
#### "ndc" OR "ndc code" OR "ndc?code"
Internal Column name "NDC"

used in: 
 - processDataElementPage 
#### "ndc?comments / considerations"
Internal Column name "NDCComments"


used in: 
 - processDataElementPage 


## Artefact

### custom-XXX codingsystem
### required column
- Scope
- FHIR codeSystem
- FHIR Version

### Ativity
 - context


### Creating valueset

A value set creation should be separated from the mapped element but it can have the same ID, name ... 

#### required column for valueset
- Multiple choice type_ Input option
- Data Type: Coding
- FHIR Version
- BindingStrength : Extensible or example or other (but not empty)

#### required column for concept inside the coding systems
- FHIR ressourceType : ends with [x]
- FHIR Version
- Scope
- BindingStrength : Extensible or example or other (but not empty)


### Mapping
#### required column
- FHIR base profile
- HL7 FHIR R4 - Ressource
- HL7 FHIR R4 - RessourceType
- Extension needed
- Custom profile ID
- Binding : Valuset define in another line
Question