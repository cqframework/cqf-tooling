library Middle_East_respiratory_syndrome_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Acute Respiratory Distress Syndrome (ARDS)" : 'https://hln.com/fhir/ValueSet/SYMP057'
valueset "MERS (Tests for MERS_CoV Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/MERS003'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "MERS (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/MERS001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Fever" : 'https://hln.com/fhir/ValueSet/SYMP007'
valueset "MERS (Disorders)" : 'https://hln.com/fhir/ValueSet/MERS002'
valueset "Pneumonia" : 'https://hln.com/fhir/ValueSet/SYMP050'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-b58760a9dd0364dcdfbc78c9ffb7747d":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Acute Respiratory Distress Syndrome (ARDS)"
)

define "Patient has a problem of-11e487b2ba775e0aba65b540e68ff66f":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Acute Respiratory Distress Syndrome (ARDS)"
)

define "Problem has status of-7087b4673d2873d741b648f8f4811710":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-7f6162c5b81b8a95dcd6f2c7f4ea7efc":
   "Problem has status of-7087b4673d2873d741b648f8f4811710"
   and ( "Patient has a problem of-11e487b2ba775e0aba65b540e68ff66f")

define "Patient has lab result with test name (specific for an organism or substance)-57dbe76d5910fc9131933e359bf7d047":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "MERS (Tests for MERS_CoV Nucleic Acid)"
)

define "Lab Result Value (ordinal)-bbdae5cc07639a2feaff02ba75ccbe49":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-8e934af45f9e066db27f47450efae38f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "MERS (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-73a5f5949c73f07af073da125dae64e1":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-3685a1a3ef5e51147576e557c564a352":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-aa4a23b42b28f962365e1a28ff701a7e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "MERS (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-dd4a3de8a3e82c1328c7e31e987230a9":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Fever"
)

define "Patient has a problem of-e6864954d7636b3eddb8d3c561d7e72d":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Fever"
)

define "Problem has status of-95b211ccab93bbc945159a665abce81b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-2fb46334e439328c63a65aded0b66c55":
   "Problem has status of-95b211ccab93bbc945159a665abce81b"
   and ( "Patient has a problem of-e6864954d7636b3eddb8d3c561d7e72d")

define "Patient has a diagnosis of-155a7c39fcbea9e2d95e0a7d98aea49a":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "MERS (Disorders)"
)

define "Patient has a problem of-f37f559d5002066b142ee44de96ca374":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "MERS (Disorders)"
)

define "Problem has status of-6d8137b8de1117354a6c88b3c907adca":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-88c3caa8873d0dc43277e2a4b299e9a6":
   "Problem has status of-6d8137b8de1117354a6c88b3c907adca"
   and ( "Patient has a problem of-f37f559d5002066b142ee44de96ca374")

define "Patient has a diagnosis of-477f21e8673053fbf5f47ab333945879":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Pneumonia"
)

define "Patient has a problem of-341a0c624126e3924346ea798ceace68":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Pneumonia"
)

define "Problem has status of-6f77def921377a7ee5a3fc132817c0c1":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-7aa36fe878ac39195cee26abc7a1ff28":
   "Problem has status of-6f77def921377a7ee5a3fc132817c0c1"
   and ( "Patient has a problem of-341a0c624126e3924346ea798ceace68")

define "ConditionCriteriaMet":
   "Group 1-7aa36fe878ac39195cee26abc7a1ff28"
   or ( "Patient has a diagnosis of-477f21e8673053fbf5f47ab333945879")
   and ( "Group 1-88c3caa8873d0dc43277e2a4b299e9a6")
   or ( "Patient has a diagnosis of-155a7c39fcbea9e2d95e0a7d98aea49a")
   and ( "Group 1-2fb46334e439328c63a65aded0b66c55")
   or ( "Patient has a diagnosis of-dd4a3de8a3e82c1328c7e31e987230a9")
   or ( "Lab Result Value (nominal)-aa4a23b42b28f962365e1a28ff701a7e")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-3685a1a3ef5e51147576e557c564a352")
   or ( "Lab Result Interpretation-73a5f5949c73f07af073da125dae64e1")
   or ( "Lab Result Value (nominal)-8e934af45f9e066db27f47450efae38f")
   or ( "Lab Result Value (ordinal)-bbdae5cc07639a2feaff02ba75ccbe49")
   and ( "Patient has lab result with test name (specific for an organism or substance)-57dbe76d5910fc9131933e359bf7d047")
   and ( "Group 1-7f6162c5b81b8a95dcd6f2c7f4ea7efc")
   or ( "Patient has a diagnosis of-b58760a9dd0364dcdfbc78c9ffb7747d")
