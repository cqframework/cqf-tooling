library Congenital_rubella_syndrome_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Congenital Rubella Syndrome (Disorders)" : 'https://hln.com/fhir/ValueSet/RUB003'
valueset "Assertion" : 'https://hln.com/fhir/ValueSet/ASSERTION'
valueset "Pregnant" : 'https://hln.com/fhir/ValueSet/PREG1'
valueset "Rubella (Tests for Rubella virus IgM Antibody)" : 'https://hln.com/fhir/ValueSet/RUB007'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Rubella (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/RUB001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Rubella (Tests for rubella virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/RUB006'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Clofazimine" : 'https://hln.com/fhir/ValueSet/MED008'
valueset "Tetanus (Tetanus Immune Globulin)" : 'https://hln.com/fhir/ValueSet/MED009'
valueset "Rubella (Tests for Rubella virus by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/RUB005'
valueset "Viruses (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST002'
valueset "Rubella (Maternal Rubella during Pregnancy) (Disorders)" : 'https://hln.com/fhir/ValueSet/RUB004'
valueset "Rubella (Disorders)" : 'https://hln.com/fhir/ValueSet/RUB002'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-6e78f69dc1bcc2247250a5623fb7f63a":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Congenital Rubella Syndrome (Disorders)"
)

define "Patient has a problem of-5b4b4cd611844933e8d12ceee8fec1b2":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Congenital Rubella Syndrome (Disorders)"
)

define "Problem has status of-a9ef90b781668c893ae98a79b991a7be":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-b09a7d66e154a69b60eb8bb8effacaa5":
   "Problem has status of-a9ef90b781668c893ae98a79b991a7be"
   and ( "Patient has a problem of-5b4b4cd611844933e8d12ceee8fec1b2")

define "Patient record contains an observation with focus-bce60b9691493fa9847ff3302bd1db0a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Assertion"
)

define "Patient record contains an observation with value-8828c65fd45a41cce6c9ce926620181f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Pregnant"
)

define "Group 1-d4f7d89ba2f3869f23e4716d6cad920d":
   "Patient record contains an observation with value-8828c65fd45a41cce6c9ce926620181f"
   and ( "Patient record contains an observation with focus-bce60b9691493fa9847ff3302bd1db0a")

define "Patient has a problem of-25194aefab0cb4f38057ba62d65584ef":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Pregnant"
)

define "Problem has status of-aa776eae114ff70bdc6d52df25045354":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 2-b437b9b4ce7342572ea9a40ebb1ac787":
   "Problem has status of-aa776eae114ff70bdc6d52df25045354"
   and ( "Patient has a problem of-25194aefab0cb4f38057ba62d65584ef")

define "Patient has lab result with test name (specific for an organism or substance)-fdc5251f73bf1d9b637eedc611fc4039":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Rubella (Tests for Rubella virus IgM Antibody)"
)

define "Lab Result Value (ordinal)-129a032a19ce12fdaf05c9fbfa42c325":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-ab3ddaddf4fa3ab08753ec794699db21":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Rubella (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-b2413649f7e902a2f1a1bf4d3378bc1b":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-ff26f06bc8aec06ca89bb11f5394c7a3":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Rubella (Tests for rubella virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-13c6206cc9dbdef2be7bc465e510cc01":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-7ff772c71260932d3c329652f5608be0":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Rubella (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-5da7a278591cc09a4a412221d916be62":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-339e3daf91f4cb86ea922591e6f866fa":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-b0704ac128f9d7cdf9db6046addd52ab":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Rubella (Organism or Substance in Lab Results)"
)

define "first-c91c0584d58ccdeaa75f1661d46279db":
  exists (
    (( 
      [MedicationRequest] M
        return M.medication as FHIR.CodeableConcept
    ) 
    union ( 
      [MedicationAdministration] M
        let medicationResource:  First (
          [Medication: id in {  Last (
            Split (( M.medication as FHIR.Reference).reference, '/')
          ) }]
        )
        
        return medicationResource.code
    ) 
  ) Alias
    where Alias in "Clofazimine"
)

define "second-1519b506b6ed20ebee78e3f42efd62f6":
  exists (
    (( 
      [MedicationRequest] M
        return M.medication as FHIR.CodeableConcept
    ) 
    union ( 
      [MedicationAdministration] M
        let medicationResource:  First (
          [Medication: id in {  Last (
            Split (( M.medication as FHIR.Reference).reference, '/')
          ) }]
        )
        
        return medicationResource.code
    ) 
  ) Alias
    where Alias in "Tetanus (Tetanus Immune Globulin)"
)

