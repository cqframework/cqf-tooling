library GeneratedCql16 version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Acute Respiratory Distress Syndrome (ARDS)" : 'https://hln.com/fhir/ValueSet/SYMP057'
valueset "SARS (Tests for SARS_CoV by Unspecified Method)" : 'https://hln.com/fhir/ValueSet/SARS006'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "SARS (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/SARS001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "SARS (Tests for SARS_CoV Antibody)" : 'https://hln.com/fhir/ValueSet/SARS005'
valueset "SARS (Tests for SARS_CoV Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/SARS004'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Fever" : 'https://hln.com/fhir/ValueSet/SYMP007'
valueset "SARS (Tests for SARS_CoV by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/SARS003'
valueset "Viruses (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST002'
valueset "SARS (Disorders)" : 'https://hln.com/fhir/ValueSet/SARS002'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-3a440f46d58456ef64671514734600e7":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Acute Respiratory Distress Syndrome (ARDS)"
)

define "Patient has a problem of-72b22da0bf194361e40e1bf43e7a48f0":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Acute Respiratory Distress Syndrome (ARDS)"
)

define "Problem has status of-fe3402a79bb17fd648daf2f94a1e55b0":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-ee3ed85f3abbadcf71f224c9f8051c60":
   "Problem has status of-fe3402a79bb17fd648daf2f94a1e55b0"
   and ( "Patient has a problem of-72b22da0bf194361e40e1bf43e7a48f0")

define "Patient has lab result with test name (specific for an organism or substance)-8190fc94e32b5e4b42e02edc1e646ba4":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "SARS (Tests for SARS_CoV by Unspecified Method)"
)

define "Lab Result Value (ordinal)-c696badb3a843176de31226b2141c470":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-104c98bb2ee5f8ccc813781f98197a52":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "SARS (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-549d43641e805a2083a4a103d86c720a":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-464c58e84c6a759fd8f330a3b448f299":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "SARS (Tests for SARS_CoV Antibody)"
)

define "Lab Result Value (ordinal)-d7699dd5c9f71888cb547cc8bc8da60c":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-ff9181282e5f9c42a0e2b2ed802c2a9d":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "SARS (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-d81a4ef8999d856c7d77c574a9e2cc1b":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-6fffe8f42993ee9eed84f952d423c06d":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "SARS (Tests for SARS_CoV Nucleic Acid)"
)

define "Lab Result Value (ordinal)-612650a5effb67bd63f98622c7f74a42":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-c7ff4af7f6f69880c755ddc3da84b6ec":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "SARS (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-6ff7b637e65abc4695c19a01dbf68dc9":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-d975d2131cc4fdbf1ae6fa2894e0b600":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-366736a8ce7654b4f6f15898fbca0628":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "SARS (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-5c740bb3877b8414e7e8591f5068ccc1":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Fever"
)

define "Patient has a problem of-2c6469ebebf44cf77f58fc0563639b90":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Fever"
)

define "Problem has status of-565696ead08b3a4b37b593408c1c1cba":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-c3085455ca71825f125f6be1aa2c9e10":
   "Problem has status of-565696ead08b3a4b37b593408c1c1cba"
   and ( "Patient has a problem of-2c6469ebebf44cf77f58fc0563639b90")

define "Patient has lab result with test name (specific for an organism or substance)-474ed1d486b2e36151c8596ed1e91eac":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "SARS (Tests for SARS_CoV by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-ac668fc7cf9adde63cf10c6a5acddafb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-12ddf543203572a6cd53f3e517f9c809":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "SARS (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-acc8d84d155503f08146376f7011281d":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-96560296ff2f88c8212e9a052b5286ad":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viruses (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-603d589dc28717f0c01367aaa6169746":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "SARS (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-938930828454242d3342e55a46f1a145":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "SARS (Disorders)"
)

define "Patient has a problem of-30c4be47192320c7e47e6e76c604bf37":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "SARS (Disorders)"
)

define "Problem has status of-ad8e153f111275a5f6f37abe1485e3b0":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-1878d7525128f27253baeea7ca68b2fa":
   "Problem has status of-ad8e153f111275a5f6f37abe1485e3b0"
   and ( "Patient has a problem of-30c4be47192320c7e47e6e76c604bf37")

define "ConditionCriteriaMet":
   "Group 1-1878d7525128f27253baeea7ca68b2fa"
   or ( "Patient has a diagnosis of-938930828454242d3342e55a46f1a145")
   or ( "Lab Result Value (nominal)-603d589dc28717f0c01367aaa6169746")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-96560296ff2f88c8212e9a052b5286ad")
   or ( "Lab Result Interpretation-acc8d84d155503f08146376f7011281d")
   or ( "Lab Result Value (nominal)-12ddf543203572a6cd53f3e517f9c809")
   or ( "Lab Result Value (ordinal)-ac668fc7cf9adde63cf10c6a5acddafb")
   and ( "Patient has lab result with test name (specific for an organism or substance)-474ed1d486b2e36151c8596ed1e91eac")
   and ( "Group 1-c3085455ca71825f125f6be1aa2c9e10")
   or ( "Patient has a diagnosis of-5c740bb3877b8414e7e8591f5068ccc1")
   or ( "Lab Result Value (nominal)-366736a8ce7654b4f6f15898fbca0628")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-d975d2131cc4fdbf1ae6fa2894e0b600")
   or ( "Lab Result Interpretation-6ff7b637e65abc4695c19a01dbf68dc9")
   or ( "Lab Result Value (nominal)-c7ff4af7f6f69880c755ddc3da84b6ec")
   or ( "Lab Result Value (ordinal)-612650a5effb67bd63f98622c7f74a42")
   and ( "Patient has lab result with test name (specific for an organism or substance)-6fffe8f42993ee9eed84f952d423c06d")
   or ( "Lab Result Interpretation-d81a4ef8999d856c7d77c574a9e2cc1b")
   or ( "Lab Result Value (nominal)-ff9181282e5f9c42a0e2b2ed802c2a9d")
   or ( "Lab Result Value (ordinal)-d7699dd5c9f71888cb547cc8bc8da60c")
   and ( "Patient has lab result with test name (specific for an organism or substance)-464c58e84c6a759fd8f330a3b448f299")
   or ( "Lab Result Interpretation-549d43641e805a2083a4a103d86c720a")
   or ( "Lab Result Value (nominal)-104c98bb2ee5f8ccc813781f98197a52")
   or ( "Lab Result Value (ordinal)-c696badb3a843176de31226b2141c470")
   and ( "Patient has lab result with test name (specific for an organism or substance)-8190fc94e32b5e4b42e02edc1e646ba4")
   and ( "Group 1-ee3ed85f3abbadcf71f224c9f8051c60")
   or ( "Patient has a diagnosis of-3a440f46d58456ef64671514734600e7")
