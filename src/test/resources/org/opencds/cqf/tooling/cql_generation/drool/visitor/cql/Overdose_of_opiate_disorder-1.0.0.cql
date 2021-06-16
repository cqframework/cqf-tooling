library Overdose_of_opiate_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Opioid Overdose and Poisoning (Naloxone) (RxNorm)" : 'https://hln.com/fhir/ValueSet/NFO004'
valueset "Bradypnea" : 'https://hln.com/fhir/ValueSet/SYMP091'
valueset "Cyanosis" : 'https://hln.com/fhir/ValueSet/SYMP093'
valueset "Glasgow Coma Scale Score < 13" : 'https://hln.com/fhir/ValueSet/SYMP096'
valueset "Glasgow Coma Scale Score < 12" : 'https://hln.com/fhir/ValueSet/SYMP095'
valueset "Consciousness [Decreased Level]" : 'https://hln.com/fhir/ValueSet/SYMP092'
valueset "Opioid Overdose and Poisoning  (Tests for Opioids [Qualitative])" : 'https://hln.com/fhir/ValueSet/NFO005'
valueset "Opioid Overdose and Poisoning  (Tests for Opioids [Quantitative])" : 'https://hln.com/fhir/ValueSet/NFO003'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Opioid Overdose and Poisoning (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/NFO001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Drugs [Unspecified] (Tests by Screen or Unspecified Method)" : 'https://hln.com/fhir/ValueSet/LabTST023'
valueset "Miosis" : 'https://hln.com/fhir/ValueSet/SYMP097'
valueset "Opioid Abuse, Dependence, and Other Use [Excluding Overdose and Poisoning] (Disorders)" : 'https://hln.com/fhir/ValueSet/NFO006'
valueset "Opioid Overdose and Poisoning (Disorders)" : 'https://hln.com/fhir/ValueSet/NFO002'
valueset "Drug Overdose and Poisoning [Suspected]" : 'https://hln.com/fhir/ValueSet/SYMP098'
valueset "[TEST] Therapeutic Medication Response" : 'https://hln.com/fhir/ValueSet/MED010'
valueset "Discharge Disposition [Expired]" : 'https://hln.com/fhir/ValueSet/ENC001'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient received administration of-2807764987eba9ef4d0c2c7b1fc1e816":
  exists (
    (( 
      [MedicationAdministration] M
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
    where Alias in "Opioid Overdose and Poisoning (Naloxone) (RxNorm)"
)

define "Patient has a diagnosis of-e19d6058143c2bc62b8076663c5b90a2":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Bradypnea"
)

define "Patient has a problem of-5a9ea50e341d6f61e83534c817b71bcf":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Bradypnea"
)

define "Problem has status of-388604ee5ac457fe4d769630d61d7d78":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-59a7c461c6355d1353c812a2caa80acd":
   "Problem has status of-388604ee5ac457fe4d769630d61d7d78"
   and ( "Patient has a problem of-5a9ea50e341d6f61e83534c817b71bcf")

define "Patient has a diagnosis of-7fdec32c95ec2769c856389c24728597":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Cyanosis"
)

define "Patient has a problem of-9365fabeffa91c2c6b358938771dbd9d":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Cyanosis"
)

define "Problem has status of-589b47ee3b2e04a2d54dd40279b266e5":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-c7e4e406cb9ba23eb5543bfe53defc38":
   "Problem has status of-589b47ee3b2e04a2d54dd40279b266e5"
   and ( "Patient has a problem of-9365fabeffa91c2c6b358938771dbd9d")

define "Patient has a diagnosis of-58b3bb9a9c108e237d8a06a9f1e286d2":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Consciousness [Decreased Level]"
)

define "Patient has a problem of-d9555d0c129b8995e1a25c44102d5b0b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Consciousness [Decreased Level]"
)

