library GeneratedCql22 version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Tests for carbapenem susceptibility [Meropenem, imipenem, and doripenem]" : 'https://hln.com/fhir/ValueSet/CPCRE001'
valueset "Resistant Lab Result Value " : 'https://hln.com/fhir/ValueSet/LabRLT014'
valueset "Resistant Interpretation of an Observation " : 'https://hln.com/fhir/ValueSet/LabRLT013'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has lab result with test name (specific or an organism or substance)-51d3d1a62b9860ac6ad172f16545ab9a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Tests for carbapenem susceptibility [Meropenem, imipenem, and doripenem]"
)

define "Lab Result Value (quantitative)-534d522d70992da73b119ea3a585cf5e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias >= 4 'ug/mL'
)

define "Lab Result Value (ordinal)-6488a727e848f3fa45e5eaaefb0dfed1":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Resistant Lab Result Value "
)

define "Lab Result Interpretation-5c408141dab735d2a8a1d209bcb24d0d":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Resistant Interpretation of an Observation "
)

define "Group 1.1-cd3560bc4372d83c7b4ebf7022349497":
   "Patient has lab result with test name (specific or an organism or substance)-51d3d1a62b9860ac6ad172f16545ab9a"
   or ( "Lab Result Value (quantitative)-534d522d70992da73b119ea3a585cf5e")
   or ( "Lab Result Value (ordinal)-6488a727e848f3fa45e5eaaefb0dfed1")
   or ( "Lab Result Interpretation-5c408141dab735d2a8a1d209bcb24d0d")

define "Group 1-2924a78c774c5eae7658c80bc70b9021":
   "Group 1.1-cd3560bc4372d83c7b4ebf7022349497"

define "ConditionCriteriaMet":
   "Group 1-2924a78c774c5eae7658c80bc70b9021"
