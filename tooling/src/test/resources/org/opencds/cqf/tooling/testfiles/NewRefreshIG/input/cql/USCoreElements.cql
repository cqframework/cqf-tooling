library USCoreElements version '0.1.0'

using USCore version '3.1.1'

include fhir.cqf.common.FHIRHelpers version '4.0.1'
include USCoreCommon version '0.1.0' called UC

valueset "Common allergy substances": 'http://hl7.org/fhir/us/core/ValueSet/us-core-allergy-substance'

code "No known allergy (situation)": '716186003' from UC.SNOMEDCT

context Patient

define "Usual Name":
  First(
    Patient.name name
      where name.use ~ 'usual'
  )

define "Official Name":
  First(
    Patient.name name
      where name.use ~ 'official'
  )

define "First Occurrence of Non-Official, Non-Usual Name":
  First(
    Patient.name name
      where not(name.use ~ 'official') and not(name.use ~ 'usual')
  )

define "Name":
  Coalesce("Official Name", "Usual Name", "First Occurrence of Non-Official, Non-Usual Name")

// TODO: Correct this once USCore.HumanName.given works as expected
// define "First Name":
//   "Name".given[0]

// TODO: Correct this once USCore.HumanName.given works as expected
// define "Middle Names":
//   Combine(Skip("Name".given, 1), ' ')

define "Last Name":
  "Name".family

// TODO: Correct this once USCore.HumanName.given works as expected
define "Name - First Middle(s) Last":
  //Combine("Name".given, ' ') + ' ' + "Name".family
  "Name".family

// TODO: Correct this once USCore.HumanName.given works as expected
define "Name - Last, First Middle(s)":
  //"Name".family + ', ' + Combine("Name".given, ' ')
  "Name".family

// Allergy/Intolerance

/*
@description: Returns all allergies and intolerances
@comment: This definition returns all allergies and intolerances conforming to the US Core 3.1.1
[AllergyIntolerance](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-allergyintolerance.html) profile.
Allergies and intolerances returned by this definition include records with any clinical status (including none)
and any verification status (including none).
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-allergyintolerance
*/
define "All Allergies and Intolerances":
  ["AllergyIntolerance"]

define "Active Confirmed Allergies and Intolerances":
  "All Allergies and Intolerances".allergyActive().allergyConfirmed()

define "Common Allergies and Intolerances":
  "All Allergies and Intolerances" A
    where A.code in "Common allergy substances"

define "Active Confirmed Common Allergies and Intolerances":
  "Common Allergies and Intolerances".allergyActive().allergyConfirmed()

// No Known Allergies (Not Asked)
define "No Known Allergies (Not Asked)":
  "All Allergies and Intolerances" A
    where A.code ~ "No known allergy (situation)"
      and A.isAllergyActive()
      and A.isAllergyUnconfirmed()

// No Known Allergies (Confirmed)
define "No Known Allergies (Confirmed)":
  "All Allergies and Intolerances" A
    where A.code ~ "No known allergy (situation)"
      and A.isAllergyActive()
      and A.isAllergyConfirmed()

// Condition

/*
@description: Returns all problem list items, encounter diagnoses, and health concerns
@comment: This definition returns all conditions of any category conforming to the US Core 3.1.1
[Condition](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-condition.html) profile.
Conditions returned by this definition include records with any clinical status (including none) and
any verification status (including none).
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition
*/
define "All Conditions":
  ["Condition"]

define "All Problem List Items":
  "All Conditions" C
    where C.isProblemListItem()

define "Active Confirmed Problem List Items":
  "All Problem List Items".active().confirmed()

define "All Encounter Diagnoses":
  "All Conditions" C
    where C.isEncounterDiagnosis()

define "All Health Concerns":
  "All Conditions" C
    where C.isHealthConcern()

// Laboratory Diagnostic Report

/*
@comment: [Diagnostic Report for Laboratory Results](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-diagnosticreport-lab.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-diagnosticreport-lab
*/
define "All Diagnostic Reports for Laboratory Results":
  ["DiagnosticReportProfileLaboratoryReporting"]

// Diagnostic Report

/*
@comment: [Diagnostic Report Note](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-diagnosticreport-note.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-diagnosticreport-note
*/
define "All Diagnostic Report Notes":
  ["DiagnosticReportProfileNoteExchange"]

// Encounter

/*
@comment: [Encounter](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-encounter.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-encounter 
*/
define "All Encounters":
  ["EncounterProfile"]

define "All Performed Encounters":
  "All Encounters" E
    where E.status = 'finished'  

// Immunization

/*
@comment: [Immunization](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-immunization.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-immunization
*/
define "All Immunizations":
  ["ImmunizationProfile"] 

define "All Completed Immunizations":
  "All Immunizations" I
    where I.status = 'completed'

// Implantable Device

/*
@comment: [Implantable Device](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-implantable-device.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-implantable-device
*/
define "All Implantable Devices":
  ["ImplantableDeviceProfile"]

// Laboratory Result

/*
@description: Returns all laboratory results
@comment: [Laboratory Result](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-observation-lab.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab
*/
define "All Laboratory Results":
  ["LaboratoryResultObservationProfile"]

define "Resulted Laboratory Results":
  "All Laboratory Results" L
    where L.status in { 'preliminary', 'final', 'amended', 'corrected' }

// Medication Request

/*
@description: Returns all medication requests
@comment: [Medication Request](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-medicationrequest.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-medicationrequest
*/
define "Most Recent Medication Request":
  UC.MostRecentMR("All Medication Requests")

