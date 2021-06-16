library Pertussis_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Apnea" : 'https://hln.com/fhir/ValueSet/SYMP001'
valueset "Cough" : 'https://hln.com/fhir/ValueSet/SYMP002'
valueset "Post-tussive vomiting" : 'https://hln.com/fhir/ValueSet/SYMP030'
valueset "Pertussis (Tests for Bordetella pertussis Antibody [Excluding Pertussis Toxin Antibody])" : 'https://hln.com/fhir/ValueSet/PER009'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Pertussis (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/PER001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Pertussis (Tests for Bordetella pertussis Antigen)" : 'https://hln.com/fhir/ValueSet/PER006'
valueset "Pertussis (Tests for Bordetella pertussis Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/PER005'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Pertussis (Tests for Bordetella pertussis Toxin Antibody)" : 'https://hln.com/fhir/ValueSet/PER007'
valueset "Pertussis (Tests for Bordetella pertussis by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/PER004'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'
valueset "Inspiratory Whoop" : 'https://hln.com/fhir/ValueSet/SYMP028'
valueset "Pertussis (Test Panels for Bordetella pertussis and Bordetella parapertussis Nucleic Acid and Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/PER010'
valueset "Pertussis (Test Panels for Bordetella pertussis Antibody)" : 'https://hln.com/fhir/ValueSet/PER011'
valueset "Pertussis (Test Panels for Bordetella pertussis Toxin Antibody)" : 'https://hln.com/fhir/ValueSet/PER002'
valueset "Paroxysmal Cough" : 'https://hln.com/fhir/ValueSet/SYMP029'
valueset "Pertussis (Disorders)" : 'https://hln.com/fhir/ValueSet/PER003'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Units:-30b4e4b89118c9722bdc353b5a78e5d4":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) < 18 'months'

define "Units:-dbc843ec4ff74f18ccdf1e8ad2e88b78":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) < 1 'years'

define "Units:-46cec6804849c065404252a683be69bd":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) <= 2 'years'

define "Patient has a diagnosis of-c6a3dc83fd196e3d48bc42d123b418a1":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Apnea"
)

define "Patient has a problem of-cc2eff712ea08a9eef5440356cec34f4":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Apnea"
)

define "Problem has status of-ca41269999443b54f24e1ecc947f381b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-305bb849c27a2df46f67c540deb3428d":
   "Problem has status of-ca41269999443b54f24e1ecc947f381b"
   and ( "Patient has a problem of-cc2eff712ea08a9eef5440356cec34f4")

define "Patient has a diagnosis of-c347daa324e0914fbb7ad946f33d6d34":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Post-tussive vomiting"
)

define "Patient has a problem of-f257cb6e59a6a6c432e852865fca0c40":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Post-tussive vomiting"
)

define "Problem has status of-e8a73d096ed08e629d339adabfc85b1c":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-9a8792ffcd8de2ea5a7f2a99b3342c90":
   "Problem has status of-e8a73d096ed08e629d339adabfc85b1c"
   and ( "Patient has a problem of-f257cb6e59a6a6c432e852865fca0c40")

define "Patient has lab result with test name (specific for an organism or substance)-5d4cf8e9e2f4039d7f176e671ce862fe":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Pertussis (Tests for Bordetella pertussis Antibody [Excluding Pertussis Toxin Antibody])"
)

define "Lab Result Value (ordinal)-407ad4d8470e12fb7745e1767fa6958a":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-a3782e944dbe18bad728d3f6ebe907dd":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Pertussis (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-9f6fc12b4250bfcc2e168ced06dbf4c9":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-eab7ad33e3eece5f96504d47b72882f2":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Pertussis (Tests for Bordetella pertussis Antigen)"
)

define "Lab Result Value (ordinal)-300cd1be1ca126adde2df954f97f17e2":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-670cce2eff34f3545f09528660bc2e08":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Pertussis (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-197a6a5741e780c17574df7d75d9f94b":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-1d66553bcd042af86e55ae726ee8c304":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Pertussis (Tests for Bordetella pertussis Nucleic Acid)"
)

define "Lab Result Value (ordinal)-faf26a28f68c93fbfd44699e997aa569":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-94a4a903c46ca8d800df6c17d0f86d91":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Pertussis (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-c9b57a046a30ae658f125e8ab7ecd945":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-3bd7aa877ae66ef2e1caf6876c0b1f4b":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-d1e5326b3f3921b24aaf4eaa8c8e9f0f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Pertussis (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-2f25df42ce94b971bf4c4309917c4d2c":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Pertussis (Tests for Bordetella pertussis Toxin Antibody)"
)

