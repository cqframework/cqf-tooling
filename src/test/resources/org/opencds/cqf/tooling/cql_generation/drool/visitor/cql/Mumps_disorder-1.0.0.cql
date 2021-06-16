library Mumps_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Aseptic meningitis" : 'https://hln.com/fhir/ValueSet/SYMP036'
valueset "Mumps (Tests for mumps virus Antigen)" : 'https://hln.com/fhir/ValueSet/MUM005'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Mumps (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/MUM001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Mumps (Tests for mumps virus IgM Antibody)" : 'https://hln.com/fhir/ValueSet/MUM006'
valueset "Mumps (Tests for mumps virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/MUM004'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Encephalitis" : 'https://hln.com/fhir/ValueSet/SYMP037'
valueset "Hearing Loss" : 'https://hln.com/fhir/ValueSet/SYMP038'
valueset "Mumps (Tests for mumps virus by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/MUM003'
valueset "Viruses (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST002'
valueset "Mumps (Tests for mumps virus IgG Antibody)" : 'https://hln.com/fhir/ValueSet/MUM007'
valueset "Mumps (Test Panels for mumps virus IgM IgG Antibody)" : 'https://hln.com/fhir/ValueSet/MUM009'
valueset "Mumps (Test Panels for mumps virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/MUM008'
valueset "Mastitis" : 'https://hln.com/fhir/ValueSet/SYMP041'
valueset "Mumps (Disorders)" : 'https://hln.com/fhir/ValueSet/MUM002'
valueset "Oophoritis" : 'https://hln.com/fhir/ValueSet/SYMP040'
valueset "Orchitis" : 'https://hln.com/fhir/ValueSet/SYMP039'
valueset "Pancreatitis" : 'https://hln.com/fhir/ValueSet/SYMP042'
valueset "Parotitis [Unspecified Cause]" : 'https://hln.com/fhir/ValueSet/SYMP035'
valueset "Salivary Gland Swelling [Unspecified Cause] (Disorders)" : 'https://hln.com/fhir/ValueSet/SYMP100'

code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'
code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has a diagnosis of-4ea006366a2e66fd9161829801a6ca12":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Aseptic meningitis"
)

define "Patient has a problem of-e0b4fb804858828b28671b1396694f21":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Aseptic meningitis"
)

define "Problem has status of-96b2453e2be658a49b37a32a878b7304":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-fa09b610216ab9fec6cc6146497cd8c3":
   "Problem has status of-96b2453e2be658a49b37a32a878b7304"
   and ( "Patient has a problem of-e0b4fb804858828b28671b1396694f21")

define "Patient has lab result with test name (specific for an organism or substance)-cfeae464f8e6f014b3988e3e78ce8318":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Mumps (Tests for mumps virus Antigen)"
)

define "Lab Result Value (ordinal)-2249de83dbcbfba1cca3b445024820a8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-d6936f081d77aef6efbb633b791d31b9":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Mumps (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-49725e649143490f88986374c1a9e297":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-912ee566e58b8c604fc75f7d18dfad43":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Mumps (Tests for mumps virus IgM Antibody)"
)

define "Lab Result Value (ordinal)-e5651827e8bb90018645b437f37d3f6f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-9090cad25a8a9beb280a6305b2346c7b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Mumps (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-caeb2de5d93b7cbcde9070e338486586":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-a838ed7a3b1f2626be0413d3f09beb75":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Mumps (Tests for mumps virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-6fb047c68322484392082e92b21fb95d":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-3e480492f9cbd97499f5c8966dfcf5df":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Mumps (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-610259d82c2587807dff07168c09c9a2":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-221a9f617ed6bae619de843c3785330d":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-d1267e53870b1ae078aade959707f3e6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Mumps (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-bc6906188b10489ae38d2ce460b5e639":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Encephalitis"
)

define "Patient has a problem of-7725daf4282f96dc6790122d1f4815fd":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Encephalitis"
)

define "Problem has status of-610b9d1b17434605e03bdba1ceb52f5e":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-db7ac79da5a9af37da0760b94d948d60":
   "Problem has status of-610b9d1b17434605e03bdba1ceb52f5e"
   and ( "Patient has a problem of-7725daf4282f96dc6790122d1f4815fd")

define "Patient has a diagnosis of-ecf17cd45a057fa81e3b26911decc92e":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Hearing Loss"
)

define "Patient has a problem of-96eb0f3ac9917aa54e6f54d3568ee4d8":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Hearing Loss"
)

define "Problem has status of-b4e5e035cb86ac9149a1efa9f665f915":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-be30dc53d92135ef948dbadb171467f5":
   "Problem has status of-b4e5e035cb86ac9149a1efa9f665f915"
   and ( "Patient has a problem of-96eb0f3ac9917aa54e6f54d3568ee4d8")

