library Salmonella_Paratyphi_A_organism version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Abdominal Cramps" : 'https://hln.com/fhir/ValueSet/SYMP011'
valueset "Anorexia" : 'https://hln.com/fhir/ValueSet/SYMP015'
valueset "S. Paratyphi Infection (Tests for S. Paratyphi Antibody)" : 'https://hln.com/fhir/ValueSet/SPI003'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "S. Paratyphi Infection (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/SPI001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Diarrhea" : 'https://hln.com/fhir/ValueSet/SYMP012'
valueset "Salmonellosis (Tests for Salmonella except S. typhi and S. paratyphi by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/SAL005'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'
valueset "S. Paratyphi Infection (Disorders)" : 'https://hln.com/fhir/ValueSet/SPI002'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-200ef5c1608f3135d02e2b62900b05c6":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Abdominal Cramps"
)

define "Patient has a problem of-1c0c0b84620c50dc40b228a4fc7550b6":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Abdominal Cramps"
)

define "Problem has status of-ce3ae70d8418cb4bf5e2989ddf50374a":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-568d63abad16a2a67939ef9dc450ed25":
   "Problem has status of-ce3ae70d8418cb4bf5e2989ddf50374a"
   and ( "Patient has a problem of-1c0c0b84620c50dc40b228a4fc7550b6")

define "Patient has a diagnosis of-595127c6a158871187807214544237d5":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Anorexia"
)

define "Patient has a problem of-978295d47e9f6e061fb91fa97505913b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Anorexia"
)

define "Problem has status of-58dd8971115903037a9858f4621ae1f1":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-786273c784d1f5325135ebca920367a8":
   "Problem has status of-58dd8971115903037a9858f4621ae1f1"
   and ( "Patient has a problem of-978295d47e9f6e061fb91fa97505913b")

define "Patient has lab result with test name (specific for an organism or substance)-d2eb80ab887cf1fb315e9746ebbd24c9":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "S. Paratyphi Infection (Tests for S. Paratyphi Antibody)"
)

define "Lab Result Value (ordinal)-ea4afd62d9b703c596317fa2f2c177ea":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-de2ce70b1a7d130b6f8b1dfb48ddf1b6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "S. Paratyphi Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-9c315f6c4e2be78462fd5459fd1b88b6":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Lab Result Value (ordinal)-e8b5303b5fb6a52bde7374a82eb72e24":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-fe5d03e040d1238450cf80651de49805":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-1683a69d7b7a35a1c4182c6a46928376":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-d7a9fd3ff81648008fdcb8302653d087":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "S. Paratyphi Infection (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-35fb4cb1fc3c72f13046203b4c9938fc":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Diarrhea"
)

define "Patient has a problem of-ede3e4595bf6b2e32d884816280bb66b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Diarrhea"
)

define "Problem has status of-f7c2c02d99e1b07ac22bbcebf596e196":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-f64529a880de3108cefa5e73d4e4af91":
   "Problem has status of-f7c2c02d99e1b07ac22bbcebf596e196"
   and ( "Patient has a problem of-ede3e4595bf6b2e32d884816280bb66b")

define "Lab Result Value (ordinal)-2bb714cb795c50e2584761cf5e6bef25":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-fa18a479e8807968b16385c5fb657a6b":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-c8ced612b8191f52b5422b1af66c8064":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-3c78db45fe523ec331fc03838aacc2d5":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "S. Paratyphi Infection (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-ebd6fad02bef2d09fff48081ee206d95":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "S. Paratyphi Infection (Disorders)"
)

define "Patient has a problem of-4a9dced6728b8d0c9477bca32a82b1fe":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "S. Paratyphi Infection (Disorders)"
)

define "Problem has status of-5ef0ebc97e2e4529accddec34850d83e":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-542d22a75f97d480a7f3c4fabd929dea":
   "Problem has status of-5ef0ebc97e2e4529accddec34850d83e"
   and ( "Patient has a problem of-4a9dced6728b8d0c9477bca32a82b1fe")

define "ConditionCriteriaMet":
   "Group 1-542d22a75f97d480a7f3c4fabd929dea"
   or ( "Patient has a diagnosis of-ebd6fad02bef2d09fff48081ee206d95")
   or ( "Lab Result Value (nominal)-3c78db45fe523ec331fc03838aacc2d5")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-c8ced612b8191f52b5422b1af66c8064")
   or ( "Lab Result Interpretation-fa18a479e8807968b16385c5fb657a6b")
   or ( "Lab Result Value (ordinal)-2bb714cb795c50e2584761cf5e6bef25")
   and ( "Group 1-f64529a880de3108cefa5e73d4e4af91")
   or ( "Patient has a diagnosis of-35fb4cb1fc3c72f13046203b4c9938fc")
   or ( "Lab Result Value (nominal)-d7a9fd3ff81648008fdcb8302653d087")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-1683a69d7b7a35a1c4182c6a46928376")
   or ( "Lab Result Interpretation-fe5d03e040d1238450cf80651de49805")
   or ( "Lab Result Value (ordinal)-e8b5303b5fb6a52bde7374a82eb72e24")
   or ( "Lab Result Interpretation-9c315f6c4e2be78462fd5459fd1b88b6")
   or ( "Lab Result Value (nominal)-de2ce70b1a7d130b6f8b1dfb48ddf1b6")
   or ( "Lab Result Value (ordinal)-ea4afd62d9b703c596317fa2f2c177ea")
   and ( "Patient has lab result with test name (specific for an organism or substance)-d2eb80ab887cf1fb315e9746ebbd24c9")
   and ( "Group 1-786273c784d1f5325135ebca920367a8")
   or ( "Patient has a diagnosis of-595127c6a158871187807214544237d5")
   and ( "Group 1-568d63abad16a2a67939ef9dc450ed25")
   or ( "Patient has a diagnosis of-200ef5c1608f3135d02e2b62900b05c6")
