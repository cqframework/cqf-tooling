library Viral_hemorrhagic_fever_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "VHF Symptoms" : 'https://hln.com/fhir/ValueSet/SYMP073'
valueset "Viral Hemorrhagic Fever (Tests for Viral Hemorrhagic Fever Viruses Antigen)" : 'https://hln.com/fhir/ValueSet/VHF004'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Viral Hemorrhagic Fever (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/VHF001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Viral Hemorrhagic Fever (Tests for Viral Hemorrhagic Fever Viruses Antibody)" : 'https://hln.com/fhir/ValueSet/VHF005'
valueset "Viral Hemorrhagic Fever (Tests for Viral Hemorrhagic Fever Viruses Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/VHF003'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Fever" : 'https://hln.com/fhir/ValueSet/SYMP007'
valueset "Viruses (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST002'
valueset "Viral Hemorrhagic Fever (Test Panels for Viral Hemorrhagic Fever Virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/VHF006'
valueset "Proteinuria (Tests for Total Protein in 24H Urine)" : 'https://hln.com/fhir/ValueSet/VHF007'
valueset "Thrombocytopenia (Tests for Platelets [#/volume] in Blood) " : 'https://hln.com/fhir/ValueSet/VHF008'
valueset "Viral Hemorrhagic Fever (Disorders)" : 'https://hln.com/fhir/ValueSet/VHF002'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-f10da01d439af33afe132b3f6333dc9a":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "VHF Symptoms"
)

define "Patient has a problem of-3fcd04b2e91f630cc3c8434418e29729":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "VHF Symptoms"
)

define "Problem has status of-3ebcbffdcf1c4db4f74dd8df2dd4f070":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-bcf3ed352c8439748a6259434c452a43":
   "Problem has status of-3ebcbffdcf1c4db4f74dd8df2dd4f070"
   and ( "Patient has a problem of-3fcd04b2e91f630cc3c8434418e29729")

define "Patient has lab result with test name (specific for an organism or substance)-21523ea68b7adbbd6eba89d91685bc97":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Tests for Viral Hemorrhagic Fever Viruses Antigen)"
)

define "Lab Result Value (ordinal)-cd6e97961d5a85faa98a1bd43f9650f8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-d30a5540a7931c893d1fa76674b936f8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-8f3ef033965982a98177625b528281fb":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-4228bddb83cd6dd669904eb3ac2058d2":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Tests for Viral Hemorrhagic Fever Viruses Antibody)"
)

define "Lab Result Value (ordinal)-153e8e51e1091d0e587af875855daa74":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-cd7912497b79c72dff187271b79acf0c":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-f5b64e8c6ea1e7571e7ae45ce91aedec":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-caa95a055d84b23c69912aab4bc10ee2":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Tests for Viral Hemorrhagic Fever Viruses Nucleic Acid)"
)

define "Lab Result Value (ordinal)-c6ab4046d21ea365dddc756bd644b4b8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-b47f260ba7b89c29149f3cb576d020a3":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-fd09dc72776b4f11164903d2964f8087":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-28017119bfc0a2b7ba5b88bf45cb8ae3":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-8b5f5a71bc4be3baaf1ebb404343dfdb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-758c4c3b1d932e295974329954b3a719":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Fever"
)

define "Patient has a problem of-df98d1d2f0f7c38a7e5c8595b2f21dbf":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Fever"
)

define "Problem has status of-eb504846a0fb1b832e59c05395298730":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-6389ee3efc464bb95850c12c955cd2b8":
   "Problem has status of-eb504846a0fb1b832e59c05395298730"
   and ( "Patient has a problem of-df98d1d2f0f7c38a7e5c8595b2f21dbf")

define "Lab Result Value (ordinal)-70584d60e385ef98e9f2d3a5f956034b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-15384b18f423fe1863a8a1fbfac47eb2":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-94dd65ae8b1b3539defe8a83fbd95fee":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viruses (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-8babf5bf23a8c73ab948d60e535210dc":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Organism or Substance in Lab Results)"
)

define "Patient has a lab test order with test name-dae7ec5c2367b79c8acf071528bb54de":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Tests for Viral Hemorrhagic Fever Viruses Antibody)"
)

define "Patient has a lab test order with test name-d16c1b133e2e1c4c8894f0d347962947":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Tests for Viral Hemorrhagic Fever Viruses Antigen)"
)

define "Patient has a lab test order with test name-d24e37d7f299950b777ccab81a4d1734":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Tests for Viral Hemorrhagic Fever Viruses Nucleic Acid)"
)

define "Patient has a lab test order with test name-4d2fda7a34a37b7286be5944b1d558a6":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Test Panels for Viral Hemorrhagic Fever Virus Nucleic Acid)"
)

define "Patient has lab result with test name (specific or an organism or substance)-a3751b97cdb6b2f7f4d03133257569c9":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Proteinuria (Tests for Total Protein in 24H Urine)"
)

define "Lab Result Value (quantitative)-f26c02eaf319b874e9754dbe87a4618b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias >= 150 'mg/(24.h)'
)

define "Lab Result Value (quantitative)-4712e5d6729ba529a19fb267d5004b64":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias >= 150 'mg'
)

