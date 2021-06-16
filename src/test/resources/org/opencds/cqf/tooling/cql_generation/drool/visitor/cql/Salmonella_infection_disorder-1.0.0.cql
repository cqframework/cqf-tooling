library Salmonella_infection_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Salmonellosis (Tests for Salmonella except S. typhi and S. paratyphi Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/SAL004'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Salmonellosis [Salmonella Species and Subspecies] (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/SAL002'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Salmonellosis [Salmonella serovar] (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/SAL001'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Diarrhea" : 'https://hln.com/fhir/ValueSet/SYMP012'
valueset "Salmonellosis (Tests for Salmonella except S. typhi and S. paratyphi by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/SAL005'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'
valueset "Salmonellosis (Disorders)" : 'https://hln.com/fhir/ValueSet/SAL003'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-a1ab1c95619c70e544f179fd50b825be":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Salmonellosis (Tests for Salmonella except S. typhi and S. paratyphi Nucleic Acid)"
)

define "Lab Result Value (ordinal)-8a20f9b6d69d9a2a8b04cda8e525e8a7":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-a6e7fbe77b4fd9818123d0c948b90d41":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Salmonellosis [Salmonella Species and Subspecies] (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-d580d35e931894cea78e7f86080597dc":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Lab Result Value (other nominal)-aa531239d8d3e9c12edca6a7dcce2598":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Salmonellosis [Salmonella serovar] (Organism or Substance in Lab Results)"
)

define "Sub-Group 1-ef5caa6a-15d0-4a0a-b657-05d05e9cf245":
   "Lab Result Value (other nominal)-aa531239d8d3e9c12edca6a7dcce2598"
   or ( "Lab Result Interpretation-d580d35e931894cea78e7f86080597dc")
   or ( "Lab Result Value (nominal)-a6e7fbe77b4fd9818123d0c948b90d41")
   or ( "Lab Result Value (ordinal)-8a20f9b6d69d9a2a8b04cda8e525e8a7")

define "Group 1-e5116baf60d972b5292ff5e0983d5bba":
   "Sub-Group 1-ef5caa6a-15d0-4a0a-b657-05d05e9cf245"
   and ( "Patient has lab result with test name (specific for an organism or substance)-a1ab1c95619c70e544f179fd50b825be")

define "Patient has lab result with test name (NOT specific for an organism or substance)-7104f8473476fd8ffdc01396ab3fbe2a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-e2a0c6b28781f57c60afa3079c72a775":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Salmonellosis [Salmonella Species and Subspecies] (Organism or Substance in Lab Results)"
)

define "Lab Result Value (other nominal)-beb0ef0db7fad7283960a0599649c6c0":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Salmonellosis [Salmonella serovar] (Organism or Substance in Lab Results)"
)

define "SubGroup 2-6c2fe0f9-0851-478d-a4a7-6466e77051dc":
   "Lab Result Value (other nominal)-beb0ef0db7fad7283960a0599649c6c0"
   or ( "Lab Result Value (nominal)-e2a0c6b28781f57c60afa3079c72a775")

define "Group 2-4232568dd10e364693cceb848b8e77bf":
   "SubGroup 2-6c2fe0f9-0851-478d-a4a7-6466e77051dc"
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-7104f8473476fd8ffdc01396ab3fbe2a")

define "Patient has a diagnosis of-470dd2aa13cd2dcdcdeea53e25ad7746":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Diarrhea"
)

define "Patient has a problem of-ea22a41b793beb7ae4aac24b70600cdc":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Diarrhea"
)

define "Problem has status of-edf7d27fa84ee2a8ae88b24e57b13ed9":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-6c10234d7aa1c2adb8458a73b773cb67":
   "Problem has status of-edf7d27fa84ee2a8ae88b24e57b13ed9"
   and ( "Patient has a problem of-ea22a41b793beb7ae4aac24b70600cdc")

define "Patient has lab result with test name (specific for an organism or substance)-32e4a9465e6383ece5b9c71c506af2b2":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Salmonellosis (Tests for Salmonella except S. typhi and S. paratyphi by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-f524f680555581efaecf9aca9817dee9":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-3d4fde74259fdc7df1c9b73191f07a0c":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Salmonellosis [Salmonella Species and Subspecies] (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-81f1a9e97059aa5ed6df1a05255de419":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Lab Result Value (other nominal)-bdaecf0090ac3addd6d88fba7912eef6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Salmonellosis [Salmonella serovar] (Organism or Substance in Lab Results)"
)

define "Sub-Group 1-b9683bdb-82c8-45fe-957e-beef1df28bb2":
   "Lab Result Value (other nominal)-bdaecf0090ac3addd6d88fba7912eef6"
   or ( "Lab Result Interpretation-81f1a9e97059aa5ed6df1a05255de419")
   or ( "Lab Result Value (nominal)-3d4fde74259fdc7df1c9b73191f07a0c")
   or ( "Lab Result Value (ordinal)-f524f680555581efaecf9aca9817dee9")

define "Group 1-1a4aef3440feae61828cb7e1bbe8c9c6":
   "Sub-Group 1-b9683bdb-82c8-45fe-957e-beef1df28bb2"
   and ( "Patient has lab result with test name (specific for an organism or substance)-32e4a9465e6383ece5b9c71c506af2b2")

define "Patient has lab result with test name (NOT specific for an organism or substance)-ec49052489315618eb9dbe301fea15f7":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-e27659d45e7064cb9916668d4607937d":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Salmonellosis [Salmonella Species and Subspecies] (Organism or Substance in Lab Results)"
)

define "Lab Result Value (other nominal)-8172de81cb36a65ecc2b6d25487e7f84":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Salmonellosis [Salmonella serovar] (Organism or Substance in Lab Results)"
)

define "SubGroup 2-fef616b0-8b71-43de-97ba-8d001d3604b6":
   "Lab Result Value (other nominal)-8172de81cb36a65ecc2b6d25487e7f84"
   or ( "Lab Result Value (nominal)-e27659d45e7064cb9916668d4607937d")

define "Group 2-184a404d70b4f5952d9375b9f7e154eb":
   "SubGroup 2-fef616b0-8b71-43de-97ba-8d001d3604b6"
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-ec49052489315618eb9dbe301fea15f7")

define "Patient has a diagnosis of-662e1f8141a5501699c1e324ac782749":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Salmonellosis (Disorders)"
)

define "Patient has a problem of-fc4ab1589f4b22c4e21e347ce19116d7":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Salmonellosis (Disorders)"
)

define "Problem has status of-3abfe2473d53b8cf372fc3f838f59643":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-eb0add1926b822e1311c8e5e468e4b68":
   "Problem has status of-3abfe2473d53b8cf372fc3f838f59643"
   and ( "Patient has a problem of-fc4ab1589f4b22c4e21e347ce19116d7")

define "ConditionCriteriaMet":
   "Group 1-eb0add1926b822e1311c8e5e468e4b68"
   or ( "Patient has a diagnosis of-662e1f8141a5501699c1e324ac782749")
   or ( "Group 2-184a404d70b4f5952d9375b9f7e154eb")
   or ( "Group 1-1a4aef3440feae61828cb7e1bbe8c9c6")
   and ( "Group 1-6c10234d7aa1c2adb8458a73b773cb67")
   or ( "Patient has a diagnosis of-470dd2aa13cd2dcdcdeea53e25ad7746")
   or ( "Group 2-4232568dd10e364693cceb848b8e77bf")
   or ( "Group 1-e5116baf60d972b5292ff5e0983d5bba")
