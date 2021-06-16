library Coronavirus_infection_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "COVID-19 (Tests for SARS-CoV-2 Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/COV003'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "COVID-19 (Disorders)" : 'https://hln.com/fhir/ValueSet/COV001'
valueset "Cough" : 'https://hln.com/fhir/ValueSet/SYMP002'
valueset "COVID-19 (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/COV004'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "COVID-19 (Test Panels for SARS-CoV-2 Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/COV002'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-5e2867f9b801aa554b7e3ad2504c0efb":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "COVID-19 (Tests for SARS-CoV-2 Nucleic Acid)"
)

define "Lab Result Value (ordinal)-c95b1385837ce90f3a8ab1ade7802243":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-6259b1d95f8e5deb3b6e5bee1430c488":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has a diagnosis of-aaf19c7f8d43a3b2fecd0007444167f2":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Cough"
)

define "Patient has a problem of-b2c555f6a8f81a4142471b0c570e5600":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Cough"
)

define "Problem has status of-7817a32f1d3867fd19f13e6131e9d73f":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-fe86eefffcb8c9238d87f16fa66269bb":
   "Problem has status of-7817a32f1d3867fd19f13e6131e9d73f"
   and ( "Patient has a problem of-b2c555f6a8f81a4142471b0c570e5600")

define "Patient has lab result with test name (specific for an organism or substance)-ba89a32cb6bea5a588fc5d76948b9cc8":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "COVID-19 (Tests for SARS-CoV-2 Nucleic Acid)"
)

define "Lab Result Value (ordinal)-d157658eddec513caba216d36bef7726":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-14fab33078c2747d8a2da01bc343a71d":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "COVID-19 (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-e020f0d0ba2c13ff541d8e9789678813":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-bb85d6abfd08eb57c2dce0464839dbb8":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-9cadfef4dbe9cae76ac8196017204fc9":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "COVID-19 (Organism or Substance in Lab Results)"
)

define "Lab Result Value (ordinal)-245ab3376ced0ffb33cdd33c4de7e0e6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-a398a325449468ad065c2ba4275a0665":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "COVID-19 (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-d02e80dc3d1a7709ca8f1c2ab7475394":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has a lab test order with test name-ad5ce106e01fc4aa8aa86653d7caf375":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "COVID-19 (Tests for SARS-CoV-2 Nucleic Acid)"
)

define "Patient has a lab test order with test name-596a59dd4bd5571063a1efeca8685d7a":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "COVID-19 (Test Panels for SARS-CoV-2 Nucleic Acid)"
)

define "ConditionCriteriaMet":
   "Patient has a lab test order with test name-596a59dd4bd5571063a1efeca8685d7a"
   or ( "Patient has a lab test order with test name-ad5ce106e01fc4aa8aa86653d7caf375")
   or ( "Lab Result Interpretation-d02e80dc3d1a7709ca8f1c2ab7475394")
   or ( "Lab Result Value (nominal)-a398a325449468ad065c2ba4275a0665")
   or ( "Lab Result Value (ordinal)-245ab3376ced0ffb33cdd33c4de7e0e6")
   or ( "Lab Result Value (nominal)-9cadfef4dbe9cae76ac8196017204fc9")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-bb85d6abfd08eb57c2dce0464839dbb8")
   or ( "Lab Result Interpretation-e020f0d0ba2c13ff541d8e9789678813")
   or ( "Lab Result Value (nominal)-14fab33078c2747d8a2da01bc343a71d")
   or ( "Lab Result Value (ordinal)-d157658eddec513caba216d36bef7726")
   and ( "Patient has lab result with test name (specific for an organism or substance)-ba89a32cb6bea5a588fc5d76948b9cc8")
   and ( "Group 1-fe86eefffcb8c9238d87f16fa66269bb")
   or ( "Patient has a diagnosis of-aaf19c7f8d43a3b2fecd0007444167f2")
   or ( "Lab Result Interpretation-6259b1d95f8e5deb3b6e5bee1430c488")
   or ( "Lab Result Value (ordinal)-c95b1385837ce90f3a8ab1ade7802243")
   and ( "Patient has lab result with test name (specific for an organism or substance)-5e2867f9b801aa554b7e3ad2504c0efb")
