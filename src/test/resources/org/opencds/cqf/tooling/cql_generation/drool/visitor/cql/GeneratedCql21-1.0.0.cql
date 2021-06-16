library GeneratedCql21 version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Meningococcal Disease (Tests for Neisseria meningitidis Antigen)" : 'https://hln.com/fhir/ValueSet/MEN009'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Meningococcal Disease (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/MEN001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Meningococcal Disease (Tests for Neisseria meningitidis Antigen in Tissue by Immunohistochemistry)" : 'https://hln.com/fhir/ValueSet/MEN007'
valueset "Meningococcal Disease (Tests for Neisseria meningitidis Antigen in CSF by Latex Agglutination)" : 'https://hln.com/fhir/ValueSet/MEN010'
valueset "Meningococcal Disease (Tests for Neisseria meningitidis Antigen in Specimen from Normally Sterile Site by Latex Agglutination)" : 'https://hln.com/fhir/ValueSet/MEN008'
valueset "Meningococcal Disease (Tests for Neisseria meningitidis Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/MEN006'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Meningococcal Disease (Tests for Neisseria meningitidis Nucleic Acid in Specimen from Normally Sterile Site)" : 'https://hln.com/fhir/ValueSet/MEN005'
valueset "Organisms (Tests for Nucleic Acid in Specimen from Normally Sterile Site)" : 'https://hln.com/fhir/ValueSet/LabTST024'
valueset "Adrenal Hemorrhage" : 'https://hln.com/fhir/ValueSet/SYMP034'
valueset "Meningococcal Disease (Tests for Neisseria meningitidis by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/MEN004'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'
valueset "Meningococcal Disease (Disorders)" : 'https://hln.com/fhir/ValueSet/MEN002'
valueset "Organisms (Tests by Microscopic Observation in Specimen from Normally Sterile Site by Gram Stain)" : 'https://hln.com/fhir/ValueSet/LabTST009'
valueset "Discharge Disposition [Expired]" : 'https://hln.com/fhir/ValueSet/ENC001'
valueset "Purpura" : 'https://hln.com/fhir/ValueSet/SYMP032'
valueset "Purulent Meninges" : 'https://hln.com/fhir/ValueSet/SYMP033'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-c0ca5397517a7f79693b049ab204e2be":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Meningococcal Disease (Tests for Neisseria meningitidis Antigen)"
)

define "Lab Result Value (ordinal)-f2035c89dd3215a9d5e87af7dbc18d99":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-ea9b3ba36173b41293fb1354bb3e7414":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-5483666fec23206a3c93b0b9ae45ea50":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-56adefe87ec8513231a5580cc87bf8f4":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Meningococcal Disease (Tests for Neisseria meningitidis Antigen in Tissue by Immunohistochemistry)"
)

define "Lab Result Value (ordinal)-be32f4fd9bd03318d9faf4f3829c9318":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-d1ccc8c168407f09c397f57efcd2d74e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-6eb93f6cd6bbd639db0c391ea5755501":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-a391260bf1c4d8e1a38320c9cfe71d95":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Meningococcal Disease (Tests for Neisseria meningitidis Antigen in CSF by Latex Agglutination)"
)

define "Lab Result Value (ordinal)-aab0719e8c318511d72498e73fde628e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-b5ec3913ba8b0b38a08f34a6649336ef":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-31c3fed63d6f16d7a1a0e77a9617d900":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-6201b09341716119c6b430ae63b97273":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Meningococcal Disease (Tests for Neisseria meningitidis Antigen in Specimen from Normally Sterile Site by Latex Agglutination)"
)

define "Lab Result Value (ordinal)-3710fca7e2880ddfc396e4ad96b8078b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-8624c3a1a15fa01a723fefc347ebe113":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-18dcb14e780b121dc08629b163a40e51":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-f429a760a2722d7d4134180a49f865d9":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Meningococcal Disease (Tests for Neisseria meningitidis Nucleic Acid)"
)

define "Lab Result Value (ordinal)-4b8a0efd3e0ec5f0f86088c1cf1a9982":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-db9dcc63d2bfc3e88e7dda3641d8d5c6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-60a15396bc0aa7316fa55b3b95d5d3d2":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-718b14d294bc64a2a4f6cb77827a8bdd":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-73023c10a51cbd2092c130c4d3906c19":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-dc91e6964ceed38280c975827cafa5e7":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Meningococcal Disease (Tests for Neisseria meningitidis Nucleic Acid in Specimen from Normally Sterile Site)"
)

