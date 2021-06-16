library GeneratedCql5 version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Tests for Vancomycin Susceptibility [OrdQn MIC]" : 'https://hln.com/fhir/ValueSet/VSA007'
valueset "Resistant Lab Result Value " : 'https://hln.com/fhir/ValueSet/LabRLT014'
valueset "Resistant Interpretation of an Observation " : 'https://hln.com/fhir/ValueSet/LabRLT013'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has lab result with test name (specific or an organism or substance)-ae487b6e3bcf5fdfe106d9829f2c8f8f":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Tests for Vancomycin Susceptibility [OrdQn MIC]"
)

define "Lab Result Value (quantitative)-d56a3377a32a47d6532b909825042c93":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias >= 16 'ug/mL'
)

define "Lab Result Value (ordinal)-67d1ed7b3d07987ef4d80bcea84e39a2":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Resistant Lab Result Value "
)

define "Lab Result Interpretation-f337072359f77b2779f6981b8d506069":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Resistant Interpretation of an Observation "
)

define "Group 1.1-2be496e46e5b9e65205c1208167afc9e":
   "Patient has lab result with test name (specific or an organism or substance)-ae487b6e3bcf5fdfe106d9829f2c8f8f"
   or ( "Lab Result Value (quantitative)-d56a3377a32a47d6532b909825042c93")
   or ( "Lab Result Value (ordinal)-67d1ed7b3d07987ef4d80bcea84e39a2")
   or ( "Lab Result Interpretation-f337072359f77b2779f6981b8d506069")

define "Group 1-72d7b74fcecddb37e79e4034760f2364":
   "Group 1.1-2be496e46e5b9e65205c1208167afc9e"

define "ConditionCriteriaMet":
   "Group 1-72d7b74fcecddb37e79e4034760f2364"
