library GeneratedCql32 version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae Antigen)" : 'https://hln.com/fhir/ValueSet/IPD007'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/IPD001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae Antigen in Specimen from Normally Sterile Site)" : 'https://hln.com/fhir/ValueSet/IPD008'
valueset "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/IPD005'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae Nucleic Acid in Specimen from Normally Sterile Site)" : 'https://hln.com/fhir/ValueSet/IPD006'
valueset "Organisms (Tests for Nucleic Acid in Specimen from Normally Sterile Site)" : 'https://hln.com/fhir/ValueSet/LabTST024'
valueset "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/IPD003'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'
valueset "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae in Specimen from Normally Sterile Site by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/IPD004'
valueset "Bacteria (Tests in Specimen from Normally Sterile Site by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST005'
valueset "Invasive Pneumococcal Disease (Disorders)" : 'https://hln.com/fhir/ValueSet/IPD002'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-b14bdb4ba04e01a54302997aeaac5f4a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae Antigen)"
)

define "Lab Result Value (ordinal)-15818d094f5794bb1c6ba3b4b39a3653":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-520e02967c64c88e3ad6426e16f82b11":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-3d28528457319184c359dc625352311b":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-2bb5f9451e82ffb1b5a7582742d4b2c7":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae Antigen in Specimen from Normally Sterile Site)"
)

define "Lab Result Value (ordinal)-77b484911f269075324f7340d7ae59cb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-2241affab8d2fd6c50961b91f99b7ad2":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-c6708f3c73f5846b33868de7469acb31":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-6acb32a1692110d3618585857d46b857":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae Nucleic Acid)"
)

define "Lab Result Value (ordinal)-fd803105484740dc6a978a63d5a21eeb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-a7ad73546d8ef27e64482378e94955f7":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-c85f7fb7297232b99e0a05b18ec68d18":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-5b3acc5fd7547df832ff11903ff8f83a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-82cf53c3b25f2c96f25803d5c6773598":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-d0482396cb4ed3e78ede38507d5c8169":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae Nucleic Acid in Specimen from Normally Sterile Site)"
)

define "Lab Result Value (ordinal)-708142e916478b451312559f66159b13":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-175b8ba2813bf87022d3a6268d0f52a1":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-c2fee84cfd9327b38a0651ae9c9b3ebe":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-ec43a1c186c9d93512567f54e0006d18":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid in Specimen from Normally Sterile Site)"
)

define "Lab Result Value (nominal)-b2bbe1fb912a4f8a391e77b018ec5371":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-30fe0f89d26aaa022db5636e25661873":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-df7d6a9a5ce5e7d208f2822e01f41485":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-33940cf2472d890b7d0ba30a15ea02af":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-233a030daa117e353eb4eac571f277f7":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-de9550bd9e0db5e369b6430f893a5d59":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-6cca91e61a4aed53cb90dec3b68d500c":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-b94f6718c9a5f0814e775dffcc49b3bf":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Tests for Streptococcus pneumoniae in Specimen from Normally Sterile Site by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-d4e05670c2176b2c6fe80bed13546ff7":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-a87c9aa293b0eeb0efb4c8c92edc6ff0":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-aa20fb44e638863a79dfcb9ef90b7dcb":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-ffea58eca80d07c5ccc39949f255bd82":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests in Specimen from Normally Sterile Site by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-2f9eb951b29d680dec88e4acc576aaa6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-ea28a14f6075f2183b02a656e2f9e3b5":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Disorders)"
)

define "Patient has a problem of-3bcb80233c89c11d35911f7c7912c33f":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Invasive Pneumococcal Disease (Disorders)"
)

define "Problem has status of-385dac209bcd8b6233fc5b6fbd32afc5":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-ecd36ccea5d2985f9a1e26bcc5d34f1c":
   "Problem has status of-385dac209bcd8b6233fc5b6fbd32afc5"
   and ( "Patient has a problem of-3bcb80233c89c11d35911f7c7912c33f")

define "ConditionCriteriaMet":
   "Group 1-ecd36ccea5d2985f9a1e26bcc5d34f1c"
   or ( "Patient has a diagnosis of-ea28a14f6075f2183b02a656e2f9e3b5")
   or ( "Lab Result Value (nominal)-2f9eb951b29d680dec88e4acc576aaa6")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-ffea58eca80d07c5ccc39949f255bd82")
   or ( "Lab Result Interpretation-aa20fb44e638863a79dfcb9ef90b7dcb")
   or ( "Lab Result Value (nominal)-a87c9aa293b0eeb0efb4c8c92edc6ff0")
   or ( "Lab Result Value (ordinal)-d4e05670c2176b2c6fe80bed13546ff7")
   and ( "Patient has lab result with test name (specific for an organism or substance)-b94f6718c9a5f0814e775dffcc49b3bf")
   or ( "Lab Result Value (nominal)-6cca91e61a4aed53cb90dec3b68d500c")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-de9550bd9e0db5e369b6430f893a5d59")
   or ( "Lab Result Interpretation-233a030daa117e353eb4eac571f277f7")
   or ( "Lab Result Value (nominal)-33940cf2472d890b7d0ba30a15ea02af")
   or ( "Lab Result Value (ordinal)-df7d6a9a5ce5e7d208f2822e01f41485")
   and ( "Patient has lab result with test name (specific for an organism or substance)-30fe0f89d26aaa022db5636e25661873")
   or ( "Lab Result Value (nominal)-b2bbe1fb912a4f8a391e77b018ec5371")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-ec43a1c186c9d93512567f54e0006d18")
   or ( "Lab Result Interpretation-c2fee84cfd9327b38a0651ae9c9b3ebe")
   or ( "Lab Result Value (nominal)-175b8ba2813bf87022d3a6268d0f52a1")
   or ( "Lab Result Value (ordinal)-708142e916478b451312559f66159b13")
   and ( "Patient has lab result with test name (specific for an organism or substance)-d0482396cb4ed3e78ede38507d5c8169")
   or ( "Lab Result Value (nominal)-82cf53c3b25f2c96f25803d5c6773598")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-5b3acc5fd7547df832ff11903ff8f83a")
   or ( "Lab Result Interpretation-c85f7fb7297232b99e0a05b18ec68d18")
   or ( "Lab Result Value (nominal)-a7ad73546d8ef27e64482378e94955f7")
   or ( "Lab Result Value (ordinal)-fd803105484740dc6a978a63d5a21eeb")
   and ( "Patient has lab result with test name (specific for an organism or substance)-6acb32a1692110d3618585857d46b857")
   or ( "Lab Result Interpretation-c6708f3c73f5846b33868de7469acb31")
   or ( "Lab Result Value (nominal)-2241affab8d2fd6c50961b91f99b7ad2")
   or ( "Lab Result Value (ordinal)-77b484911f269075324f7340d7ae59cb")
   and ( "Patient has lab result with test name (specific for an organism or substance)-2bb5f9451e82ffb1b5a7582742d4b2c7")
   or ( "Lab Result Interpretation-3d28528457319184c359dc625352311b")
   or ( "Lab Result Value (nominal)-520e02967c64c88e3ad6426e16f82b11")
   or ( "Lab Result Value (ordinal)-15818d094f5794bb1c6ba3b4b39a3653")
   and ( "Patient has lab result with test name (specific for an organism or substance)-b14bdb4ba04e01a54302997aeaac5f4a")