define "Problem has status of-a097118f428aa019e6c4e7e1baa18b02":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-3cd23a83c7d7443060313033bd12dc70":
   "Problem has status of-a097118f428aa019e6c4e7e1baa18b02"
   and ( "Patient has a problem of-d9555d0c129b8995e1a25c44102d5b0b")

define "Patient has lab result with test name (specific for an organism or substance)-6c772aaa6e69b7079380943c34efba99":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Opioid Overdose and Poisoning  (Tests for Opioids [Quantitative])"
)

define "Lab Result Value (ordinal)-1a4109a4353bd92d42367c7c550ba9ee":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-e9d236ac59b8a2f9f0b86b3b9b5e1b65":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Opioid Overdose and Poisoning (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-6b19d0fdfb2337ae3062638348352a66":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-64ff02087059b15f15a8a60c4a6e6141":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Drugs [Unspecified] (Tests by Screen or Unspecified Method)"
)

define "Lab Result Value (nominal)-383205e3b70f33ed468c0b48377276f4":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Opioid Overdose and Poisoning (Organism or Substance in Lab Results)"
)

define "Patient has a lab test order with test name-c3fde85c1ea0ae699644906048ac58a1":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Opioid Overdose and Poisoning  (Tests for Opioids [Quantitative])"
)

define "Patient has a lab test order with test name-6bcfcab365ed15ee5bdf8c83777c17af":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Opioid Overdose and Poisoning  (Tests for Opioids [Qualitative])"
)

define "Patient has a diagnosis of-ffcaae73d1f8ae1c7d2f677f97a42c45":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Miosis"
)

define "Patient has a problem of-2b524420520a034c387defd425707935":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Miosis"
)

define "Problem has status of-e307c5ff9917591beb878ae59f6df2ce":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-75734c3ae83ed496a857b306d572a2fe":
   "Problem has status of-e307c5ff9917591beb878ae59f6df2ce"
   and ( "Patient has a problem of-2b524420520a034c387defd425707935")

define "Patient has a diagnosis of-d0ebac7de85917d2f8138c0519a910ba":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Opioid Abuse, Dependence, and Other Use [Excluding Overdose and Poisoning] (Disorders)"
)

define "Patient has a problem of-6373d7e89bd5669188aec5099b13d5ed":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Opioid Abuse, Dependence, and Other Use [Excluding Overdose and Poisoning] (Disorders)"
)

define "Problem has status of-8e4f19af11968e533daa44474864d4ef":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-1e244922845c1ef53bdcceee20f4c46b":
   "Problem has status of-8e4f19af11968e533daa44474864d4ef"
   and ( "Patient has a problem of-6373d7e89bd5669188aec5099b13d5ed")

define "Patient has a diagnosis of-95f0e7e3c9a8053be5391f0cda72c4e8":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Opioid Overdose and Poisoning (Disorders)"
)

define "Patient has a problem of-108e9ffba3393e6bb65bf9b5364ba9d0":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Opioid Overdose and Poisoning (Disorders)"
)

define "Problem has status of-616e12e6bcb0ea415b38bde5c065eb96":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-5f97fba18ac77c1459b70537b0f50659":
   "Problem has status of-616e12e6bcb0ea415b38bde5c065eb96"
   and ( "Patient has a problem of-108e9ffba3393e6bb65bf9b5364ba9d0")

define "Patient has a diagnosis of-786c8308d2626d6046ec0d75c815057b":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Drug Overdose and Poisoning [Suspected]"
)

define "Patient has a problem of-e130d33eeb15255e71f91e9fcb521ebb":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Drug Overdose and Poisoning [Suspected]"
)

define "Problem has status of-4ddb8da22f13fe62952e4e21a5e21a44":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-7363122f9f650773df154c619dce4502":
   "Problem has status of-4ddb8da22f13fe62952e4e21a5e21a44"
   and ( "Patient has a problem of-e130d33eeb15255e71f91e9fcb521ebb")

