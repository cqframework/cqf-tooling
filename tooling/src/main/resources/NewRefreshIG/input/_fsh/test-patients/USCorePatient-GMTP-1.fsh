Instance: USCorePatient-GMTP-1
InstanceOf: Patient
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"
//* meta.profile[+] = "http://hl7.org/fhir/us/qicore/StructureDefinition/qicore-patient"

/** extension[+]
  * insert USCoreRace
  * insert USCoreRaceEthnicityCategory(#2106-3, "White")
  * insert USCoreRaceEthnicityCategory(#1002-5, "American Indian or Alaska Native")
  * insert USCoreRaceEthnicityCategory(#2028-9, "Asian")
  * insert USCoreRaceEthnicityDetailed(#1586-7, "Shoshone")
  * insert USCoreRaceEthnicityDetailed(#2036-2, "Filipino")
  * insert USCoreRaceEthnicityDetailed(#1735-0, "Alaska Native")
  * insert USCoreRaceEthnicityText("Mixed")

* extension[+]
  * insert USCoreEthnicity
  * insert USCoreRaceEthnicityCategory(#2135-2, "Hispanic or Latino")
  * insert USCoreRaceEthnicityDetailed(#2184-0, "Dominican")
  * insert USCoreRaceEthnicityDetailed(#2148-5, "Mexican")
  * insert USCoreRaceEthnicityText("Hispanic or Latino")

* insert USCoreBirthSexExtension(#M)
*/
* identifier
  * use = #usual
  * type = $v2-0203#MR "Medical Record Number"
  * system = "urn:oid:1.2.36.146.595.217.0.1"
  * value = "12345678"
  * period.start = "2002-05-06"
  * assigner.display = "Acme Healthcare"
  
* active = true

* name
  * family = "Smith" 
  * given = "Emilia"  
  * use = #official

* telecom[+]
  * system = #phone
  * value = "+1 (555) 1234567" 
  * use = #mobile

* gender = #female 

* birthDate = "1990-05-15" 

* deceasedBoolean = false 

* address
  * use = #home
  * type = #both
  * text = "123 Maple Street, Cityville, State, 12345" 
  * line = "123 Maple Street"
  * city = "Cityville"
  * state = "MA"
  * postalCode = "12345"
  * period.start = "1990-05-15" 


Instance: service-request-USCorePatient-GMTP-1
InstanceOf: ServiceRequest
Usage: #example
* status = #active
* intent = #order
* subject = Reference(Patient/USCorePatient-GMTP-1)
* code.coding[0] = $sct#405825005 "Molecular genetic test (procedure)"
* code.coding[+] = $icd#Z13.89 " Encounter for screening for other disorder"
* code.coding[+] = $cpt#81479 "Unlisted molecular pathology procedure"
* reasonReference = Reference(Condition/related-Condition-GMTP-1)
* subject = Reference(Patient/USCorePatient-GMTP-1)
* occurrenceDateTime = "2023-12-15T19:32:52-05:00"
* requester = Reference(Practitioner/requesting-provider-USCorePatient-GMTP-1)
* performer = Reference(Organization/servicing-provider-GMTP-1)
* authoredOn = "2023-11-29T19:32:52-05:00"
* insurance = Reference(Coverage/coverage-GMTP-1)

Instance: service-request-2-USCorePatient-GMTP-1
InstanceOf: ServiceRequest
Usage: #example
* status = #active
* intent = #order
* subject = Reference(Patient/USCorePatient-GMTP-1)
* code.coding[0] = $sct#405825005 "Molecular genetic test (procedure)"
* code.coding[+] = $icd#Z13.89 " Encounter for screening for other disorder"
* code.coding[+] = $cpt#81479 "Unlisted molecular pathology procedure"
* reasonReference = Reference(Condition/related-Condition-GMTP-1)
* subject = Reference(Patient/USCorePatient-GMTP-1)
* occurrenceDateTime = "2023-12-10T19:32:52-05:00"
* requester = Reference(Practitioner/requesting-provider-USCorePatient-GMTP-1)
* performer = Reference(Organization/servicing-provider-GMTP-1)
* authoredOn = "2023-11-10T19:32:52-05:00"
* insurance = Reference(Coverage/coverage-GMTP-1)

  
Instance: requesting-provider-USCorePatient-GMTP-1
InstanceOf: Practitioner
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner"
* identifier[0]
  * system = "http://hl7.org/fhir/sid/us-npi"
  * value = "1144221867"
