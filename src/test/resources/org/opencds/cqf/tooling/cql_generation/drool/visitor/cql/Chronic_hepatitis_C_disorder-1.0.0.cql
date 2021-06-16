library Chronic_hepatitis_C_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Hepatitis C Virus Infection (Tests for hepatitis C virus Antibody)" : 'https://hln.com/fhir/ValueSet/HCV005'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Hepatitis C Virus Infection (Tests for hepatitis C virus Antigen)" : 'https://hln.com/fhir/ValueSet/HCV004'
valueset "Hepatitis C Virus Infection (Tests for hepatitis C virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/HCV003'
valueset "Hepatitis C Virus Infection (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/HCV001'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Hepatitis C Virus Infection (Disorders)" : 'https://hln.com/fhir/ValueSet/HCV002'
valueset "Indeterminate or Equivocal Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT011'
valueset "Negative or Undetected Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT012'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-b481d66f26d02748db992306b3f782a3":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Antibody)"
)

define "Lab Result Value (ordinal)-2ae905a56c5a50243263f85fe576c225":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-fffba914c83879e9a5fbb46b4373a867":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-5a1ba399bf0666cbb38febe44fd88f8e":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Antigen)"
)

define "Lab Result Value (ordinal)-de552532c3844868df5b392615261d51":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-a9e0af8282bcfd505e8150f5783b75c0":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-a6ca23e387b49acdd343dd326133489d":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-d32d5cb74ff9434167edf35ac03db667":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-4ac72b5347ac2a30b31b3c19e8f38142":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-d2ce1a40d5615ff5ac803ff7c17472f8":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Antibody)"
)

define "Lab Result Value (ordinal)-1f89deb16439bcb8d3b6497fe893a2fc":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-12a888c3f2187e1072d67a525d16012b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-352105c364251ca7e1e83c3eaff10a12":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-726d258060b2995732f8d4a7dc70732c":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Antigen)"
)

define "Lab Result Value (ordinal)-5827e773c5ab64ef3f65c1950a512fbe":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-b813c8006acbcbff7c1fc943736883c4":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-8de29ebeca86466b133b549fab1594a1":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-1eb1f4e0509e86b6a89423412dba76da":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-b524b38cbe6fd75faf1b1516e7f25c00":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-53c93d7873931e6976ae6d6766887477":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-de207ba1aaff6218674cd4ebd27b2829":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-fa1255e5212439dcde55d39bbe79785d":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-657b59154435c5fe541acf3cb1796ca7":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-2dc8415725af446026d427eeb243a4af":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Disorders)"
)

define "Patient has a problem of-22625fdf978267746f5b28e3d7332759":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Disorders)"
)

define "Problem has status of-6fd4b327b1bd61c86e4d072b4011fa23":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-f808b7a8e6851d9bde8679a8824623b1":
   "Problem has status of-6fd4b327b1bd61c86e4d072b4011fa23"
   and ( "Patient has a problem of-22625fdf978267746f5b28e3d7332759")

define "Patient has lab result with test name (specific for an organism or substance)-3749ef34cedb77e0cd5cde1d76a912f3":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Antigen)"
)

define "Lab Result Value (ordinal)-a160d9028554c3924b2be6cd9ff23321":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Indeterminate or Equivocal Lab Result Value"
)

define "Lab Result Interpretation-9ac3719ba8053740aec096377d7b29be":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-316a1211ad3f0cbf464a84ba145c1028":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-394e88c020919cda1331f29f40f18a05":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Indeterminate or Equivocal Lab Result Value"
)

define "Lab Result Interpretation-a3484a1b2a62d6e68cf0fae2eae3ca0f":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-307a499912f58cca5b062ccca3e12470":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Antibody)"
)

define "Lab Result Value (ordinal)-f1273519cdc4cd85746573bb05422d2e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Indeterminate or Equivocal Lab Result Value"
)

define "Lab Result Interpretation-a68cec50e75e1cbebca5ab9be3e76d35":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-8faf8f0cf332fa03dc93f83bc9dbfc9e":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Antibody)"
)

define "Lab result value of (ordinal)-2e8704bc6fdb200dacc87edfb4a19b43":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Patient has lab result with test name (specific for an organism or substance)-fcc058542b96c720ff1967f5842c8442":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Antigen)"
)

define "Lab result value of (ordinal)-d6a813ee77b92376b16a79da379a7018":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Patient has lab result with test name (specific for an organism or substance)-96b965415ccdc1dc6e8eecf9783dd226":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hepatitis C Virus Infection (Tests for hepatitis C virus Nucleic Acid)"
)

