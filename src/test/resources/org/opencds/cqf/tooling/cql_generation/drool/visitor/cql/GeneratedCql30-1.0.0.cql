library GeneratedCql30 version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Invasive Haemophilus influenzae Disease (Tests for Haemophilus influenzae Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/HAE005'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Invasive Haemophilus influenzae Disease (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/HAE001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Invasive Haemophilus influenzae Disease (Tests for Haemophilus influenzae Nucleic Acid in Specimen from Normally Sterile Site)" : 'https://hln.com/fhir/ValueSet/HAE006'
valueset "Organisms (Tests for Nucleic Acid in Specimen from Normally Sterile Site)" : 'https://hln.com/fhir/ValueSet/LabTST024'
valueset "Invasive Haemophilus influenzae Disease (Tests for Haemophilus influenzae Type B Antigen in Cerebrospinal Fluid)" : 'https://hln.com/fhir/ValueSet/HAE007'
valueset "Invasive Haemophilus influenzae Disease (Tests for Haemophilus influenzae by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/HAE003'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'
valueset "Invasive Haemophilus influenzae Disease (Tests for Haemophilus influenzae in Specimen from Normally Sterile Site by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/HAE004'
valueset "Bacteria (Tests in Specimen from Normally Sterile Site by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST005'
valueset "Invasive Haemophilus Influenza Disease (Disorders)" : 'https://hln.com/fhir/ValueSet/HAE002'
valueset "Meningitis" : 'https://hln.com/fhir/ValueSet/SYMP019'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-207ad63ab7917f6c12f886c55cc32565":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Tests for Haemophilus influenzae Nucleic Acid)"
)

define "Lab Result Value (ordinal)-e7043f1e11919229df940900dbee9531":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-2238cd8d260a823cd8e2c2a3588bc842":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-894ef0817894d52b23ddccc77d0f7819":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-c27b89762bab23e7e7ba23e68fb4a696":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-98be5187b88e0235f0626593c40c7c88":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-469e956eee21543dc93cefd6dcc06125":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Tests for Haemophilus influenzae Nucleic Acid in Specimen from Normally Sterile Site)"
)

define "Lab Result Value (ordinal)-a602961613f219815088749ea02f579e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-425a2e66d20b9cc5ef376c3f6dd32a2e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-9515dc30cd1294a1ff0019fbd3978674":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-5fbf04dde9e9b11746115ccc7a68dc40":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid in Specimen from Normally Sterile Site)"
)

define "Lab Result Value (nominal)-86fd2fcd992ecf5fa87ceaf2d86e0ec2":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-bd7d6972761030f348a99493b629c2a2":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Tests for Haemophilus influenzae Type B Antigen in Cerebrospinal Fluid)"
)

define "Lab Result Value (ordinal)-bc8704a1866c40ce7bf75e53d24a6223":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-5e838d7c92632fb4a3bacb3fba78ebfb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-76c435a557d9f3b1982e8e57f28e2ea5":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-8943baef293a35ad6951355eb1a0efd9":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Tests for Haemophilus influenzae by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-71f4453cfb9a78e76838a46b7a7948be":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-c05a6c1b6511c82e9c1fa2b9a0cfbf58":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-2101fe0c776594962353484d12e148b1":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-e8ef65fa2122124db6d8421ca3ce46b9":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-29e36c51199266919507f4e5bcaf3950":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-2c24e81b4db503dc1c91f68d1273f1e9":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Tests for Haemophilus influenzae in Specimen from Normally Sterile Site by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-a62150c897f37bc473192ccaf7a12daa":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-5be3b31c1cfff95ef68c3ed586aa9807":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-f51380fd69de1c791660242fdcb8cc66":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-437f8fa8ddbd4062f24e907721382c31":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests in Specimen from Normally Sterile Site by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-ca9802c2360f8baa728aa936b70d7a88":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Invasive Haemophilus influenzae Disease (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-585c5be428f4773a9f84e69265060f63":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Invasive Haemophilus Influenza Disease (Disorders)"
)

define "Patient has a problem of-11dfdac14389361c9b115f098fa8d5fc":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Invasive Haemophilus Influenza Disease (Disorders)"
)