define "Patient has lab result with test name (specific for an organism or substance)-78ae9eef73029a2edfb503d8fa684d53":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Mumps (Tests for mumps virus by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-a357075139e27a27a0ea61ea9c48fb6d":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-ab9ed6654914e154b1ef724a2fc4691f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Mumps (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-1bf483257ed56e229e33b66071e452a0":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-80d1d2bc4737afe8039a716b1e1f74aa":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viruses (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-f483bbe4d2dcac58baf87446f7577a8d":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Mumps (Organism or Substance in Lab Results)"
)

define "Patient has a lab test order with test name-fec2818b39284687d817648ef39ceeb5":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Mumps (Tests for mumps virus IgM Antibody)"
)

define "Patient has a lab test order with test name-58e968f060c6f31362cd4bdab67a706c":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Mumps (Tests for mumps virus IgG Antibody)"
)

define "Patient has a lab test order with test name-c2c04dda32a27944dab38b05bfd6321e":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Mumps (Test Panels for mumps virus IgM IgG Antibody)"
)

define "Patient has a lab test order with test name-1bdcca0cd254a233a03017a91471e123":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Mumps (Tests for mumps virus Nucleic Acid)"
)

define "Patient has a lab test order with test name-79371c1d1bedb8aec2e301e6b1379267":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Mumps (Test Panels for mumps virus Nucleic Acid)"
)

define "Patient has a lab test order with test name-a52b159a405c714511c0bbac97d0482e":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Mumps (Tests for mumps virus by Culture and Identification Method)"
)

define "Patient has a diagnosis of-de52681fd6f25bdf4ba2875832245d62":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Mastitis"
)

define "Patient has a problem of-92d3f569d66bca26ae8ed7a4de37a057":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Mastitis"
)

define "Problem has status of-6da0760c16339f039c3a5c8672095a3f":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-b716248ca89e1dae5bdd2ba3e5a48737":
   "Problem has status of-6da0760c16339f039c3a5c8672095a3f"
   and ( "Patient has a problem of-92d3f569d66bca26ae8ed7a4de37a057")

define "Patient has a diagnosis of-f33928804901bffb3083441c10df5fa5":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Mumps (Disorders)"
)

define "Patient has a problem of-fa1a9cffe97e528bcaf382d184802db3":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Mumps (Disorders)"
)

define "Problem has status of-48cada4813ac2e80c597bc075629bb4e":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-2c1f364b67d94f6f38a239ed26ee96f3":
   "Problem has status of-48cada4813ac2e80c597bc075629bb4e"
   and ( "Patient has a problem of-fa1a9cffe97e528bcaf382d184802db3")

define "Patient has a diagnosis of-9c2770f141549ac9835e17971824512e":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Oophoritis"
)

define "Patient has a problem of-f27eef523c27af21c1a86fdac53fa589":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Oophoritis"
)

define "Problem has status of-3057986cd8faacf32b604f93521712cf":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-1746f81ba2bae8d86eaf849e9a35044a":
   "Problem has status of-3057986cd8faacf32b604f93521712cf"
   and ( "Patient has a problem of-f27eef523c27af21c1a86fdac53fa589")

define "Patient has a diagnosis of-ad91bd551fa202eb349f52bfd96af2da":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Orchitis"
)

define "Patient has a problem of-7187f76cc6be30528fa3d9b3aad87e66":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Orchitis"
)

define "Problem has status of-7dcd30331b73df4e36bae423f5e19d7a":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-89e57af9fe76547d68ee9c4be3904a7e":
   "Problem has status of-7dcd30331b73df4e36bae423f5e19d7a"
   and ( "Patient has a problem of-7187f76cc6be30528fa3d9b3aad87e66")

define "Patient has a diagnosis of-381045b9b20def39e440078f6f8d6495":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Pancreatitis"
)

define "Patient has a problem of-e814e9fd78bcaf094b2b4249e3b4df85":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Pancreatitis"
)

define "Problem has status of-ca27014239485864e758dca1ba5248e5":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-da63f47e752767bc61920357d46ab476":
   "Problem has status of-ca27014239485864e758dca1ba5248e5"
   and ( "Patient has a problem of-e814e9fd78bcaf094b2b4249e3b4df85")

define "Patient has a diagnosis of-27e6c9cbdfa421825642f851bd1f07f3":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Parotitis [Unspecified Cause]"
)

define "Patient has a problem of-9c0ce6273f4c4a9a95a0f16feab13e61":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Parotitis [Unspecified Cause]"
)

define "Problem has status of-e02bde24c39910b1fa4b5ef35364279d":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-ba8dd25b80926f35d90e3d459eb7533e":
   "Problem has status of-e02bde24c39910b1fa4b5ef35364279d"
   and ( "Patient has a problem of-9c0ce6273f4c4a9a95a0f16feab13e61")

define "Patient has a diagnosis of-bc4a67335af17e2cf1323bffc1b5e72f":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Salivary Gland Swelling [Unspecified Cause] (Disorders)"
)

