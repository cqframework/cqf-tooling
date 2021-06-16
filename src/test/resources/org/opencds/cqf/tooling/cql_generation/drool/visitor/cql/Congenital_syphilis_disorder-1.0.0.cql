library Congenital_syphilis_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Syphilis [Congenital] (Disorders)" : 'https://hln.com/fhir/ValueSet/SYP006'
valueset "Syphilis (Tests for Treponema pallidum Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/SYP003'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Syphilis (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/SYP001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Treponema pallidum (Tests by Microscopic Observation by Immunofluorescence)" : 'https://hln.com/fhir/ValueSet/SYP005'
valueset "Organisms (Tests by Microscopic Observation)" : 'https://hln.com/fhir/ValueSet/LabTST008'
valueset "Organisms (Tests by Unspecified Method)" : 'https://hln.com/fhir/ValueSet/LabTST022'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-5ecb6e9e21a5a47bf6df231dea6b177a":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Syphilis [Congenital] (Disorders)"
)

define "Patient has a problem of-d55ee43d2d2346df2c4a18cf2fa5edc1":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Syphilis [Congenital] (Disorders)"
)

define "Problem has status of-4b87b9771d2f44831d95a3c9f51410a0":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-e8a1d8a35a714be8b4f0734df8c6b2b0":
   "Problem has status of-4b87b9771d2f44831d95a3c9f51410a0"
   and ( "Patient has a problem of-d55ee43d2d2346df2c4a18cf2fa5edc1")

define "Patient has lab result with test name (specific for an organism or substance)-bf80cdcdba514d8695d4c1fb94d18a38":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Syphilis (Tests for Treponema pallidum Nucleic Acid)"
)

define "Lab Result Value (ordinal)-893d7ddfd2fcb69c8b1ab5ac55f8e05e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-99f4e749544a8a512a4ece43efff65f9":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Syphilis (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-f09049ffb1f7444e158e3d6ca302e201":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-45db3479b3ddf5a9fa2786f3dd82b23b":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-e8ed041cd12e3ae91bab898684e22cf8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Syphilis (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-5d863950b1c831812b32faaa286deb98":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Treponema pallidum (Tests by Microscopic Observation by Immunofluorescence)"
)

define "Lab Result Value (ordinal)-1f352f53704af65885106b05ddb9147a":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-91ec07475c1493ce989a61ae5b9931eb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Syphilis (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-2336fc0621f16e2da582d611223fadd4":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-1b41dd254d1d7762f749fec862209a10":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests by Microscopic Observation)"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-bec93c523eb87e74bbb7126b06f5ba5a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests by Unspecified Method)"
)

define "Lab Result Value (nominal)-ed7e2f11c30a3f4cae9b77aae7a26cd6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Syphilis (Organism or Substance in Lab Results)"
)

define "Units:-42a7c4b92ed72bc561e12784aabfb9ee":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) < 28 'days'

define "ConditionCriteriaMet":
   "Units:-42a7c4b92ed72bc561e12784aabfb9ee"
   or ( "Lab Result Value (nominal)-ed7e2f11c30a3f4cae9b77aae7a26cd6")
   or ( "Patient has lab result with test name (NOT specific for an organism or substance)-bec93c523eb87e74bbb7126b06f5ba5a")
   or ( "Patient has lab result with test name (NOT specific for an organism or substance)-1b41dd254d1d7762f749fec862209a10")
   or ( "Lab Result Interpretation-2336fc0621f16e2da582d611223fadd4")
   or ( "Lab Result Value (nominal)-91ec07475c1493ce989a61ae5b9931eb")
   or ( "Lab Result Value (ordinal)-1f352f53704af65885106b05ddb9147a")
   and ( "Patient has lab result with test name (specific for an organism or substance)-5d863950b1c831812b32faaa286deb98")
   or ( "Lab Result Value (nominal)-e8ed041cd12e3ae91bab898684e22cf8")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-45db3479b3ddf5a9fa2786f3dd82b23b")
   or ( "Lab Result Interpretation-f09049ffb1f7444e158e3d6ca302e201")
   or ( "Lab Result Value (nominal)-99f4e749544a8a512a4ece43efff65f9")
   or ( "Lab Result Value (ordinal)-893d7ddfd2fcb69c8b1ab5ac55f8e05e")
   and ( "Patient has lab result with test name (specific for an organism or substance)-bf80cdcdba514d8695d4c1fb94d18a38")
   and ( "Group 1-e8a1d8a35a714be8b4f0734df8c6b2b0")
   or ( "Patient has a diagnosis of-5ecb6e9e21a5a47bf6df231dea6b177a")