define "Problem has status of-9bb272506956441c504ba8dbc8adf229":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-e3171c881fe1361258e68fdeb809f4d8":
   "Problem has status of-9bb272506956441c504ba8dbc8adf229"
   and ( "Patient has a problem of-11dfdac14389361c9b115f098fa8d5fc")

define "Patient has a diagnosis of-1bd88d26850c728f4b7e363d3cbd89df":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Meningitis"
)

define "Patient has a problem of-13f5dc0b49fb19831acb5cc4521ae181":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Meningitis"
)

define "Problem has status of-feea313ad2080f37a16cf26d9b2140ca":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-efe0d3c3a5e90635b04fedfe8a16ee96":
   "Problem has status of-feea313ad2080f37a16cf26d9b2140ca"
   and ( "Patient has a problem of-13f5dc0b49fb19831acb5cc4521ae181")

define "ConditionCriteriaMet":
   "Group 1-efe0d3c3a5e90635b04fedfe8a16ee96"
   or ( "Patient has a diagnosis of-1bd88d26850c728f4b7e363d3cbd89df")
   and ( "Group 1-e3171c881fe1361258e68fdeb809f4d8")
   or ( "Patient has a diagnosis of-585c5be428f4773a9f84e69265060f63")
   or ( "Lab Result Value (nominal)-ca9802c2360f8baa728aa936b70d7a88")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-437f8fa8ddbd4062f24e907721382c31")
   or ( "Lab Result Interpretation-f51380fd69de1c791660242fdcb8cc66")
   or ( "Lab Result Value (nominal)-5be3b31c1cfff95ef68c3ed586aa9807")
   or ( "Lab Result Value (ordinal)-a62150c897f37bc473192ccaf7a12daa")
   and ( "Patient has lab result with test name (specific for an organism or substance)-2c24e81b4db503dc1c91f68d1273f1e9")
   or ( "Lab Result Value (nominal)-29e36c51199266919507f4e5bcaf3950")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-e8ef65fa2122124db6d8421ca3ce46b9")
   or ( "Lab Result Interpretation-2101fe0c776594962353484d12e148b1")
   or ( "Lab Result Value (nominal)-c05a6c1b6511c82e9c1fa2b9a0cfbf58")
   or ( "Lab Result Value (ordinal)-71f4453cfb9a78e76838a46b7a7948be")
   and ( "Patient has lab result with test name (specific for an organism or substance)-8943baef293a35ad6951355eb1a0efd9")
   or ( "Lab Result Interpretation-76c435a557d9f3b1982e8e57f28e2ea5")
   or ( "Lab Result Value (nominal)-5e838d7c92632fb4a3bacb3fba78ebfb")
   or ( "Lab Result Value (ordinal)-bc8704a1866c40ce7bf75e53d24a6223")
   and ( "Patient has lab result with test name (specific for an organism or substance)-bd7d6972761030f348a99493b629c2a2")
   or ( "Lab Result Value (nominal)-86fd2fcd992ecf5fa87ceaf2d86e0ec2")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-5fbf04dde9e9b11746115ccc7a68dc40")
   or ( "Lab Result Interpretation-9515dc30cd1294a1ff0019fbd3978674")
   or ( "Lab Result Value (nominal)-425a2e66d20b9cc5ef376c3f6dd32a2e")
   or ( "Lab Result Value (ordinal)-a602961613f219815088749ea02f579e")
   and ( "Patient has lab result with test name (specific for an organism or substance)-469e956eee21543dc93cefd6dcc06125")
   or ( "Lab Result Value (nominal)-98be5187b88e0235f0626593c40c7c88")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-c27b89762bab23e7e7ba23e68fb4a696")
   or ( "Lab Result Interpretation-894ef0817894d52b23ddccc77d0f7819")
   or ( "Lab Result Value (nominal)-2238cd8d260a823cd8e2c2a3588bc842")
   or ( "Lab Result Value (ordinal)-e7043f1e11919229df940900dbee9531")
   and ( "Patient has lab result with test name (specific for an organism or substance)-207ad63ab7917f6c12f886c55cc32565")