define "Lab Result Value (ordinal)-89f3f6d83792c71ac36d8687f54100c0":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-8f3d84384b61d192fffad4fa130799e5":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-27cc13fdea093f22b6b700986781bf41":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-b8518737885006b34de360585a346aae":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid in Specimen from Normally Sterile Site)"
)

define "Lab Result Value (nominal)-4a9d171cb03bb7e040077529a5b14acb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-60fcf275cc53bfaeee77efcbda6a5e08":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Adrenal Hemorrhage"
)

define "Patient has a problem of-5ef3064f1e58472c2583d0ac6dba18f9":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Adrenal Hemorrhage"
)

define "Problem has status of-4e2ff652543e6f4c5e96344194ae2011":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-4e45659a6a5fde0ada1a2de055c1d51d":
   "Problem has status of-4e2ff652543e6f4c5e96344194ae2011"
   and ( "Patient has a problem of-5ef3064f1e58472c2583d0ac6dba18f9")

define "Patient has lab result with test name (specific for an organism or substance)-512b7ab7cdc807865cb5749c5f7507fe":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Meningococcal Disease (Tests for Neisseria meningitidis by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-9e09d6688c03c2fe7857dedb509c520e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-e08f28c6d33d4aac9a442d8c3c5ade1f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-54bff3d5371677cfc3aa93949c5cfb66":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-6f3ae98e6f36d2c78411a545be0ce2b9":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-f69e0f137496c6c6755ba7394004992e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-12a239d6a196a8699e69e2e2a2a9d391":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Meningococcal Disease (Disorders)"
)

define "Patient has a problem of-406bd42b4ab351af2132355de57afb2d":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Meningococcal Disease (Disorders)"
)

define "Problem has status of-aec7cdc194c7f9d1bd3470678e03d8c0":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-b0906b073ca31dedf81fccce49a25dd8":
   "Problem has status of-aec7cdc194c7f9d1bd3470678e03d8c0"
   and ( "Patient has a problem of-406bd42b4ab351af2132355de57afb2d")

define "Patient has lab result with test name (specific for an organism or substance)-71df4e19427de75675705925fc865f4a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests by Microscopic Observation in Specimen from Normally Sterile Site by Gram Stain)"
)

define "Lab Result Value (ordinal)-edf278ef2aa370f8af0a62c4230ceb32":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-75deddebba2d8ca9711d75e69cb86248":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Meningococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-f18c623e37da33c4fefbbdb1ee9659f9":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient is deceased-5f87210438bf0f1c5d14a2f70cb5f743":
  exists (
    ([Patient] P
      return P.deceased as FHIR.boolean
  ) Alias
    where not  Equal( Alias, true)
)

define "Patient has a discharge disposition of-1cf4abd817c4592e2aa8444b9cbfb9c5":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Discharge Disposition [Expired]"
)

define "Patient has a diagnosis of-c94ea5b4893d2ff9e01fce81bb952e36":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Purpura"
)

define "Patient has a problem of-70c5e0ccb1db34399d268a77eb10f129":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Purpura"
)

define "Problem has status of-85140fe750e73b96d9a8e5115b381225":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-d2e75b84f5c1835b256fd572309c5375":
   "Problem has status of-85140fe750e73b96d9a8e5115b381225"
   and ( "Patient has a problem of-70c5e0ccb1db34399d268a77eb10f129")

define "Patient has a diagnosis of-b1765401ba713b558b786768a55b2f46":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Purulent Meninges"
)

define "Patient has a problem of-b9c3c455cf64598fd04384f51976c14f":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Purulent Meninges"
)

define "Problem has status of-3e412ac7213870c9487601574056db63":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-e5e1b17f22fed0432910cd2f724000f8":
   "Problem has status of-3e412ac7213870c9487601574056db63"
   and ( "Patient has a problem of-b9c3c455cf64598fd04384f51976c14f")