define "Lab Result Value (ordinal)-f8bd97c76a761c2d83a3e7cdd705e823":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-927f33219f0d2fa1ac975ad730d2c4aa":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Pertussis (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-d065f60c9887235a1c25dd863591b49e":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-cb8ec59d2ad6607aac46167de7a90848":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Pertussis (Tests for Bordetella pertussis by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-11d4efae28bbce1b28769524aa030755":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-b59fc57b62a8bbabbf901f6e3a7d34d8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Pertussis (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-93859e58f46dbcf231868ec76ee4d72b":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-38c86c31c925611e2f1649519e02a776":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-e580168fba19b25937347a4662dd8127":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Pertussis (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-db5bd984d51a5855d960bdcbfa32e78b":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Inspiratory Whoop"
)

define "Patient has a problem of-97adca81a88e1e5ee70f9b5d46749ccc":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Inspiratory Whoop"
)

define "Problem has status of-90db146249dbb428e3e1ab61fa4323ee":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-116f560bfcc8c55650fb4d8f4f5dbd68":
   "Problem has status of-90db146249dbb428e3e1ab61fa4323ee"
   and ( "Patient has a problem of-97adca81a88e1e5ee70f9b5d46749ccc")

define "Patient has a lab test order with test name-c0433e47ba8c5f29ff3a84c25af8408d":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Pertussis (Tests for Bordetella pertussis by Culture and Identification Method)"
)

define "Patient has a lab test order with test name-da9d7ea91b2f9abac25910795ca263f7":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Pertussis (Tests for Bordetella pertussis Nucleic Acid)"
)

define "Patient has a lab test order with test name-d15e8e0dc77a7d91546038e39b5b8413":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Pertussis (Test Panels for Bordetella pertussis and Bordetella parapertussis Nucleic Acid and Culture and Identification Method)"
)

define "Patient has a lab test order with test name-5a1ae25725113ee04b95d90a3f47ebb9":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Pertussis (Tests for Bordetella pertussis Antibody [Excluding Pertussis Toxin Antibody])"
)

define "Patient has a lab test order with test name-f915ad3ddc1d68f58a154b02c089512d":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Pertussis (Test Panels for Bordetella pertussis Antibody)"
)

define "Patient has a lab test order with test name-c1d53a34d8d1c1adf7a4a31fccb9fab1":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Pertussis (Tests for Bordetella pertussis Antigen)"
)

define "Patient has a lab test order with test name-46b16915ac129ba4212b24c21b31c2b4":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Pertussis (Tests for Bordetella pertussis Toxin Antibody)"
)

define "Patient has a lab test order with test name-7c14abfd6e471bc46e0b7c2dab36419e":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Pertussis (Test Panels for Bordetella pertussis Toxin Antibody)"
)

define "Patient has a diagnosis of-ea4bc07e811834c9464c7c99630975ea":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Paroxysmal Cough"
)

define "Patient has a problem of-92ebb07af4d6290d48aa081cbe49e9df":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Paroxysmal Cough"
)

define "Problem has status of-a7b35a229fdfc45794a1dd982c970bd2":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-efca0d14cf253113c9e1580476c4a829":
   "Problem has status of-a7b35a229fdfc45794a1dd982c970bd2"
   and ( "Patient has a problem of-92ebb07af4d6290d48aa081cbe49e9df")

define "Patient has a diagnosis of-477ee978e4f6070261d6837c2e88ef97":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Pertussis (Disorders)"
)

define "Patient has a problem of-4f3d9205c93788ee52761543db084b9e":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Pertussis (Disorders)"
)

define "Problem has status of-20896cb09c52e45da046b5aa66a353f0":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-f95cf6bcf3f12c737ca93791e7148f8b":
   "Problem has status of-20896cb09c52e45da046b5aa66a353f0"
   and ( "Patient has a problem of-4f3d9205c93788ee52761543db084b9e")

define "Patient has a diagnosis of-4f1a77d0278fc5a580eb7687f7437d61":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Post-tussive vomiting"
)

define "Patient has a problem of-913a4f7b8c55a9878e42ea699aa28768":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Post-tussive vomiting"
)

define "Problem has status of-e25e9ce9ef1830e015b1ab689ce49999":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-506db9ad85d550dcfa2add334fe97204":
   "Problem has status of-e25e9ce9ef1830e015b1ab689ce49999"
   and ( "Patient has a problem of-913a4f7b8c55a9878e42ea699aa28768")

