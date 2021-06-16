library Tuberculosis_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Tuberculosis (Mammalian Tuberculin Skin Test)" : 'https://hln.com/fhir/ValueSet/TUB007'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Tuberculosis (Interferon Gamma Release Assays)" : 'https://hln.com/fhir/ValueSet/TUB008'
valueset "Tuberculosis (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/TUB001'
valueset "Tuberculosis (Tests for Mycobacterium tuberculosis Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/TUB006'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Organisms (Tests by Microscopic Observation in Tissue by Acid Fast Stain)" : 'https://hln.com/fhir/ValueSet/LabTST014'
valueset "Acid Fast Bacilli (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/LabRLT009'
valueset "Tests by Microscopic Observation in Tissue" : 'https://hln.com/fhir/ValueSet/LabTST016'
valueset "Caseating Granuloma (Finding by Microscopic Observation in Lab Results)" : 'https://hln.com/fhir/ValueSet/TUB003'
valueset "Tuberculosis (Tests for Mycobacterium tuberculosis by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/TUB005'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'
valueset "Tuberculosis (Tests for Mycobacterium species by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/TUB009'
valueset "Azidothymidine (AZT)" : 'https://hln.com/fhir/ValueSet/MED001'
valueset "Dapsone" : 'https://hln.com/fhir/ValueSet/MED006'
valueset "Rifampin" : 'https://hln.com/fhir/ValueSet/MED007'
valueset "Clofazimine" : 'https://hln.com/fhir/ValueSet/MED008'
valueset "Opioid Overdose and Poisoning (Naloxone) (RxNorm)" : 'https://hln.com/fhir/ValueSet/NFO004'
valueset "Tetanus (Tetanus Immune Globulin)" : 'https://hln.com/fhir/ValueSet/MED009'
valueset "Bicillin (Penicillin G Benzathine)" : 'https://hln.com/fhir/ValueSet/MED002'
valueset "Hepatitis B Immune Globulin" : 'https://hln.com/fhir/ValueSet/HBV010'
valueset "Botulism Antitoxin (RXNORM)" : 'https://hln.com/fhir/ValueSet/BOT007'
valueset "Anthrax Vaccine (CVX)" : 'https://hln.com/fhir/ValueSet/ANT008'
valueset "Tuberculosis (Disorders)" : 'https://hln.com/fhir/ValueSet/TUB004'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Units:-6a8c133ab4516c3017d8748a5a3cbd1c":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) < 6 'years'

define "Patient has lab result with test name (specific for an organism or substance)-821f61a98b2d974aa2c8e1bae900584d":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Tuberculosis (Mammalian Tuberculin Skin Test)"
)

define "Lab Result Value (ordinal)-4dc7004f7f8dd329dc5a0b37b5c334e8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-efe7f2b5e3dc1cccb20ab4958155ccd3":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-2798aa8df9874e0926a9c082e1821107":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Tuberculosis (Interferon Gamma Release Assays)"
)

define "Lab Result Value (ordinal)-d39ec2837818d22632ad6839edd31d67":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-8e9afeb72482fd05a6aae1ce780b3454":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Tuberculosis (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-57e11a1386160dae7995cf0483ded99c":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-c4b157ba2f24caa42f9dfae679c82f65":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Tuberculosis (Tests for Mycobacterium tuberculosis Nucleic Acid)"
)

define "Lab Result Value (ordinal)-b25059ab67c33ae7faa4821756d7ec8d":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-1c45d46d7c32c69d753fb942601be556":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Tuberculosis (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-df7307b28a2695e5b309ad918d72eec8":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-93043391b8379250e0a791912aad8eea":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-243774028921f349b8ebac54e6696355":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Tuberculosis (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-9da7c241171d017c59fb8fcc509f9201":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests by Microscopic Observation in Tissue by Acid Fast Stain)"
)