//* identifier[+]
  //* system = "www.acme.org/practioner"
 // * value = "53432"
* name[+]
  * family = "Smart"
  * given = "Braden"
  * suffix = "PharmD"

* telecom[+]
  * system = #phone
  * value = "345-416-5672"
  * use = #mobile
* telecom[+]
  * system = #fax
  * value = "345-416-5672"
* address[+]
  * line = "8914 115TH ST"
  * city = "RICHMOND HIL"
  * state = "NY"
  * postalCode = "123456"
  * country = "USA"

Instance: servicing-provider-GMTP-1
InstanceOf: Organization
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-organization"
* identifier[0]
  * system = "http://hl7.org/fhir/sid/us-npi"
  * value = "1564221867"
* active = true
* name = "Healthcare Clinic"
* telecom[+]
  * system = #phone
  * value = "+1 (987) 6543210"
  * use = #mobile
* telecom[+]
  * system = #fax
  * value = "+1 (987) 6543210"
* address[+]
  * line = "789 Elm Street"
  * city = "Metropolis"
  * state = "NY"
  * postalCode = "67890"
  * country = "USA"

Instance: billing-provider-GMTP-1
InstanceOf: Organization
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-organization"
* identifier[0]
  * system = "http://hl7.org/fhir/sid/us-npi"
  * value = "1144221849"
* active = true
* name = "Oak Street Billing Provider"
* telecom[+]
  * system = #phone
  * value = "+1 (123) 4567890"
  * use = #mobile
* telecom[+]
  * system = #fax
  * value = "+1 (123) 4567890"
* address[+]
  * line = "456 Oak Street"
  * city = "Anytown"
  * state = "MA"
  * postalCode = "54321"
  * country = "USA"

Instance: coverage-GMTP-1
InstanceOf: Coverage
Usage: #example
* identifier.type = $v2-0203#MB
* identifier.value = "member-id-from-identifier-slice-USCorePatient-GMTP-1"
* status = #active 
* policyHolder = Reference(Patient/USCorePatient-GMTP-1) 
* subscriber = Reference(Patient/USCorePatient-GMTP-1)
* subscriberId = "subscriber-id-USCorePatient-GMTP-1"
* beneficiary = Reference(Patient/USCorePatient-GMTP-1) 
* payor = Reference(Organization/billing-provider-GMTP-1) 



Instance: research-subject-GMTP-1
InstanceOf: ResearchSubject
Usage: #example
* individual = Reference(Patient/USCorePatient-GMTP-1)
* study = Reference(ResearchStudy/research-study-GMTP-1)
* status = #on-study 

Instance: research-study-GMTP-1
InstanceOf: ResearchStudy
Usage: #example
* identifier[0]
  * system = "https://clinicaltrials.gov"
  * value = "NCT02326129"
* title = "Clinical Study on Treatment X"
* status = #active 
* period
  * start = "2023-11-01" 
  * end = "2025-11-30"
* sponsor = Reference(Organization/billing-provider-GMTP-1)
* condition = $icd#E11.618 "Type 2 diabetes mellitus with other diabetic arthropathy"

Instance: related-Condition-GMTP-1
InstanceOf: Condition
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition"
* clinicalStatus = $condition-clinical#active
* verificationStatus = $condition-ver-status#confirmed
* category = $condition-category#encounter-diagnosis "Encounter Diagnosis"
* code = $icd#E11.618 "Type 2 diabetes mellitus with other diabetic arthropathy"
* subject = Reference(Patient/USCorePatient-GMTP-1)
* onsetDateTime = "2012-05-24T00:00:00+00:00"
* recordedDate = "2012-05-24T00:00:00+00:00"


