library Gonorrhea_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Gonorrhea (Tests for Neisseria gonorrhoeae Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/GON007'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Gonorrhea (Tests for Neisseria gonorrhoeae by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/GON006'
valueset "Gonorrhea (Tests for Neisseria species by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/GON005'
valueset "Gonorrhea [Neisseria gonorrhoeae] (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/GON001'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Gonorrhea [Cervicitis Urethritis] (Disorders)" : 'https://hln.com/fhir/ValueSet/GON004'
valueset "Gonorrhea [Conjunctivitis] (Disorders)" : 'https://hln.com/fhir/ValueSet/GON003'
valueset "Gonorrhea [Secondary Sites Complications Chronic] (Disorders)" : 'https://hln.com/fhir/ValueSet/GON002'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'
valueset "Gonorrhea [Neisseria species Unspecified] (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/GON008'
valueset "Indeterminate or Equivocal Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT011'
valueset "Bacteria (Tests by Microscopic Observation of Genital and Urethral Specimens by Gram Stain)" : 'https://hln.com/fhir/ValueSet/LabTST006'
valueset "Negative or Undetected Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT012'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-4fbded97269f8665f57e8038e69da0ef":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria gonorrhoeae Nucleic Acid)"
)

define "Lab Result Value (ordinal)-a81cfe40941f2b55325c6bb6262e23ab":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-3e971bea24a05dc6120e71a23a12f8bf":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-fa8716271572444f83afb401b3dca6d9":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria gonorrhoeae by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-5b15c447e859debf210fd10690cc83b6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-faf3e14a86302f37f252c7588fc45f66":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-d34a0160128bf1b75e7d168eadd19a1c":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria species by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-67b76a1d7405435d894c153ab05fd0ca":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-4afe7192eae451e3543686aa449d3054":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-d2952295a08ca04b2631b082679271e4":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria gonorrhoeae Nucleic Acid)"
)

define "Lab Result Value (ordinal)-1534d526b7f55282fb537e8e47b42f59":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-2f8049f25aec1dbe60691cc95243fd8b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Gonorrhea [Neisseria gonorrhoeae] (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-abc0d175a2da5cafbf36e828f27cc23e":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-523c36738450a6cbad96766948b54463":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-f816c19cd09cdbcbe6ad75438181dda0":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Gonorrhea [Neisseria gonorrhoeae] (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-e65d53d2132e9232a22b74df695ca474":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Gonorrhea [Cervicitis Urethritis] (Disorders)"
)

define "Patient has a problem of-71fd8d69d2d2d3c360e349c6c6875683":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Gonorrhea [Cervicitis Urethritis] (Disorders)"
)

define "Problem has status of-b52642d3c06a952dbd19a8f37b58377b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-f3848153c348bdd9423c0852145129f9":
   "Problem has status of-b52642d3c06a952dbd19a8f37b58377b"
   and ( "Patient has a problem of-71fd8d69d2d2d3c360e349c6c6875683")

define "Patient has a diagnosis of-7403094ea58cefe26fe4c8c8d8d2e232":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Gonorrhea [Conjunctivitis] (Disorders)"
)

define "Patient has a problem of-9a6768b730343fe681dba7d55e1a8270":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Gonorrhea [Conjunctivitis] (Disorders)"
)

define "Problem has status of-8f001c38b836dc6d7d746de8f9006379":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-3f848143bb37c236af878710c2358964":
   "Problem has status of-8f001c38b836dc6d7d746de8f9006379"
   and ( "Patient has a problem of-9a6768b730343fe681dba7d55e1a8270")

define "Patient has a diagnosis of-adff12a6c346e95e6af7a610e59e1271":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Gonorrhea [Secondary Sites Complications Chronic] (Disorders)"
)

define "Patient has a problem of-43cfc1e7a8dca229ca1906cc41c52050":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Gonorrhea [Secondary Sites Complications Chronic] (Disorders)"
)

define "Problem has status of-7075baaae435fcdbc3fa71d41bb629b6":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-36b4d4260fda66546a12d8cc65cc6fa9":
   "Problem has status of-7075baaae435fcdbc3fa71d41bb629b6"
   and ( "Patient has a problem of-43cfc1e7a8dca229ca1906cc41c52050")

