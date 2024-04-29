Instance: USCorePatient-3
InstanceOf: USCorePatientProfile
Usage: #example
//* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"
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
  * value = "1234567"
  * period.start = "2002-05-06"
  * assigner.display = "Acme Healthcare"
  
* active = true

/* name[+]
  * insert USCoreName(#official, "Elizabeth", "Jennifer")
  //* given[+] = "James"
*/
* name[+]
  * insert USCoreName(#usual, "Chalmers", "Jim")

/* name[+]
  * insert USCoreName(#maiden, "Windsor", "Peter")
  * given[+] = "James"
  * period.end = "2002"
*/
* telecom[+]
  * insert USCorePhone(#work, "(02\) 5555 6473")
// * rank = 1
  
/* telecom[+]
  * insert USCorePhone(#mobile, "(03\) 3410 5613")
// * rank = 2
  
* telecom[+]
  * insert USCorePhone(#old, "(03\) 5555 8834")
  * period.end = "2014"
*/
* gender = #male

* birthDate = "1976-12-25"
  * insert USCoreBirthTimeExtension("1976-12-25T14:35:45-05:00")
  
* deceasedBoolean = false

* address
  * use = #home
  * type = #both
  * text = "536 Erewhon St PeasantVille, Utah 84416"
  * line = "536 Erewhon St"
  * city = "PleasantVille"
  * district = "Rainbow"
  * state = "UT"
  * postalCode = "84416"
  * period.start = "1977-12-25"
  
* maritalStatus = $v3-MaritalStatus#M
/* contact
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
 */ 
// * managingOrganization = Reference(Organization/example)

Instance: USCorePatient-3-height
InstanceOf: Observation
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/StructureDefinition/bodyheight"
* valueQuantity = 69.3 'in' "in" // Avg height according to Diasabled World
* code = $loinc#8302-2 "Body height"
* status = #final
* subject = Reference(Patient/USCorePatient-3)
* effectiveDateTime = "2023-02-28T00:00:00-00:00"
//* category = $observation-category#vital-signs "Vital Signs"

Instance: USCorePatient-3-weight
InstanceOf: Observation
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/StructureDefinition/bodyweight"
* valueQuantity = 180.3 'lb' "lb"
* code = $loinc#29463-7 "Body Weight"
* status = #final
* subject = Reference(Patient/USCorePatient-3)
* effectiveDateTime = "2023-02-28T00:00:00-00:00"


/*Instance: blood-pressure
InstanceOf: Observation
Usage: #example
* meta.profile[0] = "http://hl7.org/fhir/StructureDefinition/bp"
* meta.profile[+] = "http://hl7.org/fhir/StructureDefinition/vitalsigns"
* status = #final
* category = $observation-category#vital-signs "Vital Signs"
* category.text = "Vital Signs"
* code = $loinc#85354-9 "Blood pressure panel with all children optional"
* code.text = "Blood pressure systolic and diastolic"
* subject = Reference(Patient/USCorePatient-3) "Amy Shaw"
* encounter.display = "GP Visit"
* effectiveDateTime = "1999-07-02"
* component[0].code = $loinc#8480-6 "Systolic blood pressure"
* component[=].code.text = "Systolic blood pressure"
* component[=].valueQuantity = 109 'mm[Hg]' "mmHg"
* component[+].code = $loinc#8462-4 "Diastolic blood pressure"
* component[=].code.text = "Diastolic blood pressure"
* component[=].valueQuantity = 44 'mm[Hg]' "mmHg"
*/

Instance: encounter-outpatient-USCorePatient-3
InstanceOf: Encounter
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-encounter"
* status = #finished
* class = $v3-ActCode#AMB "ambulatory"
* type = $cpt#99201
* subject = Reference(Patient/USCorePatient-3)

* participant
  * type[+] = $v3-Participant#DIS "discharger"
  * individual = Reference(Practitioner/order-provider-USCorePatient-3)
  
Instance: order-provider-USCorePatient-3
InstanceOf: Practitioner
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-practioner"
* identifier[0]
  * system = "http://hl7.org.fhir/sid/us-npi"
  * value = "1821424433"
//* identifier[+]
  //* system = "www.acme.org/practioner"
 // * value = "53432"
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

Instance: condition-t2dm
InstanceOf: Condition
Usage: #example
* meta.profile[+] = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition"
* clinicalStatus = $condition-clinical#active
* verificationStatus = $condition-ver-status#confirmed
* category = $condition-category#encounter-diagnosis "Encounter Diagnosis"
* code = $icd#E11.618 "Type 2 diabetes mellitus with other diabetic arthropathy"
* subject = Reference(Patient/USCorePatient-3)
* encounter = Reference(Encounter/encounter-outpatient-USCorePatient-3)
* onsetDateTime = "2012-05-24T00:00:00+00:00"
* recordedDate = "2012-05-24T00:00:00+00:00"

/*Instance: uscore-med2
InstanceOf: USCoreMedicationProfile
Usage: #example
* code = $rxnorm#92880 "Humulin N"
* code.text = "Humulin N"
*/

Instance: MedRequest-insulin
InstanceOf: MedicationRequest
Usage: #example
* status = #active
* intent = #order
* medicationCodeableConcept
  * coding[+]
    * system = "http://www.nlm.nih.gov/research/umls/rxnorm"
    * code = $rxnorm#647241 "metformin Extended Release Oral Tablet [Glumetza]"
    * display = "metformin Extended Release Oral Tablet [Glumetza]"
  * text = "metformin Extended Release Oral Tablet [Glumetza]"
* subject = Reference(Patient/USCorePatient-3)
* authoredOn = "2016-03-25T19:32:52-05:00"
* requester = Reference(Practitioner/order-provider-USCorePatient-3)
* dosageInstruction.text = "qhs"
  /* timing[+]
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
*/