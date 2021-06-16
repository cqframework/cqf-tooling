library Zika_virus_disease_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Zika Virus Disease (Tests for Zika virus IgM Antibody)" : 'https://hln.com/fhir/ValueSet/ZIK005'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Zika Virus Disease (Tests for Zika virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/ZIK006'
valueset "Arthralgia" : 'https://hln.com/fhir/ValueSet/SYMP025'
valueset "Congenital Zika Syndromes (Disorders)" : 'https://hln.com/fhir/ValueSet/ZIK003'
valueset "Conjunctivitis" : 'https://hln.com/fhir/ValueSet/SYMP027'
valueset "Assertion" : 'https://hln.com/fhir/ValueSet/ASSERTION'
valueset "Pregnant" : 'https://hln.com/fhir/ValueSet/PREG1'
valueset "Zika Virus Disease  (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/ZIK001'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Fever" : 'https://hln.com/fhir/ValueSet/SYMP007'
valueset "Guillain-Barre Syndrome (Disorders)" : 'https://hln.com/fhir/ValueSet/GBS001'
valueset "Arboviral Diseases [Reportable] (Tests for Arbovirus by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/ARB004'
valueset "Arboviral Infection (Tests for Flavivirus by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/ARB001'
valueset "Viruses (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST002'
valueset "Negative or Indeterminate Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT003'
valueset "Zika Virus Disease (Test Panels for Zika virus IgM Antibody)" : 'https://hln.com/fhir/ValueSet/ZIK007'
valueset "Zika Virus Disease (Test Panels for Zika virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/ZIK004'
valueset "Negative or Undetected Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT012'
valueset "Postpartum" : 'https://hln.com/fhir/ValueSet/SYMP026'
valueset "Rash" : 'https://hln.com/fhir/ValueSet/SYMP024'
valueset "Zika Virus Disease (Disorders)" : 'https://hln.com/fhir/ValueSet/ZIK002'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-f67a48f9ed8c25a7995c7e543f8cbecf":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Zika Virus Disease (Tests for Zika virus IgM Antibody)"
)

define "Lab Result Value (ordinal)-ab5f7d98da9b48bee79b60fa57c79237":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-f42078489c7a57175c5dcd03b4ac16ed":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-1013ffc43d88bdcbdea631c1a482a12d":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Zika Virus Disease (Tests for Zika virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-7010aeb1e7624a0bf269e440479b28af":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-50dc618adb2c317f4c77623b63cab38b":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has a diagnosis of-ba56068bf76eca1380acf03aabc2fa90":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Arthralgia"
)

define "Problem has status of-3084d1f3af5561b504d3e723e027cc6e":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Patient has a problem of-e547222cc7be58653ee0d3d205ed1fc8":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Arthralgia"
)

define "Group 1-2ce3c1efa12c36b8f73d057634914779":
   "Patient has a problem of-e547222cc7be58653ee0d3d205ed1fc8"
   and ( "Problem has status of-3084d1f3af5561b504d3e723e027cc6e")

define "Patient has a diagnosis of-ad083a6da6055a6446a5bf6ee3246dc3":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Congenital Zika Syndromes (Disorders)"
)

define "Problem has status of-b9c3c3ff4f5c53ed8e794e5a399aff8b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Patient has a problem of-619de51e9bdf99c4b0767b38d41e5173":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Congenital Zika Syndromes (Disorders)"
)

define "Group 1-c1e2fc4369aca86fb27bd4569ef9b510":
   "Patient has a problem of-619de51e9bdf99c4b0767b38d41e5173"
   and ( "Problem has status of-b9c3c3ff4f5c53ed8e794e5a399aff8b")

define "Patient has a diagnosis of-efb77bacd58579c77ddc62779d56ca98":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Conjunctivitis"
)

define "Problem has status of-26cf27a85efa775dea27ecf84770e597":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Patient has a problem of-3a6ae3da8916503fc0335360cfc76ef3":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Conjunctivitis"
)

define "Group 1-5b784401d0d1f62e87e6abbef51d6cd9":
   "Patient has a problem of-3a6ae3da8916503fc0335360cfc76ef3"
   and ( "Problem has status of-26cf27a85efa775dea27ecf84770e597")

define "Patient record contains an observation with focus-cd2a64161518e82328c960facd081f3c":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Assertion"
)

define "Patient record contains an observation with value-e4c87e4608b5bf8555e84b1393898658":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Pregnant"
)

