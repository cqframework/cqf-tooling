library MBODAInitialExpressions version '0.1.0'

using USCore version '3.1.1'
using FHIR version '4.0.1'

include fhir.cqf.common.FHIRHelpers version '4.0.1'
include USCoreCommon version '0.1.0' called UC
include USCoreElements version '0.1.0' called UCE

//CumulativeMedicationDuration was created as part of the US ECQM and CDC Opioid Guideline development 
//http://fhir.org/guides/cdc/opioid-mme-r4
include CumulativeMedicationDuration version '3.1.000' called CMD

codesystem "LOINC": 'http://loinc.org'
codesystem "Identifier Type": 'http://terminology.hl7.org/CodeSystem/v2-0203'
code "Body surface area": '8277-6' from "LOINC" display 'Intensive care unit'
code "Member Number": 'MB' from "Identifier Type"

context Patient

define "Member ID":
  singleton from
    (
      (
        (
          ([FHIR.Coverage] C 
              where EndsWith(C.beneficiary.reference, Patient.id))
          .identifier) I
        return {value: I.value.value, type: I.type.coding.code}
      ) valuesByTypes
    where "Member Number".code in valuesByTypes.type
    return valuesByTypes.value
    )


define "Patient Name":
  UCE."Name - First Middle(s) Last"

define "Last Name":
  UCE."Last Name"

// TODO: Error: Could not resolve data provider for package 'java.util'.
// define "First Name":
//   Patient.name.given.first()

define "Date of Birth":
  Patient.birthDate
  
define "Medical Record Number":
  singleton from 
  (
    (Patient.identifier I
      return {value: I.value, type: I.type.codes.code}
    ) valueByTypes
    // TODO: create code for MR (medical record number)
    where UC."MedicalRecordNumber".code in valueByTypes.type
    return valueByTypes.value
  )

define "Phone Number":
  UC.Mobile(Patient.telecom).value

define "Allergies":
  UCE."Active Confirmed Allergies and Intolerances" A
    return A.code

// TODO: Error: Could not resolve data provider for package 'java.util'.
// define "Address":
//   (singleton from Patient.address).line
define "Address":
  { 'TODO: remove hard-coded address' }

define "City":
  Patient.address.city

define "State":
  Patient.address.state

define "Zip":
  Patient.address.postalCode

define "Height":
  convert(
    UC.MostRecent(
      UCE."All Body Height Measurements".resulted()
    ).value 
  ) to '[in_i]'

define "Height in [in_i]":
  "Height".value

define "Weight":
  convert(
    UC.MostRecent(
      UCE."All Body Weight Measurements".resulted()
    ).value 
  ) to '[lb_av]'

define "Weight in [lb_av]":
  "Weight".value

define "Most Recent BSA":
  convert( 
    UC.MostRecent(
      ([FHIR.Observation: "Body surface area"] observation
        where observation.status ~ UC."observation-final".code
          or observation.status ~ UC."observation-amended".code
          or observation.status ~ UC."observation-corrected".code
      )
    ).value 
  ) to 'm2'

define "BSA":
  Coalesce("Most Recent BSA", "Calculated BSA - Mosteller")

define "BSA in m2":
  "BSA".value

// Mosteller formula using lbs and inches
define "Calculated BSA - Mosteller":
  CalculateBSA('Mosteller', "Height", "Weight")
  //((("Weight"*"Height")/3131).value)^0.5

define "Height in cm":
  convert("Height") to 'cm'

define "Weight in kg":
  convert("Weight") to 'kg'

define "Calculated BSA - DuBois and DuBois":
  CalculateBSA('DuBois and DuBois', "Height", "Weight")

define function CalculateBSA(alg System.String, height System.Quantity, weight System.Quantity):
  System.Quantity { value: 
    // Mosteller formula using lbs and inches
    if (alg = 'Mosteller') then
      ((((convert(height) to '[in_i]') * (convert(weight) to '[lb_av]'))/3131).value)^0.5
    
    // DuBois and DuBois formula using cm and kg
    // NOTE: never to be used for newborn calculation
    else if (alg = 'DuBois and DuBois') then
      0.007184 * ((convert(height) to 'cm').value^0.725) * ((convert(weight) to 'kg').value^0.425)
    
    // No matching algorithm found
    else null,
    unit: 'm2'
  }

// QUESTION: do we want to use Patient.gender as a backup? If so we'll need a ConceptMap or translation
define "Birth Sex":
 Patient.birthsex

define "Diagnosis Codes":
  UCE."All Conditions" C
    return C.code

define "Diagnosis Descriptions":
  "Diagnosis Codes" ConceptItem
    return Combine(((ConceptItem.codes) C return C.display), '|')

define "Medication Requested":
  UCE."All Medications" M
    where EndsWith(("Most Recent Medication Request".medication as USCore.Reference).reference, M.id)

define "Medication Name":
  "Medication Requested" M
    return M.code.display

define "Code of Requested Drug":
  "Medication Requested" M
    return M.code

define "Most Recent Medication Request":
  UCE."Most Recent Medication Request"

define "Medication Request References":
  ("Most Recent Medication Request".medication as USCore.Reference).reference

define "Most Recent Medication Request dosageInstruction":
  // TODO: should this really be a singleton?
  singleton from "Most Recent Medication Request".dosageInstruction

define "Most Recent Medication Request dosageInstruction.doseAndRate":
  // TODO: should this really be a singleton?
  singleton from "Most Recent Medication Request dosageInstruction".doseAndRate

define "Medication Dose":
  "Most Recent Medication Request dosageInstruction.doseAndRate".dose

define "Medication Route":
  "Most Recent Medication Request dosageInstruction".route

define "Medication Frequency value":
  "Most Recent Medication Request dosageInstruction".timing.repeat.frequency

define "Medication Frequency Period":
  if (IsNull("Most Recent Medication Request dosageInstruction".timing.repeat.period)) then 
    null
  else
    System.Quantity {
      value: "Most Recent Medication Request dosageInstruction".timing.repeat.period,
      unit: "Most Recent Medication Request dosageInstruction".timing.repeat.periodUnit
    }

define "Medication Frequency": // '1x per 1d', '3x per 2wk', '1x per 1mo'
  ToString("Medication Frequency value") + 'x' +
  ' per ' + ToString("Medication Frequency Period".value) + 
  ' ' + "Medication Frequency Period".unit

define "Quantity or Number of requested Visits":
  "Most Recent Medication Request".dispenseRequest.quantity

define "Prior Prescription":
  "Most Recent Medication Request".priorPrescription

define "New therapy":
  IsNull("Prior Prescription")

define "New therapy code":
  if ("New therapy") then
    System.Code { code: 'NewMedication', display: 'New Medication' }
  else
    System.Code { code: 'ContinuedTherapy', display: 'Continuation of therapy' }

// MISSING: needs test data to validate
//Initial date of therapy does not return correct result if there have been more than 1 prior Prescriptions - logic needs to be adapted to that case
define "Initial date of therapy":
  if not "New therapy" then
    UCE."All Medication Requests" M
      where EndsWith(("Most Recent Medication Request".priorPrescription as USCore.Reference).reference, M.id)
      return M.authoredOn
  else 
    null

define "Medication Request Period":
  CMD."MedicationRequestPeriod"("Most Recent Medication Request")

define "Expected Therapy Length":
  convert(
    CMD.Quantity(days between start of "Medication Request Period" and end of "Medication Request Period", 'd')
  ) to 'd'

define "Anticipated/actual date of service":
  start of "Medication Request Period"

/*
//related Procedures - Procedure is not linked to medication request - not possible to find the related procedures
*/