define "Lab Result Value (ordinal)-4078471cd3eb3f6b5f9efa819abd6789":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-e6e1391a8d64113a75bab0dbf17d4bed":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Group 1.1-1ce9ab4a7ad6308de79cfeeb7fe6e966":
   "Lab Result Interpretation-e6e1391a8d64113a75bab0dbf17d4bed"
   or ( "Lab Result Value (ordinal)-4078471cd3eb3f6b5f9efa819abd6789")
   or ( "Lab Result Value (quantitative)-4712e5d6729ba529a19fb267d5004b64")
   or ( "Lab Result Value (quantitative)-f26c02eaf319b874e9754dbe87a4618b")
   and ( "Patient has lab result with test name (specific or an organism or substance)-a3751b97cdb6b2f7f4d03133257569c9")

define "Group 1-e1b2d66968445125ae213b959551efcd":
   "Group 1.1-1ce9ab4a7ad6308de79cfeeb7fe6e966"
   or ( "Patient has a lab test order with test name-4d2fda7a34a37b7286be5944b1d558a6")

define "Patient has lab result with test name (specific or an organism or substance)-72ba5234a818da33c2f18fd29a37561f":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Thrombocytopenia (Tests for Platelets [#/volume] in Blood) "
)

define "Lab Result Value (quantitative)-6cc88ab7dd698e602429d399cb0daed6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.Quantity
  ) Alias
    where Alias < ToQuantity(150000.0)150000.0
)

define "Lab Result Value (ordinal)-4d5effc0b58e9791f462bc7bf3eae0a8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-2f028fe6ff7695062f06ceb2970ddbf3":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Group 1.1-5929dd18cf3be809717eac146c13f2e8":
   "Lab Result Interpretation-2f028fe6ff7695062f06ceb2970ddbf3"
   or ( "Lab Result Value (ordinal)-4d5effc0b58e9791f462bc7bf3eae0a8")
   or ( "Lab Result Value (quantitative)-6cc88ab7dd698e602429d399cb0daed6")
   and ( "Patient has lab result with test name (specific or an organism or substance)-72ba5234a818da33c2f18fd29a37561f")
   or ( "Group 1-e1b2d66968445125ae213b959551efcd")

define "Group 1-5cab12cf89cab2605ebe5d856c844f2c":
   "Group 1.1-5929dd18cf3be809717eac146c13f2e8"
   or ( "Patient has a lab test order with test name-d24e37d7f299950b777ccab81a4d1734")

define "Patient has a diagnosis of-2d6d2029cc98d05519ed119fbc728727":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Disorders)"
)

define "Patient has a problem of-9d51c5cd89e8e9d218e1de2b91c49b73":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Viral Hemorrhagic Fever (Disorders)"
)

define "Problem has status of-d40a3104f1823df590b07b91e3fe53c9":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-5514e7b93be75ec534a2656be84c0843":
   "Problem has status of-d40a3104f1823df590b07b91e3fe53c9"
   and ( "Patient has a problem of-9d51c5cd89e8e9d218e1de2b91c49b73")

define "ConditionCriteriaMet":
   "Group 1-5514e7b93be75ec534a2656be84c0843"
   or ( "Patient has a diagnosis of-2d6d2029cc98d05519ed119fbc728727")
   or ( "Group 1-5cab12cf89cab2605ebe5d856c844f2c")
   or ( "Patient has a lab test order with test name-d16c1b133e2e1c4c8894f0d347962947")
   or ( "Patient has a lab test order with test name-dae7ec5c2367b79c8acf071528bb54de")
   or ( "Lab Result Value (nominal)-8babf5bf23a8c73ab948d60e535210dc")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-94dd65ae8b1b3539defe8a83fbd95fee")
   or ( "Lab Result Interpretation-15384b18f423fe1863a8a1fbfac47eb2")
   or ( "Lab Result Value (ordinal)-70584d60e385ef98e9f2d3a5f956034b")
   and ( "Group 1-6389ee3efc464bb95850c12c955cd2b8")
   or ( "Patient has a diagnosis of-758c4c3b1d932e295974329954b3a719")
   or ( "Lab Result Value (nominal)-8b5f5a71bc4be3baaf1ebb404343dfdb")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-28017119bfc0a2b7ba5b88bf45cb8ae3")
   or ( "Lab Result Interpretation-fd09dc72776b4f11164903d2964f8087")
   or ( "Lab Result Value (nominal)-b47f260ba7b89c29149f3cb576d020a3")
   or ( "Lab Result Value (ordinal)-c6ab4046d21ea365dddc756bd644b4b8")
   and ( "Patient has lab result with test name (specific for an organism or substance)-caa95a055d84b23c69912aab4bc10ee2")
   or ( "Lab Result Interpretation-f5b64e8c6ea1e7571e7ae45ce91aedec")
   or ( "Lab Result Value (nominal)-cd7912497b79c72dff187271b79acf0c")
   or ( "Lab Result Value (ordinal)-153e8e51e1091d0e587af875855daa74")
   and ( "Patient has lab result with test name (specific for an organism or substance)-4228bddb83cd6dd669904eb3ac2058d2")
   or ( "Lab Result Interpretation-8f3ef033965982a98177625b528281fb")
   or ( "Lab Result Value (nominal)-d30a5540a7931c893d1fa76674b936f8")
   or ( "Lab Result Value (ordinal)-cd6e97961d5a85faa98a1bd43f9650f8")
   and ( "Patient has lab result with test name (specific for an organism or substance)-21523ea68b7adbbd6eba89d91685bc97")
   and ( "Group 1-bcf3ed352c8439748a6259434c452a43")
   or ( "Patient has a diagnosis of-f10da01d439af33afe132b3f6333dc9a")
