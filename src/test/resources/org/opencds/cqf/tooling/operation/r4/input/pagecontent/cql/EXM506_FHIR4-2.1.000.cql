library EXM506_FHIR4 version '2.1.000'

using FHIR version '4.0.0'

include MATGlobalCommonFunctions_FHIR4 version '4.0.000' called Global
include FHIRHelpers version '4.0.0' called FHIRHelpers
include SupplementalDataElements_FHIR4 version '1.0.0' called SDE

codesystem "Diagnosis Role": 'http://terminology.hl7.org/CodeSystem/diagnosis-role'
codesystem "LOINC": 'http://loinc.org'
codesystem "SNOMEDCT": 'http://snomed.org'
codesystem "RequestIntent": 'http://terminology.hl7.org/CodeSystem/request-intent'
codesystem "MedicationRequestCategory": 'http://terminology.hl7.org/CodeSystem/medicationrequest-category'

valueset "All Primary and Secondary Cancer":  'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1111.161'
valueset "Encounter Inpatient": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.666.5.307'
valueset "Ethnicity": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.837'
valueset "Hospice Care": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.3157.1004.20'
valueset "ONC Administrative Sex": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1'
valueset "Palliative Care": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1111.162'
valueset "Payer": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.3591'
valueset "Race": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.836'
valueset "Schedule II and Schedule III Opioids":  'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1125.2'
valueset "Schedule IV Benzodiazepines": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1125.1'

code "Birthdate": '21112-8' from "LOINC" display 'Birth date'
code "Order": 'order' from "RequestIntent" display 'Order'
code "Plan": 'plan' from "RequestIntent" display 'Plan'

parameter "Measurement Period" Interval<DateTime>

context Patient

define "SDE Ethnicity":
  SDE."Ethnicity"

define "SDE Payer":
  SDE."Payer"

define "SDE Race":
  SDE."Race"

define "SDE Sex":
  SDE."ONC Administrative Sex"

define "Inpatient Encounter with Age Greater than or Equal to 18":
  Global."Inpatient Encounter" EncounterInpatient
     with ["Patient"] BirthDate
       such that Global.CalendarAgeInYearsAt(FHIRHelpers.ToDate(Patient.birthDate), start of FHIRHelpers.ToInterval(EncounterInpatient.period)) >= 18

define "Opioid at Discharge":
  ["MedicationRequest": "Schedule II and Schedule III Opioids"] OpioidsDischarge
     where exists (OpioidsDischarge.category C where FHIRHelpers.ToConcept(C) in {Global."Community", Global."Discharge"})
         and OpioidsDischarge.status in {'active', 'completed'}
         and OpioidsDischarge.intent in {'order', 'plan'}

define "Benzodiazepine at Discharge":
  ["MedicationRequest": "Schedule IV Benzodiazepines"] BenzodiazepineDischarge
     where exists (BenzodiazepineDischarge.category C where FHIRHelpers.ToConcept(C) in {Global."Community", Global."Discharge"})
         and BenzodiazepineDischarge.status in {'active', 'completed'}
         and BenzodiazepineDischarge.intent in {'order', 'plan'}

define "Encounter with an Opioid or Benzodiazepine at Discharge":
  "Inpatient Encounter with Age Greater than or Equal to 18" InpatientEncounter
    with ("Opioid at Discharge" union "Benzodiazepine at Discharge") OpioidBenzodiazepine
      such that FHIRHelpers.ToDateTime(OpioidBenzodiazepine.authoredOn) during (InpatientEncounter.period)

define "Initial Population":
"Encounter with an Opioid or Benzodiazepine at Discharge"

define "Denominator":
"Initial Population"

define "Denominator Exclusion":
 ("Inpatient Encounter with Age Greater than or Equal to 18" InpatientEncounter
 with [Condition: "All Primary and Secondary Cancer"]Cancer
      such that Global."Normalize Interval"(Cancer.onset) overlaps InpatientEncounter.period)

       union ("Inpatient Encounter with Age Greater than or Equal to 18" InpatientEncounter
     with (["ServiceRequest": "Palliative Care"]
     union ["ServiceRequest": "Hospice Care"] ) PalliativeOrHospiceCareOrder
     such that  PalliativeOrHospiceCareOrder.intent = 'order'
     and FHIRHelpers.ToDateTime(PalliativeOrHospiceCareOrder.authoredOn) during InpatientEncounter.period)

   union ("Inpatient Encounter with Age Greater than or Equal to 18" InpatientEncounter
 with  (["Procedure": "Palliative Care"]
     union ["Procedure": "Hospice Care"] ) PalliativeOrHospiceCarePerformed
             such that Global."Normalize Interval"(PalliativeOrHospiceCarePerformed.performed)
              overlaps InpatientEncounter.period)

define "Encounter with a Concurrent Opioid and Benzodiazepine at Discharge":
"Inpatient Encounter with Age Greater than or Equal to 18" InpatientEncounter
   with ("Opioid at Discharge") OpioidDischarge
   such that FHIRHelpers.ToDateTime(OpioidDischarge.authoredOn) during (InpatientEncounter.period)
   with ("Benzodiazepine at Discharge") BenzodiazepineDischarge
   such that FHIRHelpers.ToDateTime(BenzodiazepineDischarge.authoredOn) during (InpatientEncounter.period)

define "Encounter with Two or More Concurrent Opioids at Discharge":
	"Inpatient Encounter with Age Greater than or Equal to 18" InpatientEncounter
		where Count(
			[MedicationRequest: "Schedule II and Schedule III Opioids"] Opioids
				where FHIRHelpers.ToDateTime (Opioids.authoredOn) during (InpatientEncounter.period)
		) >= 2

define "Numerator":
 "Encounter with Two or More Concurrent Opioids at Discharge"
 union "Encounter with a Concurrent Opioid and Benzodiazepine at Discharge"
