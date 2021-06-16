library Disease_caused_by_Hantavirus_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Acute Renal Failure [Unspecified Cause]" : 'https://hln.com/fhir/ValueSet/SYMP070'
valueset "Acute Respiratory Distress Syndrome (ARDS)" : 'https://hln.com/fhir/ValueSet/SYMP057'
valueset "Acute Pulmonary Edema [Unspecified Cause]" : 'https://hln.com/fhir/ValueSet/SYMP072'
valueset "Hantavirus Infection (Tests for Hantavirus Antigen)" : 'https://hln.com/fhir/ValueSet/HAN004'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Hantavirus Infection (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/HAN001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Hantavirus Infection (Tests for Hantavirus IgM IgG Total Antibody)" : 'https://hln.com/fhir/ValueSet/HAN005'
valueset "Hantavirus Infection (Tests for Hantavirus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/HAN003'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Fever" : 'https://hln.com/fhir/ValueSet/SYMP007'
valueset "Hantavirus Infection and Diseases (Disorders)" : 'https://hln.com/fhir/ValueSet/HAN002'
valueset "Acute respiratory illness" : 'https://hln.com/fhir/ValueSet/SYMP071'
valueset "Thrombocytopenia (Tests for Platelets [#/volume] in Blood) " : 'https://hln.com/fhir/ValueSet/VHF008'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-1eb6482b450b6fbc5694265bc5db062d":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Acute Renal Failure [Unspecified Cause]"
)

define "Patient has a problem of-6464af6cc1cedbc9e6de96e8f546205b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Acute Renal Failure [Unspecified Cause]"
)

define "Problem has status of-32da6b0dfe2b90d06c94eb823cae2931":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-37793dd8d21f3726d622edda36905bf5":
   "Problem has status of-32da6b0dfe2b90d06c94eb823cae2931"
   and ( "Patient has a problem of-6464af6cc1cedbc9e6de96e8f546205b")

define "Patient has a diagnosis of-c32f1bd28716a711f5a123b023aabdbb":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Acute Respiratory Distress Syndrome (ARDS)"
)

define "Patient has a problem of-a9347afa90be53839fee6325c57d9119":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Acute Respiratory Distress Syndrome (ARDS)"
)

define "Problem has status of-713c1fe53249178ea660937c20e30e30":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-2065438ddadbc2604c12d12199074489":
   "Problem has status of-713c1fe53249178ea660937c20e30e30"
   and ( "Patient has a problem of-a9347afa90be53839fee6325c57d9119")

define "Patient has a diagnosis of-beeb90df1b5b8f713dc3c8d1513d1874":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Acute Pulmonary Edema [Unspecified Cause]"
)

define "Patient has a problem of-d85a31a792e7f744dffe02623f3858f0":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Acute Pulmonary Edema [Unspecified Cause]"
)

define "Problem has status of-93521d7fe2c9cf04420f9057bdf28e50":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-66101e563578b3f0606024fb6ae3b538":
   "Problem has status of-93521d7fe2c9cf04420f9057bdf28e50"
   and ( "Patient has a problem of-d85a31a792e7f744dffe02623f3858f0")

define "Patient has lab result with test name (specific for an organism or substance)-9bcc06e09b86dd6962dd3c318c04a5cf":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hantavirus Infection (Tests for Hantavirus Antigen)"
)

define "Lab Result Value (ordinal)-b3cd8a0452862d374dc641797faeb881":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-55ba3748b80133f3f9d68a5de57d088b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Hantavirus Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-cb07a64d3b1a199bc3421f3b219ed26d":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-5e75967ae0cc17f438483aab7c6aa852":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hantavirus Infection (Tests for Hantavirus IgM IgG Total Antibody)"
)

define "Lab Result Value (ordinal)-a24086033d00a7f9361e2aad1a5ef51f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-f97682ab9ab5645b49eff97842165e6f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Hantavirus Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-b518df00c4f9098e6a9f5b34c8f61348":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-3243b3e5d1d8297cd0e4f27d5f529d06":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Hantavirus Infection (Tests for Hantavirus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-59d4a75843314a426d4c6c2eaf0366be":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-513b99b8e8b6497f1b888821a7872989":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Hantavirus Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-319ab6047674219759dc68db7299170c":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-222710aa4de3cc8adfab4f453a5bf1dd":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-bb239de5793bb9645c69522d998f6401":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Hantavirus Infection (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-83ec8948dc92ceaeb4965dbb7a309060":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Fever"
)

define "Patient has a problem of-4420c555b6c4e97d9e555732d4843c3f":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Fever"
)

define "Problem has status of-9e149b601722f403eaedf64ec780de54":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-2c688d2ccefd974afd47c9cff81e6c6d":
   "Problem has status of-9e149b601722f403eaedf64ec780de54"
   and ( "Patient has a problem of-4420c555b6c4e97d9e555732d4843c3f")

define "Patient has a diagnosis of-7fb2b8142ffb61dee8dd487fca588dc8":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Hantavirus Infection and Diseases (Disorders)"
)

define "Patient has a problem of-6506753a96e22c395580cff062ed8bb4":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Hantavirus Infection and Diseases (Disorders)"
)

define "Problem has status of-7bb96c784db1f41bac94518c3c83e338":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-eccb4445c2b68820da3e8f8f45a9de43":
   "Problem has status of-7bb96c784db1f41bac94518c3c83e338"
   and ( "Patient has a problem of-6506753a96e22c395580cff062ed8bb4")

