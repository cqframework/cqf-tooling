library Parkinson's_disease_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Carbidopa Levodopa Combination (RXNORM)" : 'https://hln.com/fhir/ValueSet/PAR005'
valueset "Levodopa (RXNORM)" : 'https://hln.com/fhir/ValueSet/PAR004'
valueset "Multiple System Atrophy" : 'https://hln.com/fhir/ValueSet/PAR003'
valueset "Parkinsons Disease (Disorders)" : 'https://hln.com/fhir/ValueSet/PAR001'
valueset "Parkinsonism [Secondary]" : 'https://hln.com/fhir/ValueSet/PAR002'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient received administration of-fdcbe706c78b2c54e605278723289faf":
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
    where Alias in "Carbidopa Levodopa Combination (RXNORM)"
)

define "Patient received administration of-c79b80ee2f21a20f9273725dbc89e671":
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
    where Alias in "Levodopa (RXNORM)"
)

define "Patient has a diagnosis of-39dcc0bb7af286d952fd63384b54ab51":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Multiple System Atrophy"
)

define "Patient has a problem of-5c2729e4fc2cbf6fbbbfb638bddcb34c":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Multiple System Atrophy"
)

define "Problem has status of-d1aee4324c6dd4682afeb1255b4c268b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-8869df21fa4140374bb6a9c81e13419a":
   "Problem has status of-d1aee4324c6dd4682afeb1255b4c268b"
   and ( "Patient has a problem of-5c2729e4fc2cbf6fbbbfb638bddcb34c")

define "Patient has a diagnosis of-cfb2deb3edbef1eb5f9d357c78fa9cc7":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Parkinsons Disease (Disorders)"
)

define "Patient has a problem of-a48e44ef1f8bdefe8025f6afb7bf0abf":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Parkinsons Disease (Disorders)"
)

define "Problem has status of-7afbe4dd2e1b5f3d18a7b4eb52dfc5a3":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-fe597ab10649cd41399b45f5a2c46c1c":
   "Problem has status of-7afbe4dd2e1b5f3d18a7b4eb52dfc5a3"
   and ( "Patient has a problem of-a48e44ef1f8bdefe8025f6afb7bf0abf")

define "Patient has a diagnosis of-54d1544d50a4b934a0b3b93e938d6d42":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Parkinsonism [Secondary]"
)

define "Patient has a problem of-dc064feb3ab03ac5223b1c5250716a3c":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Parkinsonism [Secondary]"
)

define "Problem has status of-8c05f341580be9444333c6767d40d493":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-d97ae83f7efcb079b0fb665f895e9e7e":
   "Problem has status of-8c05f341580be9444333c6767d40d493"
   and ( "Patient has a problem of-dc064feb3ab03ac5223b1c5250716a3c")

define "ConditionCriteriaMet":
   "Group 1-d97ae83f7efcb079b0fb665f895e9e7e"
   or ( "Patient has a diagnosis of-54d1544d50a4b934a0b3b93e938d6d42")
   and ( "Group 1-fe597ab10649cd41399b45f5a2c46c1c")
   or ( "Patient has a diagnosis of-cfb2deb3edbef1eb5f9d357c78fa9cc7")
   and ( "Group 1-8869df21fa4140374bb6a9c81e13419a")
   or ( "Patient has a diagnosis of-39dcc0bb7af286d952fd63384b54ab51")
   and ( "Patient received administration of-c79b80ee2f21a20f9273725dbc89e671")
   and ( "Patient received administration of-fdcbe706c78b2c54e605278723289faf")