define "Patient has lab result with test name (specific for an organism or substance)-269bdce4d6e20f789ae62918f9b6dd9e":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria gonorrhoeae by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-12605ead99d3d613c4def517c3577849":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-5f8e389dc4b05da83aba9fefecfe9be8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Gonorrhea [Neisseria gonorrhoeae] (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-a83cafc661ce67bd168804ca15d000f9":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-4fe79383459f1acf775d19cd86a2046a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-e5238b443a6548913d64e4aeee358ffc":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Gonorrhea [Neisseria gonorrhoeae] (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-50dcc3bbc6a14f31697ae074ce096323":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria species by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-c8bf4f7f89ea0519367d3e98fe9dbc75":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-75af0d89d3b3ab511bd4d9466c935c7c":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Gonorrhea [Neisseria species Unspecified] (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-48cc02dd312caa99d58663425276849f":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Lab Result Value (other nominal)-2deb2cf8f5e0b6a1bae3ec7c5586112a":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Gonorrhea [Neisseria gonorrhoeae] (Organism or Substance in Lab Results)"
)

define "Group 1.1-1a08524c108f7b10234c94abe24dfea2":
   "Lab Result Value (other nominal)-2deb2cf8f5e0b6a1bae3ec7c5586112a"
   or ( "Lab Result Interpretation-48cc02dd312caa99d58663425276849f")
   or ( "Lab Result Value (nominal)-75af0d89d3b3ab511bd4d9466c935c7c")
   or ( "Lab Result Value (ordinal)-c8bf4f7f89ea0519367d3e98fe9dbc75")

define "Group 1-ebd30d1ea988f539e13b217d5923fd40":
   "Group 1.1-1a08524c108f7b10234c94abe24dfea2"
   and ( "Patient has lab result with test name (specific for an organism or substance)-50dcc3bbc6a14f31697ae074ce096323")

define "Patient has lab result with test name (NOT specific for an organism or substance)-e0f416f8805f6796adbeef6eb31a6b96":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-45ebfb01451436b49de7cca8c5970aff":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Gonorrhea [Neisseria gonorrhoeae] (Organism or Substance in Lab Results)"
)

define "Lab Result Value (other nominal)-1351a7b6507ae1533a80f849478023de":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Gonorrhea [Neisseria species Unspecified] (Organism or Substance in Lab Results)"
)

define "Group 2.1-51d94ecc7579dd80fc37a6e272506c6b":
   "Lab Result Value (other nominal)-1351a7b6507ae1533a80f849478023de"
   or ( "Lab Result Value (nominal)-45ebfb01451436b49de7cca8c5970aff")

define "Group 2-b1595547670f51b54a0afb72766c86de":
   "Group 2.1-51d94ecc7579dd80fc37a6e272506c6b"
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-e0f416f8805f6796adbeef6eb31a6b96")

define "Patient has lab result with test name (specific for an organism or substance)-a37b91369da61c8cf22d4320ce6b2c11":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria gonorrhoeae Nucleic Acid)"
)

define "Lab Result Value (ordinal)-f57550c9491311f3be8b0eca3b85377c":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Indeterminate or Equivocal Lab Result Value"
)

define "Lab Result Interpretation-521ea8907dac715b5d13bb39402b29fa":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-e77128c72010c0a199fee9a8d7cbe632":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria gonorrhoeae by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-5b177366e721e4018e684f2298246c16":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Indeterminate or Equivocal Lab Result Value"
)

define "Lab Result Interpretation-75e69ceddec1d72032cf6b5254d2fe72":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-dd2dad1490d07ddd62f1e5f684975e90":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Microscopic Observation of Genital and Urethral Specimens by Gram Stain)"
)

define "Lab Result Value (ordinal)-73f5b2df375750afc18005deb15405b4":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-8398d28c2f8025cc2ee94aa945fabc99":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Gonorrhea [Neisseria gonorrhoeae] (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-fdf8a82848d85fd8e0ef0c391f3259a6":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-26064012b85d841c687365fa535bbcbc":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria gonorrhoeae Nucleic Acid)"
)

define "Lab result value of (ordinal)-e414a317f2ad3345cd9b4d3d44306adc":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Patient has lab result with test name (specific for an organism or substance)-c3bdf5842ab0a1e7ee4559729f14f6c2":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria gonorrhoeae by Culture and Identification Method)"
)

define "Lab result value of (ordinal)-84bf591b664141395132f0909eb5c11a":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Patient has lab result with test name (specific for an organism or substance)-9e1eac0e87929a66c40fd039a4229f8a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Gonorrhea (Tests for Neisseria species by Culture and Identification Method)"
)

define "Lab result value of (ordinal)-e917e58590731fe54e8b5195a14c9e4e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Units:-14c163407c5c1a3bf538e2149050b8f1":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) < 1 'years'

define "Units:-b527d2076cd6a9d08872326275c5976f":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) >= 1 'years'