define "Group 1-5290b4b333c73e76c147c1a589d61ab6":
   "Patient record contains an observation with value-e4c87e4608b5bf8555e84b1393898658"
   and ( "Patient record contains an observation with focus-cd2a64161518e82328c960facd081f3c")

define "Patient has a problem of-6b82e944555d10b7c765caf5bb363246":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Pregnant"
)

define "Problem has status of-6564bf7ca1e5b092370c75eff3d3013a":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 2-565cc2dbe527092d5a7e22bec2f83d2b":
   "Problem has status of-6564bf7ca1e5b092370c75eff3d3013a"
   and ( "Patient has a problem of-6b82e944555d10b7c765caf5bb363246")

define "Patient has lab result with test name (specific for an organism or substance)-1d90ccaa7735d75328ed8431860b15f0":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Zika Virus Disease (Tests for Zika virus IgM Antibody)"
)

define "Lab Result Value (ordinal)-6ff9bf44eff53b0ec918422d8eff8d5f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-c200fac838b57c433c40c174755591c2":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Zika Virus Disease  (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-e01c1cba22f4189965405bdbebb5a834":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-d8da7c0c20a9673a1b14b6bb3db6fc5f":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Zika Virus Disease (Tests for Zika virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-43a1b7246dedfeb96e9b12017e5ef440":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-fad3a91293423bb8e0c43fdc2657385f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Zika Virus Disease  (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-d6007955543c8bf812d60cf5a2b928e7":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-9fdc2c5ce8bb141434800bcb2aae9eda":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-a62308f43c8624fb7b469a1ca35405cc":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Zika Virus Disease  (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-ad2cdd823e838f1cdbcf9b4f7ca90162":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Fever"
)

define "Problem has status of-8bdf5d706278a42b66d530848af416b5":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Patient has a problem of-c1efedf045b2abd16638b071652a7d3d":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Fever"
)

define "Group 1-bdd6abc090e6a688bdbbc7dbf8d737b9":
   "Patient has a problem of-c1efedf045b2abd16638b071652a7d3d"
   and ( "Problem has status of-8bdf5d706278a42b66d530848af416b5")

define "Patient has a diagnosis of-b7fa2b784252f89734c8f0901936ef08":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Guillain-Barre Syndrome (Disorders)"
)

define "Problem has status of-30c302d8a95cb53f301b1eb6d04fb64e":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Patient has a problem of-c145faf32e0d5648f21286d718c60a46":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Guillain-Barre Syndrome (Disorders)"
)

define "Group 1-4bd04497414025e9e48ceeb8b4e8a827":
   "Patient has a problem of-c145faf32e0d5648f21286d718c60a46"
   and ( "Problem has status of-30c302d8a95cb53f301b1eb6d04fb64e")

define "Lab Result Value (ordinal)-e0e83041abbcf4fd4e0ac7b527a284fd":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-137c2216fb8a72ce306693f616894db1":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-1c3a4ec3dad7821f7ddc543c3e4783e0":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Arboviral Diseases [Reportable] (Tests for Arbovirus by Culture and Identification Method)"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-ca7eca0d6034b22d1e1b815b44e92934":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Arboviral Infection (Tests for Flavivirus by Culture and Identification Method)"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-a36f0808a2113b51b704b5eb2e299d9c":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viruses (Tests by Culture and Identification Method)"
)

define "Group 2.1-d94443b7d40d53a19d040ef937f70a65":
   "Patient has lab result with test name (NOT specific for an organism or substance)-a36f0808a2113b51b704b5eb2e299d9c"
   or ( "Patient has lab result with test name (NOT specific for an organism or substance)-ca7eca0d6034b22d1e1b815b44e92934")
   or ( "Patient has lab result with test name (NOT specific for an organism or substance)-1c3a4ec3dad7821f7ddc543c3e4783e0")

define "Lab Result Value (nominal)-5a4638b5bcdf0770442c4884603a364d":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Zika Virus Disease  (Organism or Substance in Lab Results)"
)

define "Group 2.2-318044d87de18a1ea854255767fc6e9f":
   "Lab Result Value (nominal)-5a4638b5bcdf0770442c4884603a364d"
   and ( "Group 2.1-d94443b7d40d53a19d040ef937f70a65")

define "Group 2-c6086ddccb2163da27ed79681d9e9918":
   "Group 2.2-318044d87de18a1ea854255767fc6e9f"
   or ( "Lab Result Interpretation-137c2216fb8a72ce306693f616894db1")

