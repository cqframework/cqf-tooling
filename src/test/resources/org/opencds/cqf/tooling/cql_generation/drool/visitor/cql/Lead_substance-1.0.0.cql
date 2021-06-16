library Lead_substance version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Elevated Blood Lead Level (Tests for Lead in Blood)" : 'https://hln.com/fhir/ValueSet/BLL003'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Elevated Blood Lead Level (Disorders)" : 'https://hln.com/fhir/ValueSet/BLL001'
valueset "Elevated Blood Lead Level (Tests for Lead [Quantitative] in Capillary Blood" : 'https://hln.com/fhir/ValueSet/BLL005'
valueset "Toxic Effects of Lead (Disorders)" : 'https://hln.com/fhir/ValueSet/BLL002'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-e9d91db9fba2e842a6b81da80d6c0f36":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Elevated Blood Lead Level (Tests for Lead in Blood)"
)

define "Lab Result Value (ordinal)-b1509847738185a587bc703820dce4fb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-be796496b45c5db56f0f0139bc16e07c":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has a diagnosis of-503dd85b899726d8e4201b664b5ca1b2":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Elevated Blood Lead Level (Disorders)"
)

define "Patient has a problem of-f6befabcce39668b18d566dbf07676bd":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Elevated Blood Lead Level (Disorders)"
)

define "Problem has status of-73818df6e9346310e9ccd9dd1f52960a":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-b8d43c982241f9784a93fb5180259174":
   "Problem has status of-73818df6e9346310e9ccd9dd1f52960a"
   and ( "Patient has a problem of-f6befabcce39668b18d566dbf07676bd")

define "Units:-fffffd05d80909d1f5a0df3fd007c4e4":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) < 16 'years'

define "Patient has lab result with test name (specific or an organism or substance)-59e5b372920d2cdc3333c72227aff174":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Elevated Blood Lead Level (Tests for Lead [Quantitative] in Capillary Blood"
)

define "Lab Result Value (quantitative)-f6577fccec9cf93fb57076f1fec682d3":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias >= 5 'ug/dL'
)

define "Lab Result Value (quantitative)-59cb15a33c9a5c9784471751be650924":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias >= 0.24 'umol/L'
)

define "Lab Result Value (ordinal)-b358c64d51ac0e2f4d5c77b2c8d5964e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-5428564c11e7b89e75ec6c7e28c93d43":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Group 1.1-b18ddda846a1e31e3cbfd2aaebc3b728":
   "Lab Result Interpretation-5428564c11e7b89e75ec6c7e28c93d43"
   or ( "Lab Result Value (ordinal)-b358c64d51ac0e2f4d5c77b2c8d5964e")
   or ( "Lab Result Value (quantitative)-59cb15a33c9a5c9784471751be650924")
   or ( "Lab Result Value (quantitative)-f6577fccec9cf93fb57076f1fec682d3")
   and ( "Patient has lab result with test name (specific or an organism or substance)-59e5b372920d2cdc3333c72227aff174")

define "Group 1-8f8e910c886a1da46f3554f9917a2e15":
   "Group 1.1-b18ddda846a1e31e3cbfd2aaebc3b728"
   and ( "Units:-fffffd05d80909d1f5a0df3fd007c4e4")

define "Patient has a diagnosis of-96acc1d1f097555b87bdd48ed06cf475":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Toxic Effects of Lead (Disorders)"
)

define "Patient has a problem of-1987741c62f32b4939ec3bd957dfd447":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Toxic Effects of Lead (Disorders)"
)

define "Problem has status of-9f381de14e4434bdd31164cd8b634ade":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-2b39562763fa32863b03106d661dd6bb":
   "Problem has status of-9f381de14e4434bdd31164cd8b634ade"
   and ( "Patient has a problem of-1987741c62f32b4939ec3bd957dfd447")

define "ConditionCriteriaMet":
   "Group 1-2b39562763fa32863b03106d661dd6bb"
   or ( "Patient has a diagnosis of-96acc1d1f097555b87bdd48ed06cf475")
   or ( "Group 1-8f8e910c886a1da46f3554f9917a2e15")
   and ( "Group 1-b8d43c982241f9784a93fb5180259174")
   or ( "Patient has a diagnosis of-503dd85b899726d8e4201b664b5ca1b2")
   or ( "Lab Result Interpretation-be796496b45c5db56f0f0139bc16e07c")
   or ( "Lab Result Value (ordinal)-b1509847738185a587bc703820dce4fb")
   and ( "Patient has lab result with test name (specific for an organism or substance)-e9d91db9fba2e842a6b81da80d6c0f36")
