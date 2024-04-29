Instance: example
InstanceOf: USCorePatientProfile
Usage: #example
//* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"

* extension[+]
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

* identifier
  * use = #usual
  * type = $v2-0203#MR
  * system = "urn:oid:1.2.36.146.595.217.0.1"
  * value = "12345"
  * period.start = "2001-05-06"
  * assigner.display = "Acme Healthcare"
  
* active = true

* name[+]
  * insert USCoreName(#official, "Chalmers", "Peter")
  * given[+] = "James"

* name[+]
  * insert USCoreName(#usual, "Chalmers", "Jim")

* name[+]
  * insert USCoreName(#maiden, "Windsor", "Peter")
  * given[+] = "James"
  * period.end = "2002"

* telecom[+]
  * insert USCorePhone(#work, "(03\) 5555 6473")
// * rank = 1
  
* telecom[+]
  * insert USCorePhone(#mobile, "(03\) 3410 5613")
// * rank = 2
  
* telecom[+]
  * insert USCorePhone(#old, "(03\) 5555 8834")
  * period.end = "2014"

* gender = #male

* birthDate = "1974-12-25"
  * insert USCoreBirthTimeExtension("1974-12-25T14:35:45-05:00")
  
* deceasedBoolean = false

* address
  * use = #home
  * type = #both
  * text = "534 Erewhon St PeasantVille, Utah 84414"
  * line = "534 Erewhon St"
  * city = "PleasantVille"
  * district = "Rainbow"
  * state = "UT"
  * postalCode = "84414"
  * period.start = "1974-12-25"
  
* maritalStatus = $v3-MaritalStatus#M
* contact
  * relationship = $v2-0131#N
  * name
    * family = "du Marché"
      * extension
        * url = "http://hl7.org/fhir/StructureDefinition/humanname-own-prefix"
        * valueString = "VV"
    * given = "Bénédicte"
  * telecom[+]
    * system = #phone
    * value = "+33 (237) 998327"
  * address
    * use = #home
    * type = #both
    * line = "534 Erewhon St"
    * city = "PleasantVille"
    * district = "Rainbow"
    * state = "VT"
    * postalCode = "3999"
    * period.start = "1974-12-25"
  * gender = #female
  * period.start = "2012"
  
// * managingOrganization = Reference(Organization/example)


Instance: example-height
InstanceOf: Observation
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/StructureDefinition/bodyheight"
* valueQuantity = 175.4 'cm' "cm" // Avg height according to Diasabled World
* code = $loinc#8302-2 "Body height"
* status = #final
* subject = Reference(Patient/example)
* effectiveDateTime = "2023-02-23"
//* category = $observation-category#vital-signs "Vital Signs"

Instance: example-weight
InstanceOf: Observation
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/StructureDefinition/bodyweight"
* valueQuantity = 80 'kg' "kg"
* code = $loinc#29463-7 "Body Weight"
* status = #final
* subject = Reference(Patient/example)
* effectiveDateTime = "2023-02-23"


Instance: encounter-outpatient-example
InstanceOf: Encounter
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-encounter"
* status = #finished
* class = $v3-ActCode#AMB "ambulatory"
* type = $cpt#99201
* subject = Reference(Patient/example)

* participant
  * type[+] = $v3-Participant#DIS "discharger"
  * individual = Reference(Practitioner/order-provider-example)
  
Instance: order-provider-example
InstanceOf: Practitioner
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-practioner"
* identifier[+]
  * system = "http://hl7.org.fhir/sid/us-npi"
  * value = "1821424433"
* name[+]
  * family = "AGGARWAL"
  * given = "VAIBHAV"
  * suffix = "PharmD"

* telecom[+]
  * system = #phone
  * value = "718-416-5672"

* address[+]
  * line = "8914 115TH ST"
  * city = "RICHMOND HIL"
  * state = "NY"
  * postalCode = "114183135"
  * country = "US"
  

Instance: condition-gdm
InstanceOf: Condition
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition"
* clinicalStatus = $condition-clinical#active
* verificationStatus = $condition-ver-status#confirmed
* category = $condition-category#encounter-diagnosis "Encounter Diagnosis"
* code = $icd#O24.414 "Gestational diabetes mellitus in pregnancy, insulin controlled"
* subject = Reference(Patient/example)
* encounter = Reference(Encounter/encounter-outpatient-example)
* onsetDateTime = "2012-05-24T00:00:00+00:00"
* recordedDate = "2012-05-24T00:00:00+00:00"


Instance: uscore-med1
InstanceOf: USCoreMedicationProfile
Usage: #example
* code = $rxnorm#206765 "Lisinopril 10 MG Oral Tablet [Prinivil]"
* code.text = "lisinopril oral 10 mg"


Instance: MedRequest-example
InstanceOf: USCoreMedicationRequestProfile
Usage: #example
* status = #active
* intent = #order
* medicationReference = Reference(Medication/example)
* subject = Reference(Patient/example)
* authoredOn = "2015-03-25T19:32:52-05:00"
* requester = Reference(Practitioner/example)

* dosageInstruction[+]
  * timing[+]
    * repeat[+]
      * frequency = 3
      * period = 1
      * periodUnit = #d
  * site = $sct#447964005
  * route = $sct#394899003 "oral administration of treatment"
  * doseAndRate[+]
    * type = $dose-rate-type#ordered "Ordered"
    * doseQuantity = 5 'ml' "ml"
* dispenseRequest.quantity = 100 'ml' "ml"