define "Patient has a lab test order with test name-d6148c0fd88be242f4d32c58c6c4b734":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Hantavirus Infection (Tests for Hantavirus Antigen)"
)

define "Patient has a lab test order with test name-5ba59f136d6b081891d5cc51efba7a03":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Hantavirus Infection (Tests for Hantavirus Nucleic Acid)"
)

define "Patient has a lab test order with test name-b941ca0fbea10e10b193dffa5d021d61":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Hantavirus Infection (Tests for Hantavirus IgM IgG Total Antibody)"
)

define "Patient has a diagnosis of-69f9052caf7bf64e1fc1d6a97734ee2d":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Acute respiratory illness"
)

define "Patient has a problem of-4583094728d8b4f0badaf58da263c0a1":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Acute respiratory illness"
)

define "Problem has status of-4221f334821d6fd64ef992a2133b5a96":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-939b1d51c9e7b01e28862e66e21d24b9":
   "Problem has status of-4221f334821d6fd64ef992a2133b5a96"
   and ( "Patient has a problem of-4583094728d8b4f0badaf58da263c0a1")

define "Patient has lab result with test name (specific or an organism or substance)-3f12c73cbe886ab3f908544bb1ca8266":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Thrombocytopenia (Tests for Platelets [#/volume] in Blood) "
)

define "Lab Result Value (quantitative)-9da45e8eee5fa68c6a32bcaebfa1dbd2":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias < ToQuantity(150000.0)150000.0
)

define "Lab Result Value (ordinal)-8967fdeb8531c69f9d8026eb13ca3a81":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-459dc3e6db577f49f4221697c63ffec9":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Group 1.1-d5acd129f47353d9de23d4f5c54c4821":
   "Lab Result Interpretation-459dc3e6db577f49f4221697c63ffec9"
   or ( "Lab Result Value (ordinal)-8967fdeb8531c69f9d8026eb13ca3a81")
   or ( "Lab Result Value (quantitative)-9da45e8eee5fa68c6a32bcaebfa1dbd2")
   and ( "Patient has lab result with test name (specific or an organism or substance)-3f12c73cbe886ab3f908544bb1ca8266")
   and ( "Group 1-939b1d51c9e7b01e28862e66e21d24b9")

define "Group 1-b2ec8fd8ef385f444209414cdd4f0bad":
   "Group 1.1-d5acd129f47353d9de23d4f5c54c4821"
   or ( "Patient has a diagnosis of-69f9052caf7bf64e1fc1d6a97734ee2d")

define "ConditionCriteriaMet":
   "Group 1-b2ec8fd8ef385f444209414cdd4f0bad"
   or ( "Patient has a lab test order with test name-b941ca0fbea10e10b193dffa5d021d61")
   or ( "Patient has a lab test order with test name-5ba59f136d6b081891d5cc51efba7a03")
   or ( "Patient has a lab test order with test name-d6148c0fd88be242f4d32c58c6c4b734")
   and ( "Group 1-eccb4445c2b68820da3e8f8f45a9de43")
   or ( "Patient has a diagnosis of-7fb2b8142ffb61dee8dd487fca588dc8")
   and ( "Group 1-2c688d2ccefd974afd47c9cff81e6c6d")
   or ( "Patient has a diagnosis of-83ec8948dc92ceaeb4965dbb7a309060")
   or ( "Lab Result Value (nominal)-bb239de5793bb9645c69522d998f6401")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-222710aa4de3cc8adfab4f453a5bf1dd")
   or ( "Lab Result Interpretation-319ab6047674219759dc68db7299170c")
   or ( "Lab Result Value (nominal)-513b99b8e8b6497f1b888821a7872989")
   or ( "Lab Result Value (ordinal)-59d4a75843314a426d4c6c2eaf0366be")
   and ( "Patient has lab result with test name (specific for an organism or substance)-3243b3e5d1d8297cd0e4f27d5f529d06")
   or ( "Lab Result Interpretation-b518df00c4f9098e6a9f5b34c8f61348")
   or ( "Lab Result Value (nominal)-f97682ab9ab5645b49eff97842165e6f")
   or ( "Lab Result Value (ordinal)-a24086033d00a7f9361e2aad1a5ef51f")
   and ( "Patient has lab result with test name (specific for an organism or substance)-5e75967ae0cc17f438483aab7c6aa852")
   or ( "Lab Result Interpretation-cb07a64d3b1a199bc3421f3b219ed26d")
   or ( "Lab Result Value (nominal)-55ba3748b80133f3f9d68a5de57d088b")
   or ( "Lab Result Value (ordinal)-b3cd8a0452862d374dc641797faeb881")
   and ( "Patient has lab result with test name (specific for an organism or substance)-9bcc06e09b86dd6962dd3c318c04a5cf")
   and ( "Group 1-66101e563578b3f0606024fb6ae3b538")
   or ( "Patient has a diagnosis of-beeb90df1b5b8f713dc3c8d1513d1874")
   and ( "Group 1-2065438ddadbc2604c12d12199074489")
   or ( "Patient has a diagnosis of-c32f1bd28716a711f5a123b023aabdbb")
   and ( "Group 1-37793dd8d21f3726d622edda36905bf5")
   or ( "Patient has a diagnosis of-1eb6482b450b6fbc5694265bc5db062d")
