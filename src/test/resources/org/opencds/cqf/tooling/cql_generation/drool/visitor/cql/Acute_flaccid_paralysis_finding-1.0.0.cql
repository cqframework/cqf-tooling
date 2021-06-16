library Acute_flaccid_paralysis_finding version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Limb Weakness" : 'https://hln.com/fhir/ValueSet/SYMP113'
valueset "Myelitis [Non Specific]" : 'https://hln.com/fhir/ValueSet/POL004'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has a diagnosis of-6f073b96f2867d72177c65ebb7db9bad":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Limb Weakness"
)

define "Patient has a problem of-4941abf86a7551feed605848adf0025d":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Limb Weakness"
)

define "Problem has status of-79bf4ed76a7d1b943c836ced5f91a711":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-e9b482b3671fd187181e8b1aa2d25e61":
   "Problem has status of-79bf4ed76a7d1b943c836ced5f91a711"
   and ( "Patient has a problem of-4941abf86a7551feed605848adf0025d")

define "Patient has a diagnosis of-eb235a105661481694506e4c1496edfa":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Myelitis [Non Specific]"
)

define "Patient has a problem of-13ce5c061f1f417317bf2a41471d0027":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Myelitis [Non Specific]"
)

define "Problem has status of-5dee8c8324d8d30c73bcfe3b80028148":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-4c9f3a53906a12dff65e81d594403649":
   "Problem has status of-5dee8c8324d8d30c73bcfe3b80028148"
   and ( "Patient has a problem of-13ce5c061f1f417317bf2a41471d0027")

define "ConditionCriteriaMet":
   "Group 1-4c9f3a53906a12dff65e81d594403649"
   or ( "Patient has a diagnosis of-eb235a105661481694506e4c1496edfa")
   and ( "Group 1-e9b482b3671fd187181e8b1aa2d25e61")
   or ( "Patient has a diagnosis of-6f073b96f2867d72177c65ebb7db9bad")
