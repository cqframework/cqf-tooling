library Chancroid_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Chancroid (Disorders)" : 'https://hln.com/fhir/ValueSet/CHA002'
valueset "Chancroid (Tests for Haemophilus ducreyi by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/CHA003'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Chancroid (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/CHA001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-18de8299f72781b3ed3f8b607d76e7f7":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Chancroid (Disorders)"
)

define "Patient has a problem of-d4d756774dde5697ffbb59d4d3416818":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Chancroid (Disorders)"
)

define "Problem has status of-d4bf9775cf5f2125c9c96b931b19c381":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-31147cbb1e44db3fc730fd6ed92ebd82":
   "Problem has status of-d4bf9775cf5f2125c9c96b931b19c381"
   and ( "Patient has a problem of-d4d756774dde5697ffbb59d4d3416818")

define "Patient has lab result with test name (specific for an organism or substance)-c77cb7bb369b9a69240a11436d627eb7":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chancroid (Tests for Haemophilus ducreyi by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-a6423c449420ace2bc0bf2ceac2dfb1f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-168073d338cdba9ebb48ac3124a01ce4":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chancroid (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-a44046966f08eb381f8cb250504589e6":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-1ae5bfb16bf09b666d1de4c194fc2ffe":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-58493b068c1d744f71820caf6fcbaf0b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chancroid (Organism or Substance in Lab Results)"
)

define "ConditionCriteriaMet":
   "Lab Result Value (nominal)-58493b068c1d744f71820caf6fcbaf0b"
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-1ae5bfb16bf09b666d1de4c194fc2ffe")
   or ( "Lab Result Interpretation-a44046966f08eb381f8cb250504589e6")
   or ( "Lab Result Value (nominal)-168073d338cdba9ebb48ac3124a01ce4")
   or ( "Lab Result Value (ordinal)-a6423c449420ace2bc0bf2ceac2dfb1f")
   and ( "Patient has lab result with test name (specific for an organism or substance)-c77cb7bb369b9a69240a11436d627eb7")
   and ( "Group 1-31147cbb1e44db3fc730fd6ed92ebd82")
   or ( "Patient has a diagnosis of-18de8299f72781b3ed3f8b607d76e7f7")