define "Lab Result Value (ordinal)-9a61a1aede9765c7138571aa886dc387":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-759e57569b9d3671648ef9822d85dc48":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Acid Fast Bacilli (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-ba1efbc8ac2414bee23fe2d8db26a15c":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-6ac3ee183180c51adb23c9935ef684bc":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Tests by Microscopic Observation in Tissue"
)

define "Lab Result Value (nominal)-1a3f60168e3b539cfaf44f77c5d8c4e0":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Caseating Granuloma (Finding by Microscopic Observation in Lab Results)"
)

define "Lab Result Value (other nominal)-7751be58a69fc1c4c81e647f6375fde7":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Acid Fast Bacilli (Organism or Substance in Lab Results)"
)

define "Group 2.2-a81fd2c4be2a4608b81906b7fb806b23":
   "Lab Result Value (other nominal)-7751be58a69fc1c4c81e647f6375fde7"
   or ( "Lab Result Value (nominal)-1a3f60168e3b539cfaf44f77c5d8c4e0")

define "Group 2-895399afdbf9f167b2bca7cb144bb970":
   "Group 2.2-a81fd2c4be2a4608b81906b7fb806b23"
   or ( "Patient has lab result with test name (NOT specific for an organism or substance)-6ac3ee183180c51adb23c9935ef684bc")

define "Patient has lab result with test name (specific for an organism or substance)-50a34768d76b926da06c4b1839c06772":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Tuberculosis (Tests for Mycobacterium tuberculosis by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-363c4b8ef82a648a7367a05ca3adc50f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-e16a10442c1dc049dfb890c834dacecf":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Tuberculosis (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-17ad67114b832760157fb38cbd59ad25":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-db586f19cbcf3adc1f9c2643dd471612":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-a4b127c977e808a7a8b02ccbe82b7c73":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Tuberculosis (Tests for Mycobacterium species by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-6372401394f3059ad507d5babdb44e47":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Tuberculosis (Organism or Substance in Lab Results)"
)

define "Medication Administered-3818a028f4cc8a77e7445ded93214f91":
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
    where Alias in "Azidothymidine (AZT)"
)

define "Patient has a lab test order with test name-9c5ffc5efdd1a73ae99d5c0a92727abf":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Tuberculosis (Tests for Mycobacterium tuberculosis Nucleic Acid)"
)

define "Patient has a lab test order with test name-38f6ac497ed3fa7e7b2ab06f7c43fe32":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Tuberculosis (Tests for Mycobacterium tuberculosis by Culture and Identification Method)"
)

define "Patient has a lab test order with test name-e109c7a8ffe2a2395c9d57287f6e078d":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Tuberculosis (Tests for Mycobacterium species by Culture and Identification Method)"
)