define "ConditionCriteriaMet":
   "Units:-b527d2076cd6a9d08872326275c5976f"
   and ( "Units:-14c163407c5c1a3bf538e2149050b8f1")
   and ( "Lab result value of (ordinal)-e917e58590731fe54e8b5195a14c9e4e")
   and ( "Patient has lab result with test name (specific for an organism or substance)-9e1eac0e87929a66c40fd039a4229f8a")
   and ( "Lab result value of (ordinal)-84bf591b664141395132f0909eb5c11a")
   and ( "Patient has lab result with test name (specific for an organism or substance)-c3bdf5842ab0a1e7ee4559729f14f6c2")
   and ( "Lab result value of (ordinal)-e414a317f2ad3345cd9b4d3d44306adc")
   and ( "Patient has lab result with test name (specific for an organism or substance)-26064012b85d841c687365fa535bbcbc")
   or ( "Lab Result Interpretation-fdf8a82848d85fd8e0ef0c391f3259a6")
   or ( "Lab Result Value (nominal)-8398d28c2f8025cc2ee94aa945fabc99")
   or ( "Lab Result Value (ordinal)-73f5b2df375750afc18005deb15405b4")
   and ( "Patient has lab result with test name (specific for an organism or substance)-dd2dad1490d07ddd62f1e5f684975e90")
   or ( "Lab Result Interpretation-75e69ceddec1d72032cf6b5254d2fe72")
   or ( "Lab Result Value (ordinal)-5b177366e721e4018e684f2298246c16")
   and ( "Patient has lab result with test name (specific for an organism or substance)-e77128c72010c0a199fee9a8d7cbe632")
   or ( "Lab Result Interpretation-521ea8907dac715b5d13bb39402b29fa")
   or ( "Lab Result Value (ordinal)-f57550c9491311f3be8b0eca3b85377c")
   and ( "Patient has lab result with test name (specific for an organism or substance)-a37b91369da61c8cf22d4320ce6b2c11")
   or ( "Group 2-b1595547670f51b54a0afb72766c86de")
   or ( "Group 1-ebd30d1ea988f539e13b217d5923fd40")
   or ( "Lab Result Value (nominal)-e5238b443a6548913d64e4aeee358ffc")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-4fe79383459f1acf775d19cd86a2046a")
   or ( "Lab Result Interpretation-a83cafc661ce67bd168804ca15d000f9")
   or ( "Lab Result Value (nominal)-5f8e389dc4b05da83aba9fefecfe9be8")
   or ( "Lab Result Value (ordinal)-12605ead99d3d613c4def517c3577849")
   and ( "Patient has lab result with test name (specific for an organism or substance)-269bdce4d6e20f789ae62918f9b6dd9e")
   and ( "Group 1-36b4d4260fda66546a12d8cc65cc6fa9")
   or ( "Patient has a diagnosis of-adff12a6c346e95e6af7a610e59e1271")
   and ( "Group 1-3f848143bb37c236af878710c2358964")
   or ( "Patient has a diagnosis of-7403094ea58cefe26fe4c8c8d8d2e232")
   and ( "Group 1-f3848153c348bdd9423c0852145129f9")
   or ( "Patient has a diagnosis of-e65d53d2132e9232a22b74df695ca474")
   or ( "Lab Result Value (nominal)-f816c19cd09cdbcbe6ad75438181dda0")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-523c36738450a6cbad96766948b54463")
   or ( "Lab Result Interpretation-abc0d175a2da5cafbf36e828f27cc23e")
   or ( "Lab Result Value (nominal)-2f8049f25aec1dbe60691cc95243fd8b")
   or ( "Lab Result Value (ordinal)-1534d526b7f55282fb537e8e47b42f59")
   and ( "Patient has lab result with test name (specific for an organism or substance)-d2952295a08ca04b2631b082679271e4")
   or ( "Lab Result Interpretation-4afe7192eae451e3543686aa449d3054")
   or ( "Lab Result Value (ordinal)-67b76a1d7405435d894c153ab05fd0ca")
   and ( "Patient has lab result with test name (specific for an organism or substance)-d34a0160128bf1b75e7d168eadd19a1c")
   or ( "Lab Result Interpretation-faf3e14a86302f37f252c7588fc45f66")
   or ( "Lab Result Value (ordinal)-5b15c447e859debf210fd10690cc83b6")
   and ( "Patient has lab result with test name (specific for an organism or substance)-fa8716271572444f83afb401b3dca6d9")
   or ( "Lab Result Interpretation-3e971bea24a05dc6120e71a23a12f8bf")
   or ( "Lab Result Value (ordinal)-a81cfe40941f2b55325c6bb6262e23ab")
   and ( "Patient has lab result with test name (specific for an organism or substance)-4fbded97269f8665f57e8038e69da0ef")