define "Patient has lab result with test name (specific for an organism or substance)-1802298f3454327aa8bfe3dbbff3de5a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Zika Virus Disease (Tests for Zika virus IgM Antibody)"
)

define "Lab Result Value (ordinal)-9660d9a8c3c2bdebbe5d850ba91f014d":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Indeterminate Lab Result Value"
)

define "Lab Result Interpretation-f706107daa2efd2e35db2a084f73fb26":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-379afb47a8b290edb9b986679f1dde54":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Zika Virus Disease (Tests for Zika virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-cb5a64a69f187d06e537025ddc27b9fd":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Indeterminate Lab Result Value"
)

define "Lab Result Interpretation-8119564724852e5fb8f2595cf4d206b4":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has a lab test order with test name-473d1e9710464e8fbff8c8acf005b566":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Zika Virus Disease (Tests for Zika virus IgM Antibody)"
)

define "Patient has a lab test order with test name-13687ec7ef39e7bfd4713741b7bb5d5c":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Zika Virus Disease (Test Panels for Zika virus IgM Antibody)"
)

define "Patient has a lab test order with test name-5f1c30a8adebc34e4f037ada44da9a7b":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Zika Virus Disease (Tests for Zika virus Nucleic Acid)"
)

define "Patient has a lab test order with test name-203cf2ea839f2d913f15a51bf849fa7c":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Zika Virus Disease (Test Panels for Zika virus Nucleic Acid)"
)

define "Patient has lab result with test name (specific for an organism or substance)-f2fb76774c4f43681c33a59150386d0b":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Zika Virus Disease (Tests for Zika virus IgM Antibody)"
)

define "Lab result value of (ordinal)-dd929303243cbb08e460683f6db5adde":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Patient has lab result with test name (specific for an organism or substance)-ec58a9c2240bd23b2188f6a2ec379555":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Zika Virus Disease (Tests for Zika virus Nucleic Acid)"
)

define "Lab result value of (ordinal)-fc6112b8b1fdd22d6efb1f93b1073967":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Units:-4cafe6f9351b2843cf7e7912bc4b1edc":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) < 1 'years'

define "Patient has a diagnosis of-3901b27d8313cc3ad01803db7a385952":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Postpartum"
)

define "Problem has status of-874bc309d1ddd7bf9e6f4be7cf2e9778":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Patient has a problem of-69a87f6e46d6e40eb5bcb6ef9ed90e7a":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Postpartum"
)

define "Group 1-e4662a95a58eb3e3e9b1756b25de77ee":
   "Patient has a problem of-69a87f6e46d6e40eb5bcb6ef9ed90e7a"
   and ( "Problem has status of-874bc309d1ddd7bf9e6f4be7cf2e9778")

define "Patient has a diagnosis of-5aa150a14b31611fcf10f2744a4dfbe8":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Rash"
)

define "Problem has status of-26152743cd452670d9966702e93828ce":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Patient has a problem of-f1dff4cfe01c9dccaabec77ea4a3f708":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Rash"
)

define "Group 1-46c7b9d34156e834529426fdad7b0220":
   "Patient has a problem of-f1dff4cfe01c9dccaabec77ea4a3f708"
   and ( "Problem has status of-26152743cd452670d9966702e93828ce")

define "Patient has a diagnosis of-e24bb8935320750f87daf18573be615b":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Zika Virus Disease (Disorders)"
)

define "Problem has status of-df94689439a6e7014cd6985998b8a83e":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Patient has a problem of-982cd3e18b77adf6bb4bd60c245495b8":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Zika Virus Disease (Disorders)"
)

define "Group 1-70432ba7fcb71e9ef5463ea3bf4c9b62":
   "Patient has a problem of-982cd3e18b77adf6bb4bd60c245495b8"
   and ( "Problem has status of-df94689439a6e7014cd6985998b8a83e")