define "Patient has a problem of-6df0dac6e2a2e10c30350eaa86dcfb2a":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Salivary Gland Swelling [Unspecified Cause] (Disorders)"
)

define "Problem has status of-94476d94948eb5c70cbdce65fc13e68e":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-97569bf11f655ddfbdd7b83100046d25":
   "Problem has status of-94476d94948eb5c70cbdce65fc13e68e"
   and ( "Patient has a problem of-6df0dac6e2a2e10c30350eaa86dcfb2a")

define "ConditionCriteriaMet":
   "Group 1-97569bf11f655ddfbdd7b83100046d25"
   or ( "Patient has a diagnosis of-bc4a67335af17e2cf1323bffc1b5e72f")
   and ( "Group 1-ba8dd25b80926f35d90e3d459eb7533e")
   or ( "Patient has a diagnosis of-27e6c9cbdfa421825642f851bd1f07f3")
   and ( "Group 1-da63f47e752767bc61920357d46ab476")
   or ( "Patient has a diagnosis of-381045b9b20def39e440078f6f8d6495")
   and ( "Group 1-89e57af9fe76547d68ee9c4be3904a7e")
   or ( "Patient has a diagnosis of-ad91bd551fa202eb349f52bfd96af2da")
   and ( "Group 1-1746f81ba2bae8d86eaf849e9a35044a")
   or ( "Patient has a diagnosis of-9c2770f141549ac9835e17971824512e")
   and ( "Group 1-2c1f364b67d94f6f38a239ed26ee96f3")
   or ( "Patient has a diagnosis of-f33928804901bffb3083441c10df5fa5")
   and ( "Group 1-b716248ca89e1dae5bdd2ba3e5a48737")
   or ( "Patient has a diagnosis of-de52681fd6f25bdf4ba2875832245d62")
   or ( "Patient has a lab test order with test name-a52b159a405c714511c0bbac97d0482e")
   or ( "Patient has a lab test order with test name-79371c1d1bedb8aec2e301e6b1379267")
   or ( "Patient has a lab test order with test name-1bdcca0cd254a233a03017a91471e123")
   or ( "Patient has a lab test order with test name-c2c04dda32a27944dab38b05bfd6321e")
   or ( "Patient has a lab test order with test name-58e968f060c6f31362cd4bdab67a706c")
   or ( "Patient has a lab test order with test name-fec2818b39284687d817648ef39ceeb5")
   or ( "Lab Result Value (nominal)-f483bbe4d2dcac58baf87446f7577a8d")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-80d1d2bc4737afe8039a716b1e1f74aa")
   or ( "Lab Result Interpretation-1bf483257ed56e229e33b66071e452a0")
   or ( "Lab Result Value (nominal)-ab9ed6654914e154b1ef724a2fc4691f")
   or ( "Lab Result Value (ordinal)-a357075139e27a27a0ea61ea9c48fb6d")
   and ( "Patient has lab result with test name (specific for an organism or substance)-78ae9eef73029a2edfb503d8fa684d53")
   and ( "Group 1-be30dc53d92135ef948dbadb171467f5")
   or ( "Patient has a diagnosis of-ecf17cd45a057fa81e3b26911decc92e")
   and ( "Group 1-db7ac79da5a9af37da0760b94d948d60")
   or ( "Patient has a diagnosis of-bc6906188b10489ae38d2ce460b5e639")
   or ( "Lab Result Value (nominal)-d1267e53870b1ae078aade959707f3e6")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-221a9f617ed6bae619de843c3785330d")
   or ( "Lab Result Interpretation-610259d82c2587807dff07168c09c9a2")
   or ( "Lab Result Value (nominal)-3e480492f9cbd97499f5c8966dfcf5df")
   or ( "Lab Result Value (ordinal)-6fb047c68322484392082e92b21fb95d")
   and ( "Patient has lab result with test name (specific for an organism or substance)-a838ed7a3b1f2626be0413d3f09beb75")
   or ( "Lab Result Interpretation-caeb2de5d93b7cbcde9070e338486586")
   or ( "Lab Result Value (nominal)-9090cad25a8a9beb280a6305b2346c7b")
   or ( "Lab Result Value (ordinal)-e5651827e8bb90018645b437f37d3f6f")
   and ( "Patient has lab result with test name (specific for an organism or substance)-912ee566e58b8c604fc75f7d18dfad43")
   or ( "Lab Result Interpretation-49725e649143490f88986374c1a9e297")
   or ( "Lab Result Value (nominal)-d6936f081d77aef6efbb633b791d31b9")
   or ( "Lab Result Value (ordinal)-2249de83dbcbfba1cca3b445024820a8")
   and ( "Patient has lab result with test name (specific for an organism or substance)-cfeae464f8e6f014b3988e3e78ce8318")
   and ( "Group 1-fa09b610216ab9fec6cc6146497cd8c3")
   or ( "Patient has a diagnosis of-4ea006366a2e66fd9161829801a6ca12")