define "Patient has medication administered with name of-ea6502a7451bfa7a6d4df67aa370bcb8":
  exists (
    (( 
      [MedicationAdministration] M
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
    where Alias in "Opioid Overdose and Poisoning (Naloxone) (RxNorm)"
)

define "Patient has therapeutic response to administered medication of-38479a6180d587c3f8b6ec61f55be7f3":
  exists (
    (( 
      flatten (
        [MedicationAdministration] M
          return M.reasonCode
      )
    ) 
    union ( 
      [MedicationAdministration] M
        let reasonConditions: [Condition: id in { { 
          ( M.reasonReference
        ) R
          return Last (
            Split ( R.reference, '/')
          ) } }]
        
        return reasonConditions.code
    ) 
  ) Alias
    where Alias in "[TEST] Therapeutic Medication Response"
)

define "Patient is deceased-5ac71d3e86103a3d45cb01f3497f5f01":
  exists (
    ([Patient] P
      return P.deceased as FHIR.boolean
  ) Alias
    where not  Equal( Alias, true)
)

define "Patient has a discharge disposition of-6ff8beb15ce0add65b41d080669c199b":
   not AnyInValueSet(
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) X
    return X,  "Discharge Disposition [Expired]")

define "ConditionCriteriaMet":
   "Patient has a discharge disposition of-6ff8beb15ce0add65b41d080669c199b"
   or ( "Patient is deceased-5ac71d3e86103a3d45cb01f3497f5f01")
   or ( "Patient has therapeutic response to administered medication of-38479a6180d587c3f8b6ec61f55be7f3")
   and ( "Patient has medication administered with name of-ea6502a7451bfa7a6d4df67aa370bcb8")
   and ( "Group 1-7363122f9f650773df154c619dce4502")
   or ( "Patient has a diagnosis of-786c8308d2626d6046ec0d75c815057b")
   and ( "Group 1-5f97fba18ac77c1459b70537b0f50659")
   or ( "Patient has a diagnosis of-95f0e7e3c9a8053be5391f0cda72c4e8")
   and ( "Group 1-1e244922845c1ef53bdcceee20f4c46b")
   or ( "Patient has a diagnosis of-d0ebac7de85917d2f8138c0519a910ba")
   and ( "Group 1-75734c3ae83ed496a857b306d572a2fe")
   or ( "Patient has a diagnosis of-ffcaae73d1f8ae1c7d2f677f97a42c45")
   or ( "Patient has a lab test order with test name-6bcfcab365ed15ee5bdf8c83777c17af")
   or ( "Patient has a lab test order with test name-c3fde85c1ea0ae699644906048ac58a1")
   or ( "Lab Result Value (nominal)-383205e3b70f33ed468c0b48377276f4")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-64ff02087059b15f15a8a60c4a6e6141")
   or ( "Lab Result Interpretation-6b19d0fdfb2337ae3062638348352a66")
   or ( "Lab Result Value (nominal)-e9d236ac59b8a2f9f0b86b3b9b5e1b65")
   or ( "Lab Result Value (ordinal)-1a4109a4353bd92d42367c7c550ba9ee")
   and ( "Patient has lab result with test name (specific for an organism or substance)-6c772aaa6e69b7079380943c34efba99")
   and ( "Group 1-3cd23a83c7d7443060313033bd12dc70")
   or ( "Patient has a diagnosis of-58b3bb9a9c108e237d8a06a9f1e286d2")
   and ( "Group 1-c7e4e406cb9ba23eb5543bfe53defc38")
   or ( "Patient has a diagnosis of-7fdec32c95ec2769c856389c24728597")
   and ( "Group 1-59a7c461c6355d1353c812a2caa80acd")
   or ( "Patient has a diagnosis of-e19d6058143c2bc62b8076663c5b90a2")
   and ( "Patient received administration of-2807764987eba9ef4d0c2c7b1fc1e816")
