library Toxic_effect_of_carbon_monoxide_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Carbon Monoxide Poisoning (Disorders)" : 'https://hln.com/fhir/ValueSet/CMP002'
valueset "Chest Pain" : 'https://hln.com/fhir/ValueSet/SYMP054'
valueset "Confusion" : 'https://hln.com/fhir/ValueSet/SYMP063'
valueset "Loss of Consciousness" : 'https://hln.com/fhir/ValueSet/SYMP065'
valueset "Nausea" : 'https://hln.com/fhir/ValueSet/SYMP062'
valueset "Smoking Status [Nonsmoker]" : 'https://hln.com/fhir/ValueSet/SYMP087'
valueset "Smoking Status [Unknown]" : 'https://hln.com/fhir/ValueSet/SYMP088'
valueset "Dyspnea" : 'https://hln.com/fhir/ValueSet/SYMP058'
valueset "Carbon Monoxide Poisoning (Tests for Carboxyhemoglobin in Blood [Quantitative])" : 'https://hln.com/fhir/ValueSet/CMP004'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Carbon Monoxide Poisoning (Carbon Monoxide Exposure)" : 'https://hln.com/fhir/ValueSet/CMP003'
valueset "Vomiting" : 'https://hln.com/fhir/ValueSet/SYMP010'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-83459dc3afadb3fb58c6d082ac2ac665":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Carbon Monoxide Poisoning (Disorders)"
)

define "Patient has a problem of-f6f22b70e3b54fa4510a25efdd2115c1":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Carbon Monoxide Poisoning (Disorders)"
)

define "Problem has status of-44f8c1b04ec5b20f4d27034368b33fad":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-72177d31d9c78bc2fe198d778b8e5a2a":
   "Problem has status of-44f8c1b04ec5b20f4d27034368b33fad"
   and ( "Patient has a problem of-f6f22b70e3b54fa4510a25efdd2115c1")

define "Patient has a diagnosis of-da36a25ce16c87446f972ab7c3fb6f49":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Chest Pain"
)

define "Patient has a problem of-bcb727add2de43f5906f295f4bc0edb3":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Chest Pain"
)

define "Problem has status of-3ba6f12353b44162b0e1c7656b708b56":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-fc42b98f11ac8587eb88b830f90a55cc":
   "Problem has status of-3ba6f12353b44162b0e1c7656b708b56"
   and ( "Patient has a problem of-bcb727add2de43f5906f295f4bc0edb3")

define "Patient has a diagnosis of-97aa263451269e99bd38d6064d6b65be":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Confusion"
)

define "Patient has a problem of-8391d87cd5d4831a7c002c244878f78d":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Confusion"
)

define "Problem has status of-e4b05f094dad6709c0369c37cc70bdd0":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-d88787552df6d5f0e5a121d1447c7fd5":
   "Problem has status of-e4b05f094dad6709c0369c37cc70bdd0"
   and ( "Patient has a problem of-8391d87cd5d4831a7c002c244878f78d")

define "Patient has a diagnosis of-354cb5268711421cabe7e75259744e9f":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Loss of Consciousness"
)

define "Patient has a problem of-6a904c5c8039505eed5e1c081a62519e":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Loss of Consciousness"
)

define "Problem has status of-76a622471b23b0de713fd5ffc34e7a47":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-9ea3d98b5eee3960d98c75ed2d41c963":
   "Problem has status of-76a622471b23b0de713fd5ffc34e7a47"
   and ( "Patient has a problem of-6a904c5c8039505eed5e1c081a62519e")

define "Patient has a diagnosis of-628ba41db48e260a6984c665db6e5360":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Nausea"
)

define "Patient has a problem of-11f2d21e1d6fabcf7fe45ac22ee948b1":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Nausea"
)

define "Problem has status of-4baec43797c261c035f4f10ca7ee08e0":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-ccaf8f557b8d164224b34c2819a73031":
   "Problem has status of-4baec43797c261c035f4f10ca7ee08e0"
   and ( "Patient has a problem of-11f2d21e1d6fabcf7fe45ac22ee948b1")

define "Patient has a diagnosis of-19a4179c2696da45f8cb040f37bc65d8":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Smoking Status [Unknown]"
)

define "Patient has a problem of-88e5f3327cecbe45d5b32b7897599239":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Smoking Status [Unknown]"
)

define "Problem has status of-a857bbfdd1c9cb1d1ca0625d20c33d3c":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-f503827d332bad1250cb99592672b9fc":
   "Problem has status of-a857bbfdd1c9cb1d1ca0625d20c33d3c"
   and ( "Patient has a problem of-88e5f3327cecbe45d5b32b7897599239")

define "Patient has a diagnosis of-19e7a43c76797c7d3325a9801afd4bc9":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Dyspnea"
)