define "Lab result value of (ordinal)-2f28b0ea75979e7b1885eff6c81939b4":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "ConditionCriteriaMet":
   "Lab result value of (ordinal)-2f28b0ea75979e7b1885eff6c81939b4"
   and ( "Patient has lab result with test name (specific for an organism or substance)-96b965415ccdc1dc6e8eecf9783dd226")
   and ( "Lab result value of (ordinal)-d6a813ee77b92376b16a79da379a7018")
   and ( "Patient has lab result with test name (specific for an organism or substance)-fcc058542b96c720ff1967f5842c8442")
   and ( "Lab result value of (ordinal)-2e8704bc6fdb200dacc87edfb4a19b43")
   and ( "Patient has lab result with test name (specific for an organism or substance)-8faf8f0cf332fa03dc93f83bc9dbfc9e")
   or ( "Lab Result Interpretation-a68cec50e75e1cbebca5ab9be3e76d35")
   or ( "Lab Result Value (ordinal)-f1273519cdc4cd85746573bb05422d2e")
   and ( "Patient has lab result with test name (specific for an organism or substance)-307a499912f58cca5b062ccca3e12470")
   or ( "Lab Result Interpretation-a3484a1b2a62d6e68cf0fae2eae3ca0f")
   or ( "Lab Result Value (ordinal)-394e88c020919cda1331f29f40f18a05")
   and ( "Patient has lab result with test name (specific for an organism or substance)-316a1211ad3f0cbf464a84ba145c1028")
   or ( "Lab Result Interpretation-9ac3719ba8053740aec096377d7b29be")
   or ( "Lab Result Value (ordinal)-a160d9028554c3924b2be6cd9ff23321")
   and ( "Patient has lab result with test name (specific for an organism or substance)-3749ef34cedb77e0cd5cde1d76a912f3")
   and ( "Group 1-f808b7a8e6851d9bde8679a8824623b1")
   or ( "Patient has a diagnosis of-2dc8415725af446026d427eeb243a4af")
   or ( "Lab Result Value (nominal)-657b59154435c5fe541acf3cb1796ca7")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-fa1255e5212439dcde55d39bbe79785d")
   or ( "Lab Result Interpretation-de207ba1aaff6218674cd4ebd27b2829")
   or ( "Lab Result Value (nominal)-53c93d7873931e6976ae6d6766887477")
   or ( "Lab Result Value (ordinal)-b524b38cbe6fd75faf1b1516e7f25c00")
   and ( "Patient has lab result with test name (specific for an organism or substance)-1eb1f4e0509e86b6a89423412dba76da")
   or ( "Lab Result Interpretation-8de29ebeca86466b133b549fab1594a1")
   or ( "Lab Result Value (nominal)-b813c8006acbcbff7c1fc943736883c4")
   or ( "Lab Result Value (ordinal)-5827e773c5ab64ef3f65c1950a512fbe")
   and ( "Patient has lab result with test name (specific for an organism or substance)-726d258060b2995732f8d4a7dc70732c")
   or ( "Lab Result Interpretation-352105c364251ca7e1e83c3eaff10a12")
   or ( "Lab Result Value (nominal)-12a888c3f2187e1072d67a525d16012b")
   or ( "Lab Result Value (ordinal)-1f89deb16439bcb8d3b6497fe893a2fc")
   and ( "Patient has lab result with test name (specific for an organism or substance)-d2ce1a40d5615ff5ac803ff7c17472f8")
   or ( "Lab Result Interpretation-4ac72b5347ac2a30b31b3c19e8f38142")
   or ( "Lab Result Value (ordinal)-d32d5cb74ff9434167edf35ac03db667")
   and ( "Patient has lab result with test name (specific for an organism or substance)-a6ca23e387b49acdd343dd326133489d")
   or ( "Lab Result Interpretation-a9e0af8282bcfd505e8150f5783b75c0")
   or ( "Lab Result Value (ordinal)-de552532c3844868df5b392615261d51")
   and ( "Patient has lab result with test name (specific for an organism or substance)-5a1ba399bf0666cbb38febe44fd88f8e")
   or ( "Lab Result Interpretation-fffba914c83879e9a5fbb46b4373a867")
   or ( "Lab Result Value (ordinal)-2ae905a56c5a50243263f85fe576c225")
   and ( "Patient has lab result with test name (specific for an organism or substance)-b481d66f26d02748db992306b3f782a3")
