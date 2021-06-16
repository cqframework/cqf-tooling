library Influenza_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Influenza (Tests for influenza A or B virus Antigen)" : 'https://hln.com/fhir/ValueSet/FLU008'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Influenza (influenza A or B virus in Lab Results)" : 'https://hln.com/fhir/ValueSet/FLU001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Influenza (Tests for Influenza A or B Virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/FLU007'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Influenza (Tests for influenza A or B virus by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/FLU006'
valueset "Viruses (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST002'
valueset "Influenza (Disorders)" : 'https://hln.com/fhir/ValueSet/FLU004'
valueset "Discharge Disposition [Expired]" : 'https://hln.com/fhir/ValueSet/ENC001'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-46420e10f3f0d1b8902191b83e9f4985":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Influenza (Tests for influenza A or B virus Antigen)"
)

define "Lab Result Value (ordinal)-5b191b47529bfa12e3c5a941f7659322":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-aeb994523fd466caf7cc9f0ff6de3847":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Influenza (influenza A or B virus in Lab Results)"
)

define "Lab Result Interpretation-447f04934e9aba632c97974fc572d5f5":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-37880672c015a4f223a1ddec71bffc22":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Influenza (Tests for Influenza A or B Virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-9c85e04a1462b7da20688bc3ea693651":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-fbc2900f5d53ffe37bed353e26cad1e8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Influenza (influenza A or B virus in Lab Results)"
)

define "Lab Result Interpretation-a30a2d21f84c73e44ff315ec607311ad":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-47743a4f4082350741f7ca9c6864833c":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-e26821d121f629083306307610e438f4":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Influenza (influenza A or B virus in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-cfb34e52e67083cf740547503f9c209a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Influenza (Tests for influenza A or B virus by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-d4273634cefb63a98fd15aaec2a506b9":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-2cde5460e326ac8ea9f5425a8d6feda3":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Influenza (influenza A or B virus in Lab Results)"
)

define "Lab Result Interpretation-10b384876e966ca7b1cd00262be1191d":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-87a90285c7f86d26ebf3b017dbc7aa65":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viruses (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-e6d8e893ac7aafb8e73c71eb13857154":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Influenza (influenza A or B virus in Lab Results)"
)

define "Patient has a diagnosis of-e92fbd5662ef3899df7807c7d05520fd":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Influenza (Disorders)"
)

define "Patient has a problem of-418ae52451f6409a2f3a0f4f2e1ee7e3":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Influenza (Disorders)"
)

define "Problem has status of-83d274ddd8c9594d7ab7636c9896f32b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-5babaac74966ddc0b9e969e64866c1ce":
   "Problem has status of-83d274ddd8c9594d7ab7636c9896f32b"
   and ( "Patient has a problem of-418ae52451f6409a2f3a0f4f2e1ee7e3")

define "Units:-f6d98e67344668905c152ddd1b818aa9":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) < 18 'years'

define "Patient is deceased-5c32d09b8bff7c8945353e9f589b1133":
  exists (
    ([Patient] P
      return P.deceased as FHIR.boolean
  ) Alias
    where not  Equal( Alias, true)
)

define "Patient has a discharge disposition of-54190975fad7c3157df4c233c10af50d":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Discharge Disposition [Expired]"
)

define "ConditionCriteriaMet":
   "Patient has a discharge disposition of-54190975fad7c3157df4c233c10af50d"
   or ( "Patient is deceased-5c32d09b8bff7c8945353e9f589b1133")
   and ( "Units:-f6d98e67344668905c152ddd1b818aa9")
   and ( "Group 1-5babaac74966ddc0b9e969e64866c1ce")
   or ( "Patient has a diagnosis of-e92fbd5662ef3899df7807c7d05520fd")
   or ( "Lab Result Value (nominal)-e6d8e893ac7aafb8e73c71eb13857154")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-87a90285c7f86d26ebf3b017dbc7aa65")
   or ( "Lab Result Interpretation-10b384876e966ca7b1cd00262be1191d")
   or ( "Lab Result Value (nominal)-2cde5460e326ac8ea9f5425a8d6feda3")
   or ( "Lab Result Value (ordinal)-d4273634cefb63a98fd15aaec2a506b9")
   and ( "Patient has lab result with test name (specific for an organism or substance)-cfb34e52e67083cf740547503f9c209a")
   or ( "Lab Result Value (nominal)-e26821d121f629083306307610e438f4")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-47743a4f4082350741f7ca9c6864833c")
   or ( "Lab Result Interpretation-a30a2d21f84c73e44ff315ec607311ad")
   or ( "Lab Result Value (nominal)-fbc2900f5d53ffe37bed353e26cad1e8")
   or ( "Lab Result Value (ordinal)-9c85e04a1462b7da20688bc3ea693651")
   and ( "Patient has lab result with test name (specific for an organism or substance)-37880672c015a4f223a1ddec71bffc22")
   or ( "Lab Result Interpretation-447f04934e9aba632c97974fc572d5f5")
   or ( "Lab Result Value (nominal)-aeb994523fd466caf7cc9f0ff6de3847")
   or ( "Lab Result Value (ordinal)-5b191b47529bfa12e3c5a941f7659322")
   and ( "Patient has lab result with test name (specific for an organism or substance)-46420e10f3f0d1b8902191b83e9f4985")
