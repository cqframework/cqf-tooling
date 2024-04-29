// Patient
Alias: $v2-0203 = http://terminology.hl7.org/CodeSystem/v2-0203
Alias: $v3-MaritalStatus = http://terminology.hl7.org/CodeSystem/v3-MaritalStatus
Alias: $v2-0131 = http://terminology.hl7.org/CodeSystem/v2-0131

// Observation
Alias: $observation-category = http://terminology.hl7.org/CodeSystem/observation-category
Alias: $loinc = http://loinc.org

// Encounter
Alias: $v3-ActCode = http://terminology.hl7.org/CodeSystems/v3-ActCode
Alias: $v3-Participant = http://terminology.hl7.org/CodeSystem/v3-ParticipationType
Alias: $cpt = http://www.ama-assn.org/go/cpt

// Condition
Alias: $condition-clinical = http://terminology.hl7.org/CodeSystem/condition-clinical
Alias: $condition-ver-status = http://terminology.hl7.org/CodeSystem/condition-ver-status
Alias: $condition-category = http://terminology.hl7.org/CodeSystem/condition-category
Alias: $sct = http://snomed.info/sct
Alias: $icd = http://hl7.org/fhir/sid/icd-10-cm

// Medication
Alias: $rxnorm = http://www.nlm.nih.gov/research/umls/rxnorm
Alias: $dose-rate-type = http://terminology.hl7.org/CodeSystem/dose-rate-type

RuleSet: USCoreBirthSexExtension(valueCode)
* extension[+]
  * insert USCoreBirthSex
  * insert USCoreBirthSexCode({valueCode})

RuleSet: USCoreBirthSex
* url = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-birthsex"

RuleSet: USCoreBirthSexCode(valueCode)
* valueCode = {valueCode}
  
RuleSet: USCoreBirthTimeExtension(valueDateTime)
* extension
  * insert USCoreBirthTime
  * insert USCoreBirthTimeValue({valueDateTime})

RuleSet: USCoreBirthTime
* url = "http://hl7.org/fhir/StructureDefinition/patient-birthTime"

RuleSet: USCoreBirthTimeValue(valueDateTime)
* valueDateTime = {valueDateTime}

RuleSet: USCoreRace
* url = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race"

RuleSet: USCoreEthnicity
* url = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity"

RuleSet: USCoreRaceEthnicityCategory(code, display)
* extension[+]
  * url = "ombCategory"
  * valueCoding = urn:oid:2.16.840.1.113883.6.238{code} {display}
  
RuleSet: USCoreRaceEthnicityDetailed(code, display)
* extension[+]
  * url = "detailed"
  * valueCoding = urn:oid:2.16.840.1.113883.6.238{code} {display}
  
RuleSet: USCoreRaceEthnicityText(description)
* extension[+]
  * url = "text"
  * valueString = {description}
  
RuleSet: USCorePhone(code, value)
* system = #phone
* value = {value}
* use = {code}

RuleSet: USCoreName(use, last, first)
* use = {use}
* family = {last}
* given[+] = {first}