define "Patient has medication ordered of-19435552a8984bb4e5bcd06b55264896":
  exists (
    (( 
      [MedicationRequest] M
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
    where Alias in "Dapsone"
)

define "Medication ID-249fb67fc594209b5393496b2b921611":
  exists (
    ({ 'TODO: Determine Modeling' }
  ) Alias
    where not  Equal( Alias, null)
)

define "Group 1-a2b24913b8ca1a2968860dbb5ab343f9":
   "Medication ID-249fb67fc594209b5393496b2b921611"
   and ( "Patient has medication ordered of-19435552a8984bb4e5bcd06b55264896")

define "substanceOrder2-8fd19c386a476bb36bec59b1b2bd632f":
  exists (
    (( 
      [MedicationRequest] M
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
    where Alias in "Rifampin"
)

define "substanceOrder2-f5fc933e72ae7b9880919c6c4c348c4f":
  exists (
    ({ 'TODO: Determine Modeling' }
  ) Alias
    where not  Equal( Alias, null)
)

define "Group 2-3cf1911e51b8e0829af5e4639a7340da":
   "substanceOrder2-f5fc933e72ae7b9880919c6c4c348c4f"
   and ( "substanceOrder2-8fd19c386a476bb36bec59b1b2bd632f")

define "substanceOrder3-f4f500479f52b8aa906e0b19dd631bfb":
  exists (
    (( 
      [MedicationRequest] M
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
    where Alias in "Clofazimine"
)

define "substanceOrder3-e09db6d9919afe2f8d31a474d55b77a4":
  exists (
    ({ 'TODO: Determine Modeling' }
  ) Alias
    where not  Equal( Alias, null)
)

define "Group 3-6657536281f4ae7224377478656a47fe":
   "substanceOrder3-e09db6d9919afe2f8d31a474d55b77a4"
   and ( "substanceOrder3-f4f500479f52b8aa906e0b19dd631bfb")

define "first-a2fc807f9a6eab2cc8d06e15a81f49ea":
  exists (
    (( 
      [MedicationRequest] M
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
    where Alias in "Dapsone"
)

define "second-e8fcee19e493ae4dd248da309d2099cb":
  exists (
    (( 
      [MedicationRequest] M
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
    where Alias in "Rifampin"
)

define "Medication Administered-85929c02d1f35520aab4a6e9523e8f68":
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
    where Alias in "Rifampin"
)

define "Medication Administered-e77dfb5eeb7a4485f5e7e3d030773ef5":
  exists (
    (( 
      [MedicationRequest] M
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
    where Alias in "Dapsone"
)

define "# of medications administered from the following:-26691f9cd2349d4d1c0abdab52af365b":
  ToDecimal(Count(distinct (
    
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
  ))distinct (
    
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
  ))Count(distinct (
    
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
  ))distinct (
    
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
  ) >= 2.0

define "Patient has medication ordered of-21e3a455556d022a6c60355b1677abdb":
  exists (
    (( 
      [MedicationRequest] M
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
    where Alias in "Dapsone"
)

define "Medication ID-9cc892e1f33391721dc1721e2855d2f2":
  exists (
    ({ 'TODO: Determine Modeling' }
  ) Alias
    where not  Equal( Alias, null)
)

define "Group 1-b42e6c65189f81b1e23f0fe2617df5bf":
   "Medication ID-9cc892e1f33391721dc1721e2855d2f2"
   and ( "Patient has medication ordered of-21e3a455556d022a6c60355b1677abdb")

define "substanceOrder2-10d46d073be769ee91e13d1fac5ced03":
  exists (
    ({ 'TODO: Determine Modeling' }
  ) Alias
    where not  Equal( Alias, null)
)

define "Group 2-8d6e523a7341ba8fa0ee79fc40a8395a":
   "substanceOrder2-10d46d073be769ee91e13d1fac5ced03"
   and ( "Group 1-b42e6c65189f81b1e23f0fe2617df5bf")

define "substanceOrder3-76b1c10cce685522a8464f6447ee0197":
  exists (
    ({ 'TODO: Determine Modeling' }
  ) Alias
    where not  Equal( Alias, null)
)

define "Group 3-27cef34ec926694df124cc54ca12a82c":
   "substanceOrder3-76b1c10cce685522a8464f6447ee0197"
   and ( "Group 2-8d6e523a7341ba8fa0ee79fc40a8395a")

define "Patient has a diagnosis of-d54b6788fb3e2056559e264d5959c2e6":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Tuberculosis (Disorders)"
)

define "Patient has a problem of-79996349bc5a4378b73364ad34c3ab4d":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Tuberculosis (Disorders)"
)

define "Problem has status of-b19308caa007d2daab4b7ebdfbd0f732":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-f9e5ee56536caca367b06f88787e69f9":
   "Problem has status of-b19308caa007d2daab4b7ebdfbd0f732"
   and ( "Patient has a problem of-79996349bc5a4378b73364ad34c3ab4d")

define "ConditionCriteriaMet":
   "Group 1-f9e5ee56536caca367b06f88787e69f9"
   or ( "Patient has a diagnosis of-d54b6788fb3e2056559e264d5959c2e6")
   and ( "Group 3-27cef34ec926694df124cc54ca12a82c")
   or ( "# of medications administered from the following:-26691f9cd2349d4d1c0abdab52af365b")
   and ( "Medication Administered-e77dfb5eeb7a4485f5e7e3d030773ef5")
   and ( "Medication Administered-85929c02d1f35520aab4a6e9523e8f68")
   and ( "second-e8fcee19e493ae4dd248da309d2099cb")
   and ( "first-a2fc807f9a6eab2cc8d06e15a81f49ea")
   and ( "Group 3-6657536281f4ae7224377478656a47fe")
   and ( "Group 2-3cf1911e51b8e0829af5e4639a7340da")
   and ( "Group 1-a2b24913b8ca1a2968860dbb5ab343f9")
   or ( "Patient has a lab test order with test name-e109c7a8ffe2a2395c9d57287f6e078d")
   or ( "Patient has a lab test order with test name-38f6ac497ed3fa7e7b2ab06f7c43fe32")
   or ( "Patient has a lab test order with test name-9c5ffc5efdd1a73ae99d5c0a92727abf")
   and ( "Medication Administered-3818a028f4cc8a77e7445ded93214f91")
   or ( "Lab Result Value (nominal)-6372401394f3059ad507d5babdb44e47")
   or ( "Patient has lab result with test name (NOT specific for an organism or substance)-a4b127c977e808a7a8b02ccbe82b7c73")
   or ( "Patient has lab result with test name (NOT specific for an organism or substance)-db586f19cbcf3adc1f9c2643dd471612")
   or ( "Lab Result Interpretation-17ad67114b832760157fb38cbd59ad25")
   or ( "Lab Result Value (nominal)-e16a10442c1dc049dfb890c834dacecf")
   or ( "Lab Result Value (ordinal)-363c4b8ef82a648a7367a05ca3adc50f")
   and ( "Patient has lab result with test name (specific for an organism or substance)-50a34768d76b926da06c4b1839c06772")
   and ( "Group 2-895399afdbf9f167b2bca7cb144bb970")
   or ( "Lab Result Interpretation-ba1efbc8ac2414bee23fe2d8db26a15c")
   or ( "Lab Result Value (nominal)-759e57569b9d3671648ef9822d85dc48")
   or ( "Lab Result Value (ordinal)-9a61a1aede9765c7138571aa886dc387")
   and ( "Patient has lab result with test name (specific for an organism or substance)-9da7c241171d017c59fb8fcc509f9201")
   or ( "Lab Result Value (nominal)-243774028921f349b8ebac54e6696355")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-93043391b8379250e0a791912aad8eea")
   or ( "Lab Result Interpretation-df7307b28a2695e5b309ad918d72eec8")
   or ( "Lab Result Value (nominal)-1c45d46d7c32c69d753fb942601be556")
   or ( "Lab Result Value (ordinal)-b25059ab67c33ae7faa4821756d7ec8d")
   and ( "Patient has lab result with test name (specific for an organism or substance)-c4b157ba2f24caa42f9dfae679c82f65")
   or ( "Lab Result Interpretation-57e11a1386160dae7995cf0483ded99c")
   or ( "Lab Result Value (nominal)-8e9afeb72482fd05a6aae1ce780b3454")
   or ( "Lab Result Value (ordinal)-d39ec2837818d22632ad6839edd31d67")
   and ( "Patient has lab result with test name (specific for an organism or substance)-2798aa8df9874e0926a9c082e1821107")
   or ( "Lab Result Interpretation-efe7f2b5e3dc1cccb20ab4958155ccd3")
   or ( "Lab Result Value (ordinal)-4dc7004f7f8dd329dc5a0b37b5c334e8")
   and ( "Patient has lab result with test name (specific for an organism or substance)-821f61a98b2d974aa2c8e1bae900584d")
   and ( "Units:-6a8c133ab4516c3017d8748a5a3cbd1c")