define "ConditionCriteriaMet":
   "Group 1-70432ba7fcb71e9ef5463ea3bf4c9b62"
   or ( "Patient has a diagnosis of-e24bb8935320750f87daf18573be615b")
   and ( "Group 1-46c7b9d34156e834529426fdad7b0220")
   or ( "Patient has a diagnosis of-5aa150a14b31611fcf10f2744a4dfbe8")
   and ( "Group 1-e4662a95a58eb3e3e9b1756b25de77ee")
   or ( "Patient has a diagnosis of-3901b27d8313cc3ad01803db7a385952")
   and ( "Units:-4cafe6f9351b2843cf7e7912bc4b1edc")
   and ( "Lab result value of (ordinal)-fc6112b8b1fdd22d6efb1f93b1073967")
   and ( "Patient has lab result with test name (specific for an organism or substance)-ec58a9c2240bd23b2188f6a2ec379555")
   and ( "Lab result value of (ordinal)-dd929303243cbb08e460683f6db5adde")
   and ( "Patient has lab result with test name (specific for an organism or substance)-f2fb76774c4f43681c33a59150386d0b")
   or ( "Patient has a lab test order with test name-203cf2ea839f2d913f15a51bf849fa7c")
   or ( "Patient has a lab test order with test name-5f1c30a8adebc34e4f037ada44da9a7b")
   or ( "Patient has a lab test order with test name-13687ec7ef39e7bfd4713741b7bb5d5c")
   or ( "Patient has a lab test order with test name-473d1e9710464e8fbff8c8acf005b566")
   or ( "Lab Result Interpretation-8119564724852e5fb8f2595cf4d206b4")
   or ( "Lab Result Value (ordinal)-cb5a64a69f187d06e537025ddc27b9fd")
   and ( "Patient has lab result with test name (specific for an organism or substance)-379afb47a8b290edb9b986679f1dde54")
   or ( "Lab Result Interpretation-f706107daa2efd2e35db2a084f73fb26")
   or ( "Lab Result Value (ordinal)-9660d9a8c3c2bdebbe5d850ba91f014d")
   and ( "Patient has lab result with test name (specific for an organism or substance)-1802298f3454327aa8bfe3dbbff3de5a")
   and ( "Group 2-c6086ddccb2163da27ed79681d9e9918")
   or ( "Lab Result Value (ordinal)-e0e83041abbcf4fd4e0ac7b527a284fd")
   and ( "Group 1-4bd04497414025e9e48ceeb8b4e8a827")
   or ( "Patient has a diagnosis of-b7fa2b784252f89734c8f0901936ef08")
   and ( "Group 1-bdd6abc090e6a688bdbbc7dbf8d737b9")
   or ( "Patient has a diagnosis of-ad2cdd823e838f1cdbcf9b4f7ca90162")
   or ( "Lab Result Value (nominal)-a62308f43c8624fb7b469a1ca35405cc")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-9fdc2c5ce8bb141434800bcb2aae9eda")
   or ( "Lab Result Interpretation-d6007955543c8bf812d60cf5a2b928e7")
   or ( "Lab Result Value (nominal)-fad3a91293423bb8e0c43fdc2657385f")
   or ( "Lab Result Value (ordinal)-43a1b7246dedfeb96e9b12017e5ef440")
   and ( "Patient has lab result with test name (specific for an organism or substance)-d8da7c0c20a9673a1b14b6bb3db6fc5f")
   or ( "Lab Result Interpretation-e01c1cba22f4189965405bdbebb5a834")
   or ( "Lab Result Value (nominal)-c200fac838b57c433c40c174755591c2")
   or ( "Lab Result Value (ordinal)-6ff9bf44eff53b0ec918422d8eff8d5f")
   and ( "Patient has lab result with test name (specific for an organism or substance)-1d90ccaa7735d75328ed8431860b15f0")
   or ( "Group 2-565cc2dbe527092d5a7e22bec2f83d2b")
   or ( "Group 1-5290b4b333c73e76c147c1a589d61ab6")
   and ( "Group 1-5b784401d0d1f62e87e6abbef51d6cd9")
   or ( "Patient has a diagnosis of-efb77bacd58579c77ddc62779d56ca98")
   and ( "Group 1-c1e2fc4369aca86fb27bd4569ef9b510")
   or ( "Patient has a diagnosis of-ad083a6da6055a6446a5bf6ee3246dc3")
   and ( "Group 1-2ce3c1efa12c36b8f73d057634914779")
   or ( "Patient has a diagnosis of-ba56068bf76eca1380acf03aabc2fa90")
   or ( "Lab Result Interpretation-50dc618adb2c317f4c77623b63cab38b")
   or ( "Lab Result Value (ordinal)-7010aeb1e7624a0bf269e440479b28af")
   and ( "Patient has lab result with test name (specific for an organism or substance)-1013ffc43d88bdcbdea631c1a482a12d")
   or ( "Lab Result Interpretation-f42078489c7a57175c5dcd03b4ac16ed")
   or ( "Lab Result Value (ordinal)-ab5f7d98da9b48bee79b60fa57c79237")
   and ( "Patient has lab result with test name (specific for an organism or substance)-f67a48f9ed8c25a7995c7e543f8cbecf")