define "Patient has lab result with test name (specific for an organism or substance)-712574c339f7494bbd38167b12e92f3a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Rubella (Tests for Rubella virus by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-c9d3a8b0d5cf2dbafafd29ae182925a5":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-49ae88f593442094e3103185b85407fc":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Rubella (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-1606139f5b3b881275b06fa9e170b671":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-92bde211c1050b2a58e2e5c8b0e1c45a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viruses (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-418a6af296a94de4d1619662db14dc02":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Rubella (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-29e4ad934760712c61e7d1b28c61e771":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Rubella (Maternal Rubella during Pregnancy) (Disorders)"
)

define "Patient has a problem of-b192bc94d6bf6ccff13c1238340de227":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Rubella (Maternal Rubella during Pregnancy) (Disorders)"
)

define "Problem has status of-45d462f02db9841c4cfd3202e586bf7d":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-c3913561cd81f09cce79ac07ba43c996":
   "Problem has status of-45d462f02db9841c4cfd3202e586bf7d"
   and ( "Patient has a problem of-b192bc94d6bf6ccff13c1238340de227")

define "Units:-b12f7635aaec1162109664325287c475":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) < 12 'months'

define "Patient has a diagnosis of-6dee8cf8dbbf5637103aed5a4f2306fb":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Rubella (Disorders)"
)

define "Patient has a problem of-bd945ea3e089e6bbe8000ff74c770fd4":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Rubella (Disorders)"
)

define "Problem has status of-1c43de862da0ab61488230f87a32e463":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-a64b8268f82165781f8907eed3659a06":
   "Problem has status of-1c43de862da0ab61488230f87a32e463"
   and ( "Patient has a problem of-bd945ea3e089e6bbe8000ff74c770fd4")

define "ConditionCriteriaMet":
   "Group 1-a64b8268f82165781f8907eed3659a06"
   or ( "Patient has a diagnosis of-6dee8cf8dbbf5637103aed5a4f2306fb")
   and ( "Units:-b12f7635aaec1162109664325287c475")
   and ( "Group 1-c3913561cd81f09cce79ac07ba43c996")
   or ( "Patient has a diagnosis of-29e4ad934760712c61e7d1b28c61e771")
   or ( "Lab Result Value (nominal)-418a6af296a94de4d1619662db14dc02")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-92bde211c1050b2a58e2e5c8b0e1c45a")
   or ( "Lab Result Interpretation-1606139f5b3b881275b06fa9e170b671")
   or ( "Lab Result Value (nominal)-49ae88f593442094e3103185b85407fc")
   or ( "Lab Result Value (ordinal)-c9d3a8b0d5cf2dbafafd29ae182925a5")
   and ( "Patient has lab result with test name (specific for an organism or substance)-712574c339f7494bbd38167b12e92f3a")
   and ( "second-1519b506b6ed20ebee78e3f42efd62f6")
   and ( "first-c91c0584d58ccdeaa75f1661d46279db")
   or ( "Lab Result Value (nominal)-b0704ac128f9d7cdf9db6046addd52ab")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-339e3daf91f4cb86ea922591e6f866fa")
   or ( "Lab Result Interpretation-5da7a278591cc09a4a412221d916be62")
   or ( "Lab Result Value (nominal)-7ff772c71260932d3c329652f5608be0")
   or ( "Lab Result Value (ordinal)-13c6206cc9dbdef2be7bc465e510cc01")
   and ( "Patient has lab result with test name (specific for an organism or substance)-ff26f06bc8aec06ca89bb11f5394c7a3")
   or ( "Lab Result Interpretation-b2413649f7e902a2f1a1bf4d3378bc1b")
   or ( "Lab Result Value (nominal)-ab3ddaddf4fa3ab08753ec794699db21")
   or ( "Lab Result Value (ordinal)-129a032a19ce12fdaf05c9fbfa42c325")
   and ( "Patient has lab result with test name (specific for an organism or substance)-fdc5251f73bf1d9b637eedc611fc4039")
   or ( "Group 2-b437b9b4ce7342572ea9a40ebb1ac787")
   or ( "Group 1-d4f7d89ba2f3869f23e4716d6cad920d")
   and ( "Group 1-b09a7d66e154a69b60eb8bb8effacaa5")
   or ( "Patient has a diagnosis of-6e78f69dc1bcc2247250a5623fb7f63a")