define "ConditionCriteriaMet":
   "Group 1-e5e1b17f22fed0432910cd2f724000f8"
   or ( "Patient has a diagnosis of-b1765401ba713b558b786768a55b2f46")
   and ( "Group 1-d2e75b84f5c1835b256fd572309c5375")
   or ( "Patient has a diagnosis of-c94ea5b4893d2ff9e01fce81bb952e36")
   or ( "Patient has a discharge disposition of-1cf4abd817c4592e2aa8444b9cbfb9c5")
   or ( "Patient is deceased-5f87210438bf0f1c5d14a2f70cb5f743")
   or ( "Lab Result Interpretation-f18c623e37da33c4fefbbdb1ee9659f9")
   or ( "Lab Result Value (nominal)-75deddebba2d8ca9711d75e69cb86248")
   or ( "Lab Result Value (ordinal)-edf278ef2aa370f8af0a62c4230ceb32")
   and ( "Patient has lab result with test name (specific for an organism or substance)-71df4e19427de75675705925fc865f4a")
   and ( "Group 1-b0906b073ca31dedf81fccce49a25dd8")
   or ( "Patient has a diagnosis of-12a239d6a196a8699e69e2e2a2a9d391")
   or ( "Lab Result Value (nominal)-f69e0f137496c6c6755ba7394004992e")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-6f3ae98e6f36d2c78411a545be0ce2b9")
   or ( "Lab Result Interpretation-54bff3d5371677cfc3aa93949c5cfb66")
   or ( "Lab Result Value (nominal)-e08f28c6d33d4aac9a442d8c3c5ade1f")
   or ( "Lab Result Value (ordinal)-9e09d6688c03c2fe7857dedb509c520e")
   and ( "Patient has lab result with test name (specific for an organism or substance)-512b7ab7cdc807865cb5749c5f7507fe")
   and ( "Group 1-4e45659a6a5fde0ada1a2de055c1d51d")
   or ( "Patient has a diagnosis of-60fcf275cc53bfaeee77efcbda6a5e08")
   or ( "Lab Result Value (nominal)-4a9d171cb03bb7e040077529a5b14acb")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-b8518737885006b34de360585a346aae")
   or ( "Lab Result Interpretation-27cc13fdea093f22b6b700986781bf41")
   or ( "Lab Result Value (nominal)-8f3d84384b61d192fffad4fa130799e5")
   or ( "Lab Result Value (ordinal)-89f3f6d83792c71ac36d8687f54100c0")
   and ( "Patient has lab result with test name (specific for an organism or substance)-dc91e6964ceed38280c975827cafa5e7")
   or ( "Lab Result Value (nominal)-73023c10a51cbd2092c130c4d3906c19")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-718b14d294bc64a2a4f6cb77827a8bdd")
   or ( "Lab Result Interpretation-60a15396bc0aa7316fa55b3b95d5d3d2")
   or ( "Lab Result Value (nominal)-db9dcc63d2bfc3e88e7dda3641d8d5c6")
   or ( "Lab Result Value (ordinal)-4b8a0efd3e0ec5f0f86088c1cf1a9982")
   and ( "Patient has lab result with test name (specific for an organism or substance)-f429a760a2722d7d4134180a49f865d9")
   or ( "Lab Result Interpretation-18dcb14e780b121dc08629b163a40e51")
   or ( "Lab Result Value (nominal)-8624c3a1a15fa01a723fefc347ebe113")
   or ( "Lab Result Value (ordinal)-3710fca7e2880ddfc396e4ad96b8078b")
   and ( "Patient has lab result with test name (specific for an organism or substance)-6201b09341716119c6b430ae63b97273")
   or ( "Lab Result Interpretation-31c3fed63d6f16d7a1a0e77a9617d900")
   or ( "Lab Result Value (nominal)-b5ec3913ba8b0b38a08f34a6649336ef")
   or ( "Lab Result Value (ordinal)-aab0719e8c318511d72498e73fde628e")
   and ( "Patient has lab result with test name (specific for an organism or substance)-a391260bf1c4d8e1a38320c9cfe71d95")
   or ( "Lab Result Interpretation-6eb93f6cd6bbd639db0c391ea5755501")
   or ( "Lab Result Value (nominal)-d1ccc8c168407f09c397f57efcd2d74e")
   or ( "Lab Result Value (ordinal)-be32f4fd9bd03318d9faf4f3829c9318")
   and ( "Patient has lab result with test name (specific for an organism or substance)-56adefe87ec8513231a5580cc87bf8f4")
   or ( "Lab Result Interpretation-5483666fec23206a3c93b0b9ae45ea50")
   or ( "Lab Result Value (nominal)-ea9b3ba36173b41293fb1354bb3e7414")
   or ( "Lab Result Value (ordinal)-f2035c89dd3215a9d5e87af7dbc18d99")
   and ( "Patient has lab result with test name (specific for an organism or substance)-c0ca5397517a7f79693b049ab204e2be")