define "Patient has a problem of-4b75c459d1f552fd2d08025e9a878a95":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Dyspnea"
)

define "Problem has status of-4de0a640f6734169378cf68cce6f7390":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-d843e2818ddb156a7927177638187c05":
   "Problem has status of-4de0a640f6734169378cf68cce6f7390"
   and ( "Patient has a problem of-4b75c459d1f552fd2d08025e9a878a95")

define "Patient has lab result with test name (specific or an organism or substance)-1918fca84fc58e3c6d920571f796779d":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Carbon Monoxide Poisoning (Tests for Carboxyhemoglobin in Blood [Quantitative])"
)

define "Lab Result Value (quantitative)-d917b2e2f79bfa51412bba447fe6c7bf":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias >= 2.5 '%'
)

define "Lab Result Value (quantitative)-db3c31df3bdf7e8703734156e2df64cf":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias >= ToQuantity(0.025)0.025
)

define "Lab Result Value (ordinal)-af0ff1fa125791d76331f9834beef2bf":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-9ef4c3d366668f1f181bb7a3a08a3e8c":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Group 1.1-b0d9fbc0f863f970639a3e3a1b9ff950":
   "Lab Result Interpretation-9ef4c3d366668f1f181bb7a3a08a3e8c"
   or ( "Lab Result Value (ordinal)-af0ff1fa125791d76331f9834beef2bf")
   or ( "Lab Result Value (quantitative)-db3c31df3bdf7e8703734156e2df64cf")
   or ( "Lab Result Value (quantitative)-d917b2e2f79bfa51412bba447fe6c7bf")
   and ( "Patient has lab result with test name (specific or an organism or substance)-1918fca84fc58e3c6d920571f796779d")

define "Group 1-75c8254eedcc0289dadb7118410e72d9":
   "Group 1.1-b0d9fbc0f863f970639a3e3a1b9ff950"
   and ( "Group 1-d843e2818ddb156a7927177638187c05")

define "Patient has a diagnosis of-8eba53475ad9e55e36eed33c10a26bff":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Carbon Monoxide Poisoning (Carbon Monoxide Exposure)"
)

define "Patient has problem of-c488363dcac81704abc586ab7c0cfc71":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Carbon Monoxide Poisoning (Carbon Monoxide Exposure)"
)

define "Problem has status of-a8ecb557761e73d6623feccc2cfd2366":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "null-34bf57cb-cc20-4686-ac21-1c563d17903b":
   "Problem has status of-a8ecb557761e73d6623feccc2cfd2366"
   or ( "Patient has problem of-c488363dcac81704abc586ab7c0cfc71")

define "Patient has a diagnosis of-a94b60d91575c6949d6beb27624b0135":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Vomiting"
)

define "Patient has a problem of-cfcd16531bd540ff339d87fd8aa070ea":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Vomiting"
)

define "Problem has status of-6cc940a53c656734adb8a91b1929b003":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-81235489aca2b635bcb032fce6e471a9":
   "Problem has status of-6cc940a53c656734adb8a91b1929b003"
   and ( "Patient has a problem of-cfcd16531bd540ff339d87fd8aa070ea")

define "ConditionCriteriaMet":
   "Group 1-81235489aca2b635bcb032fce6e471a9"
   or ( "Patient has a diagnosis of-a94b60d91575c6949d6beb27624b0135")
   or ( "null-34bf57cb-cc20-4686-ac21-1c563d17903b")
   or ( "Patient has a diagnosis of-8eba53475ad9e55e36eed33c10a26bff")
   or ( "Group 1-75c8254eedcc0289dadb7118410e72d9")
   or ( "Patient has a diagnosis of-19e7a43c76797c7d3325a9801afd4bc9")
   and ( "Group 1-f503827d332bad1250cb99592672b9fc")
   or ( "Patient has a diagnosis of-19a4179c2696da45f8cb040f37bc65d8")
   and ( "Group 1-ccaf8f557b8d164224b34c2819a73031")
   or ( "Patient has a diagnosis of-628ba41db48e260a6984c665db6e5360")
   and ( "Group 1-9ea3d98b5eee3960d98c75ed2d41c963")
   or ( "Patient has a diagnosis of-354cb5268711421cabe7e75259744e9f")
   and ( "Group 1-d88787552df6d5f0e5a121d1447c7fd5")
   or ( "Patient has a diagnosis of-97aa263451269e99bd38d6064d6b65be")
   and ( "Group 1-fc42b98f11ac8587eb88b830f90a55cc")
   or ( "Patient has a diagnosis of-da36a25ce16c87446f972ab7c3fb6f49")
   and ( "Group 1-72177d31d9c78bc2fe198d778b8e5a2a")
   or ( "Patient has a diagnosis of-83459dc3afadb3fb58c6d082ac2ac665")