define "All Medication Requests":
  ["MedicationRequestProfile"]

define "Active Medication Orders":
  "All Medication Requests" M
    where M.status = 'active'
      and M.intent in { 'order', 'original-order', 'reflex-order', 'filler-order', 'instance-order' }

define "All Medications":
  ["MedicationProfile"]

// Pediatric BMI for Age

/*
@comment: [Pediatric BMI for Age](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-pediatric-bmi-for-age.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/pediatric-bmi-for-age
*/
define "Pediatric BMI for Age":
  ["PediatricBMIforAgeObservationProfile"]

// Pediatric Head Circumference Percentile

/*
@comment: [Pediatric Head Circumference Percentile](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-head-occipital-frontal-circumference-percentile.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/head-occipital-frontal-circumference-percentile
*/
//define "All Pediatric Head Circumference Percentile Measurements":
  //TODO - profile not in modelinfo
  //UsCorePediatricHeadOccipitalFrontalCircumferencePercentileProfile


// Pediatric Weight for Height

/*
@comment: [Pediatric Weight for Height](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-pediatric-weight-for-height.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/pediatric-weight-for-height
*/
define "Pediatric Weight for Height":
  ["PediatricWeightForHeightObservationProfile"]

// Procedure

/*
@comment: [Procedure](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-procedure.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-procedure
*/
define "All Procedures":
  ["ProcedureProfile"] P
    where P.status in { 'preparation', 'in-progress', 'on-hold', 'completed' }

define "All Performed Procedures":
  "All Procedures" P
    where P.status = 'completed'

// Pulse Oximetry

/*
@comment: [Pulse Oximetry](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-pulse-oximetry.html)
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-pulse-oximetry
*/
define "Pulse Oximetry":
  ["PulseOximetryProfile"]

// Smoking Status

/*
@description: Returns all smoking status observations
@comment: This definition returns all smoking status observations conforming to the US Core 3.1.1
[Smoking Status](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-smokingstatus.html)
profile.
@profile: http://hl7.org/fhir/us/core/StructureDefinition/us-core-smokingstatus
*/
define "Smoking Status":
  ["SmokingStatusProfile"]

/*
@description: Returns the most recent smoking status
@comment: This definition returns the most recent (by issued time) smoking status observation conforming to the
US Core 3.1.1 [Smoking Status](https://hl7.org/fhir/us/core/STU3.1.1/StructureDefinition-us-core-smokingstatus.html)
profile.
*/
define "Most Recent Smoking Status":
  Last(
    "Smoking Status" SS
        where SS.status = 'final'
        sort by issued
  )

// Vital Signs Panel

/*
@comment: [Vital Signs Panel](http://hl7.org/fhir/R4/vitalspanel.html)
@profile: http://hl7.org/fhir/StructureDefinition/vitalspanel
*/
define "All Vital Signs Panels":
  ["observation-vitalspanel"]

// Respiratory Rate

/*
@comment: [Respiratory Rate](http://hl7.org/fhir/R4/resprate.html)
@profile: http://hl7.org/fhir/StructureDefinition/resprate
*/
define "All Respiratory Rate Measurements":
  ["observation-resprate"]

// Heart Rate

/*
@comment: [Heart Rate](http://hl7.org/fhir/R4/heartrate.html)
@profile:http://hl7.org/fhir/StructureDefinition/heartrate
*/
define "All Heart Rate Measurements":
  ["observation-heartrate"]

// Oxygen Saturation

/*
@comment: [Oxygen Saturation](http://hl7.org/fhir/R4/oxygensat.html)
@profile: http://hl7.org/fhir/StructureDefinition/oxygensat
*/
define "All Oxygen Saturation Measurements":
  ["observation-oxygensat"]

// Body Temperature

/*
@comment: [Body Temperature](http://hl7.org/fhir/R4/bodytemp.html)
@profile: http://hl7.org/fhir/StructureDefinition/bodytemp
*/
define "All Body Temperature Measurements":
  ["observation-bodytemp"]

// Body Height

/*
@comment: [Body Height](http://hl7.org/fhir/R4/bodyheight.html)
@profile: http://hl7.org/fhir/StructureDefinition/bodyheight
*/
define "All Body Height Measurements":
  ["observation-bodyheight"]

// Head Circumference

/*
@comment: [Head Circumference](http://hl7.org/fhir/R4/headcircum.html)
@profile: http://hl7.org/fhir/StructureDefinition/headcircum
*/
define "All Head Circumference Measurements":
  ["observation-headcircum"]

// Body Weight

/*
@comment: [Body Weight](http://hl7.org/fhir/R4/bodyweight.html)
@profile: http://hl7.org/fhir/StructureDefinition/bodyweight
*/
define "All Body Weight Measurements":
  ["observation-bodyweight"]

// Body Mass Index

/*
@comment: [Body Mass Index](http://hl7.org/fhir/R4/bmi.html)
@profile: http://hl7.org/fhir/StructureDefinition/bmi
*/
define "All Body Mass Index Measurements":
  ["observation-bmi"]

// Blood Pressure

/*
@comment: [Blood Pressure](http://hl7.org/fhir/R4/bp.html)
@profile: http://hl7.org/fhir/StructureDefinition/bp
*/
define "All Blood Pressure Measurements":
  ["observation-bp"]

// Systolic Blood Pressure
define "Systolic Blood Pressure":
  "All Blood Pressure Measurements".SystolicBP.value
  
// Diastolic Blood Pressure
define "Diastolic Blood Pressure":
  "All Blood Pressure Measurements".DiastolicBP.value


