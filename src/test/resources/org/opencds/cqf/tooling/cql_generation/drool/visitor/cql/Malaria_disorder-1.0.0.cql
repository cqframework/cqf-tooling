library Malaria_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Plasmodium species (Tests by Microscopic Observation in Blood)" : 'https://hln.com/fhir/ValueSet/MAL005'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Malaria (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/MAL001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Tests by Microscopic Observation in Blood Smear" : 'https://hln.com/fhir/ValueSet/LabTST017'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has lab result with test name (specific or an organism or substance)-46a33d3e5dd12828685e09ccbba07365":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Plasmodium species (Tests by Microscopic Observation in Blood)"
)

define "Lab Result Value (quantitative)-5165a9fb9ba4523d6327b4b6263b9558":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias > ToQuantity(0.0)0.0
)

define "Lab Result Value (ordinal)-a65e9197a8fba590196bc837dc9debac":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-6a5137d69fe2277be5cb8b09ac25bae6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Malaria (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-b4ef4721a85bd8932ae8d296b13aea5f":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Group 1.1-62b8a27d7b2f598e10cb2af35f3871d4":
   "Lab Result Interpretation-b4ef4721a85bd8932ae8d296b13aea5f"
   or ( "Lab Result Value (nominal)-6a5137d69fe2277be5cb8b09ac25bae6")
   or ( "Lab Result Value (ordinal)-a65e9197a8fba590196bc837dc9debac")
   or ( "Lab Result Value (quantitative)-5165a9fb9ba4523d6327b4b6263b9558")
   and ( "Patient has lab result with test name (specific or an organism or substance)-46a33d3e5dd12828685e09ccbba07365")

define "Group 1-db13b3173d3164fcdb8f69914c850c6c":
   "Group 1.1-62b8a27d7b2f598e10cb2af35f3871d4"

define "Patient has lab result with test name (NOT specific for an organism or substance)-507e33dd44efa5a2c674c3b76285dddb":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Tests by Microscopic Observation in Blood Smear"
)

define "Lab Result Value (nominal)-7636439c8fbfb0359265e185f515e816":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Malaria (Organism or Substance in Lab Results)"
)

define "ConditionCriteriaMet":
   "Lab Result Value (nominal)-7636439c8fbfb0359265e185f515e816"
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-507e33dd44efa5a2c674c3b76285dddb")
   or ( "Group 1-db13b3173d3164fcdb8f69914c850c6c")