define "ConditionCriteriaMet":
   "Group 1-506db9ad85d550dcfa2add334fe97204"
   or ( "Patient has a diagnosis of-4f1a77d0278fc5a580eb7687f7437d61")
   and ( "Group 1-f95cf6bcf3f12c737ca93791e7148f8b")
   or ( "Patient has a diagnosis of-477ee978e4f6070261d6837c2e88ef97")
   and ( "Group 1-efca0d14cf253113c9e1580476c4a829")
   or ( "Patient has a diagnosis of-ea4bc07e811834c9464c7c99630975ea")
   or ( "Patient has a lab test order with test name-7c14abfd6e471bc46e0b7c2dab36419e")
   or ( "Patient has a lab test order with test name-46b16915ac129ba4212b24c21b31c2b4")
   or ( "Patient has a lab test order with test name-c1d53a34d8d1c1adf7a4a31fccb9fab1")
   or ( "Patient has a lab test order with test name-f915ad3ddc1d68f58a154b02c089512d")
   or ( "Patient has a lab test order with test name-5a1ae25725113ee04b95d90a3f47ebb9")
   or ( "Patient has a lab test order with test name-d15e8e0dc77a7d91546038e39b5b8413")
   or ( "Patient has a lab test order with test name-da9d7ea91b2f9abac25910795ca263f7")
   or ( "Patient has a lab test order with test name-c0433e47ba8c5f29ff3a84c25af8408d")
   and ( "Group 1-116f560bfcc8c55650fb4d8f4f5dbd68")
   or ( "Patient has a diagnosis of-db5bd984d51a5855d960bdcbfa32e78b")
   or ( "Lab Result Value (nominal)-e580168fba19b25937347a4662dd8127")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-38c86c31c925611e2f1649519e02a776")
   or ( "Lab Result Interpretation-93859e58f46dbcf231868ec76ee4d72b")
   or ( "Lab Result Value (nominal)-b59fc57b62a8bbabbf901f6e3a7d34d8")
   or ( "Lab Result Value (ordinal)-11d4efae28bbce1b28769524aa030755")
   and ( "Patient has lab result with test name (specific for an organism or substance)-cb8ec59d2ad6607aac46167de7a90848")
   or ( "Lab Result Interpretation-d065f60c9887235a1c25dd863591b49e")
   or ( "Lab Result Value (nominal)-927f33219f0d2fa1ac975ad730d2c4aa")
   or ( "Lab Result Value (ordinal)-f8bd97c76a761c2d83a3e7cdd705e823")
   and ( "Patient has lab result with test name (specific for an organism or substance)-2f25df42ce94b971bf4c4309917c4d2c")
   or ( "Lab Result Value (nominal)-d1e5326b3f3921b24aaf4eaa8c8e9f0f")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-3bd7aa877ae66ef2e1caf6876c0b1f4b")
   or ( "Lab Result Interpretation-c9b57a046a30ae658f125e8ab7ecd945")
   or ( "Lab Result Value (nominal)-94a4a903c46ca8d800df6c17d0f86d91")
   or ( "Lab Result Value (ordinal)-faf26a28f68c93fbfd44699e997aa569")
   and ( "Patient has lab result with test name (specific for an organism or substance)-1d66553bcd042af86e55ae726ee8c304")
   or ( "Lab Result Interpretation-197a6a5741e780c17574df7d75d9f94b")
   or ( "Lab Result Value (nominal)-670cce2eff34f3545f09528660bc2e08")
   or ( "Lab Result Value (ordinal)-300cd1be1ca126adde2df954f97f17e2")
   and ( "Patient has lab result with test name (specific for an organism or substance)-eab7ad33e3eece5f96504d47b72882f2")
   or ( "Lab Result Interpretation-9f6fc12b4250bfcc2e168ced06dbf4c9")
   or ( "Lab Result Value (nominal)-a3782e944dbe18bad728d3f6ebe907dd")
   or ( "Lab Result Value (ordinal)-407ad4d8470e12fb7745e1767fa6958a")
   and ( "Patient has lab result with test name (specific for an organism or substance)-5d4cf8e9e2f4039d7f176e671ce862fe")
   and ( "Group 1-9a8792ffcd8de2ea5a7f2a99b3342c90")
   or ( "Patient has a diagnosis of-c347daa324e0914fbb7ad946f33d6d34")
   and ( "Group 1-305bb849c27a2df46f67c540deb3428d")
   or ( "Patient has a diagnosis of-c6a3dc83fd196e3d48bc42d123b418a1")
   and ( "Units:-46cec6804849c065404252a683be69bd")
   and ( "Units:-dbc843ec4ff74f18ccdf1e8ad2e88b78")
   and ( "Units:-30b4e4b89118c9722bdc353b5a78e5d4")
