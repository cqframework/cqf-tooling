# READ ME DAK processor


## column 

### Data element

#### "data element id"
Internal Column name "DataElementID

#### "[anc] activity id" OR "activity id"
Internal Column name "ActivityID"

#### "core, fp, sti" OR "scope"
Internal Column name "Scope"
        
#### "context"
Internal Column name "Context"
        
#### "selector"
Internal Column name "Selector"
        
#### "in new dd"
Internal Column name "InNewDD
        


#### "due"
Internal Column name "Due"


#### "description and definition" OR#### "description"
Internal Column name "Description"

#### "data element label"
Internal Column name "DataElementLabel"
Internal Column name "Name"
Internal Column name "Label"
 
#### "data element name"
Internal Column name "DataElementName"

#### "notes"
Internal Column name "Notes"

#### "data type"
Internal Column name "Type"

#### "multiple choice" OR "multiple choice type" OR "multiple choice (if applicable)" OR "multiple choice type ?(if applicable)"

Internal Column name "MultipleChoiceType"

#### "input options"
Internal Column name "Choices"

#### "calculation"
Internal Column name "Calculation"

#### "validation required"
Internal Column name "Constraint"

#### "required"
Internal Column name "Required"

#### "editable"
Internal Column name "Editable"

#### "custom profile id"
Internal Column name "CustomProfileId"

#### "binding or custom value set name or reference"
Internal Column name "CustomValueSetName"

#### "binding strength"
Internal Column name "BindingStrength"

#### "ucum"
Internal Column name "UnitOfMeasure"

#### "extension needed"
Internal Column name "ExtensionNeeded"


### fhir resource details

#### "master data element path"
Internal Column name "MasterDataElementPath"

#### "hl7 fhir r4 - resource"
Internal Column name "FhirR4Resource"

#### "hl7 fhir r4 - resource type"
Internal Column name "FhirR4ResourceType"

#### "hl7 fhir r4 - base profile"
Internal Column name "FhirR4BaseProfile"

#### "hl7 fhir r4 - version number"
Internal Column name "FhirR4VersionNumber"

#### "hl7 fhir r4 - additional fhir mapping details"
Internal Column name "FhirR4AdditionalFHIRMappingDetails"


### terminology

#### "fhir code system"
Internal Column name "FhirCodeSystem"

#### "hl7 fhir r4 code"
Internal Column name "FhirR4Code"

#### "hl7 fhir r4 code display"
Internal Column name "FhirR4CodeDisplay"

#### "hl7 fhir r4 code definition"
Internal Column name "FhirR4CodeDefinition"

#### "icd-10-who" OR "icd-10 code"
#### "icd-10?code"
Internal Column name "ICD-10"

#### "icd-10?comments / considerations"
Internal Column name "ICD-10Comments"

#### "icf?code"
Internal Column name "ICF"

#### "icf?comments / considerations"
Internal Column name "ICFComments"

#### "ichi?code" OR#### "ichi (beta 3)?code"
Internal Column name "ICHI"

#### "ichi?comments / considerations"
Internal Column name "ICHIComments"

#### "snomed-ct" OR "snomed-ct code" OR "snomed ct" OR "snomed ct?code" OR "snomed ct international version?code"
Internal Column name "SNOMED-CT"

#### "snomed ct international version?comments / considerations"
Internal Column name "SNOMEDComments"

#### "loinc" OR "loinc code" OR "loinc version 2.68?code"
Internal Column name "LOINC"

#### "loinc version 2.68?comments / considerations"
Internal Column name "LOINCComments"

#### "rxnorm" OR "rxnorm code" OR  "rxnorm?code"
Internal Column name "RxNorm"

#### "rxnorm?comments / considerations"
Internal Column name "RXNormComments"

#### "icd-11" OR "icd-11 code"
#### "icd-11?code"
Internal Column name "ICD-11"

#### "icd-11?comments / considerations"
Internal Column name "ICD-11Comments"

#### "ciel"
Internal Column name "CIEL"

#### "openmrs entity parent"
Internal Column name "OpenMRSEntityParent"

#### "openmrs entity"
Internal Column name "OpenMRSEntity"

#### "openmrs entity id"
Internal Column name "OpenMRSEntityId"

#### "cpt" OR "cpt code" OR "cpt?code"
Internal Column name "CPT"

#### "cpt?comments / considerations"
Internal Column name "CPTComments"

#### "hcpcs" OR "hcpcs code" OR  "hcpcs?code" OR "hcpcs level ii code" OR "hcpcs?level ii code"
Internal Column name "HCPCS"

#### "hcpcs?comments / considerations"
Internal Column name "HCPCSComments"

#### "ndc" OR "ndc code" OR "ndc?code"
Internal Column name "NDC"

#### "ndc?comments / considerations"
Internal Column name "NDCComments"





# custom-XXX codingsystem
## required column
- Scope
- FHIR codeSystem
- FHIR Version

# Ativity
 - context


# Creating valueset

A value set creation should be separated from the mapped element but it can have the same ID, name ... 

## required column for valueset
- Multiple choice type_ Input option
- Data Type: Coding
- FHIR Version
- BindingStrength : Extensible or example or other (but not empty)

## required column for concept inside the coding systems
- FHIR ressourceType : ends with [x]
- FHIR Version
- Scope
- BindingStrength : Extensible or example or other (but not empty)


# Mapping
## required column
- FHIR base profile
- HL7 FHIR R4 - Ressource
- HL7 FHIR R4 - RessourceType
- Extension needed
- Custom profile ID
- Binding : Valuset define in another line
Question