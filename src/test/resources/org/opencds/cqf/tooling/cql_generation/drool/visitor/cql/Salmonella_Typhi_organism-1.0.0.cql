library Salmonella_Typhi_organism version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Abdominal Cramps" : 'https://hln.com/fhir/ValueSet/SYMP011'
valueset "Anorexia" : 'https://hln.com/fhir/ValueSet/SYMP015'
valueset "S. Typhi Infection (Tests for S. Typhi Antibody)" : 'https://hln.com/fhir/ValueSet/STI003'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "S. Typhi Infection (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/STI001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "S. Typhi Infection (Tests for S. Typhi Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/STI004'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Diarrhea" : 'https://hln.com/fhir/ValueSet/SYMP012'
valueset "S. Typhi Infection (Tests for S. Typhi by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/STI005'
valueset "Salmonellosis (Tests for Salmonella except S. typhi and S. paratyphi by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/SAL005'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'
valueset "S. Typhi Infection (Disorders)" : 'https://hln.com/fhir/ValueSet/STI002'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-63a816452dbfa5649772ad264359064b":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Abdominal Cramps"
)

define "Patient has a problem of-3e9e51797a76358df7381b648500238b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Abdominal Cramps"
)

define "Problem has status of-e6a2b5b87bb1da6937282e034d27a1ec":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-1d4c0e98dfb4a8e9c2cdaf43aaf0bea2":
   "Problem has status of-e6a2b5b87bb1da6937282e034d27a1ec"
   and ( "Patient has a problem of-3e9e51797a76358df7381b648500238b")

define "Patient has a diagnosis of-92e09dc6a56dc22c3d35107b34b19e74":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Anorexia"
)

define "Patient has a problem of-dd627a1a39486aa845dd9269816d1e97":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Anorexia"
)

define "Problem has status of-cc081d88166d1ab3b1b147c1dfc5af01":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-cd822d0894d407462e40ca8dd2976d1b":
   "Problem has status of-cc081d88166d1ab3b1b147c1dfc5af01"
   and ( "Patient has a problem of-dd627a1a39486aa845dd9269816d1e97")

define "Patient has lab result with test name (specific for an organism or substance)-5eac50bb595172815b989dad948ab9ac":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "S. Typhi Infection (Tests for S. Typhi Antibody)"
)

define "Lab Result Value (ordinal)-9dd0170357f9e82ffbd5200023f4d75a":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-c65b6f0c1a29b7754f0be48ceecb6825":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "S. Typhi Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-e5d7df8c646a514184c7ca55958a3c41":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-bd7e38302684fa9dabfc7c87400aa897":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "S. Typhi Infection (Tests for S. Typhi Nucleic Acid)"
)

define "Lab Result Value (ordinal)-a13cb243a6b7ad7bce3f7774d1f495e6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-c7c4fee2c9ae1a114242767138cf59bf":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "S. Typhi Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-ea094f859d53dad170d1149912fb116b":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-dc51bbf582ccd544e1d4af6394d34908":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-592ca46431917e63a01be233343e7247":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "S. Typhi Infection (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-99a7db537f7d13cd25cb8f71993a27cd":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Diarrhea"
)

define "Patient has a problem of-1b48cf5761e54b0bc7b292557de99a5c":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Diarrhea"
)

define "Problem has status of-9fb4339b685aa5c527ce6a2b2ed0a057":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-a2da5b06c2299dbcad140dd9c3048848":
   "Problem has status of-9fb4339b685aa5c527ce6a2b2ed0a057"
   and ( "Patient has a problem of-1b48cf5761e54b0bc7b292557de99a5c")

define "Patient has lab result with test name (specific for an organism or substance)-8e42937e2277b1dd3d7f0ab70d3613c1":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "S. Typhi Infection (Tests for S. Typhi by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-f5aa741d69814097e112882cc3f93830":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-2cfd1f6fbe5bed677bfb0d1e5f671c07":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "S. Typhi Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-4e2b1311daee0025af377f373b00c01e":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-b9d8b67092f75636ea21404758f1a4cd":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-eaa251a3957e98d4a8c8f01ad29afe95":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "S. Typhi Infection (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-27ca28e3d637dd3f89b9e937be53497b":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "S. Typhi Infection (Disorders)"
)

define "Patient has a problem of-fe614bde7ab28ec95f78a36d69de9857":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "S. Typhi Infection (Disorders)"
)

define "Problem has status of-9462bff4e9a545d9452c960d043b78ca":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-2e20f08a40266356d89e07f45c08a7f3":
   "Problem has status of-9462bff4e9a545d9452c960d043b78ca"
   and ( "Patient has a problem of-fe614bde7ab28ec95f78a36d69de9857")

define "ConditionCriteriaMet":
   "Group 1-2e20f08a40266356d89e07f45c08a7f3"
   or ( "Patient has a diagnosis of-27ca28e3d637dd3f89b9e937be53497b")
   or ( "Lab Result Value (nominal)-eaa251a3957e98d4a8c8f01ad29afe95")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-b9d8b67092f75636ea21404758f1a4cd")
   or ( "Lab Result Interpretation-4e2b1311daee0025af377f373b00c01e")
   or ( "Lab Result Value (nominal)-2cfd1f6fbe5bed677bfb0d1e5f671c07")
   or ( "Lab Result Value (ordinal)-f5aa741d69814097e112882cc3f93830")
   and ( "Patient has lab result with test name (specific for an organism or substance)-8e42937e2277b1dd3d7f0ab70d3613c1")
   and ( "Group 1-a2da5b06c2299dbcad140dd9c3048848")
   or ( "Patient has a diagnosis of-99a7db537f7d13cd25cb8f71993a27cd")
   or ( "Lab Result Value (nominal)-592ca46431917e63a01be233343e7247")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-dc51bbf582ccd544e1d4af6394d34908")
   or ( "Lab Result Interpretation-ea094f859d53dad170d1149912fb116b")
   or ( "Lab Result Value (nominal)-c7c4fee2c9ae1a114242767138cf59bf")
   or ( "Lab Result Value (ordinal)-a13cb243a6b7ad7bce3f7774d1f495e6")
   and ( "Patient has lab result with test name (specific for an organism or substance)-bd7e38302684fa9dabfc7c87400aa897")
   or ( "Lab Result Interpretation-e5d7df8c646a514184c7ca55958a3c41")
   or ( "Lab Result Value (nominal)-c65b6f0c1a29b7754f0be48ceecb6825")
   or ( "Lab Result Value (ordinal)-9dd0170357f9e82ffbd5200023f4d75a")
   and ( "Patient has lab result with test name (specific for an organism or substance)-5eac50bb595172815b989dad948ab9ac")
   and ( "Group 1-cd822d0894d407462e40ca8dd2976d1b")
   or ( "Patient has a diagnosis of-92e09dc6a56dc22c3d35107b34b19e74")
   and ( "Group 1-1d4c0e98dfb4a8e9c2cdaf43aaf0bea2")
   or ( "Patient has a diagnosis of-63a816452dbfa5649772ad264359064b")
