library Chlamydial_infection_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Chlamydia trachomatis Infection (Tests for Chlamydia species Antigen)" : 'https://hln.com/fhir/ValueSet/CHL009'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Chlamydia trachomatis Infection (Tests for Chlamydia species Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/CHL008'
valueset "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis Antigen)" : 'https://hln.com/fhir/ValueSet/CHL005'
valueset "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/CHL010'
valueset "Chlamydia trachomatis Infection (Tests for Chlamydia species by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/CHL007'
valueset "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/CHL006'
valueset "Chlamydia trachomatis Infection [Cervicitis Urethritis LGV] (Disorders)" : 'https://hln.com/fhir/ValueSet/CHL002'
valueset "Chlamydia trachomatis Infection [Conjunctivitis] (Disorders)" : 'https://hln.com/fhir/ValueSet/CHL004'
valueset "Chlamydia trachomatis Infection [Secondary Sites Complications] (Disorders)" : 'https://hln.com/fhir/ValueSet/CHL003'
valueset "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/CHL001'
valueset "Chlamydia species (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/CHL011'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Bacteria (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST001'
valueset "Indeterminate or Equivocal Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT011'
valueset "Negative or Undetected Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT012'
valueset "Trachoma (Disorders)" : 'https://hln.com/fhir/ValueSet/TRA001'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-e7199e8c181aa9461f4584ec810468e9":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia species Antigen)"
)

define "Lab Result Value (ordinal)-b941a484379bd7fd89e6be067cf66f47":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-2f5185d71a9937fe531157942a913f37":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-449d57739d7053ff4bab75913b00edaa":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia species Nucleic Acid)"
)

define "Lab Result Value (ordinal)-9c09b43e7bbac398f5122a0fa00d6cf8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-3a03af04ef1d8749826d96ee0d67ab2c":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-8a71972d4db2a23275a1b7d3f2d24f11":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis Antigen)"
)

define "Lab Result Value (ordinal)-c392c969dfbc62c5db5e2162a799a754":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-82fec239e022522bf9dd18d560f413d3":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-e75d770da0376e8594fea8fe7b8e1df6":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis Nucleic Acid)"
)

define "Lab Result Value (ordinal)-a679e18398d78d174a3acfa6e29765e4":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-35103f6e5dc04b639a2d97d862bf5eca":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-3186363ba02360d86e149fd8882ba6f1":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia species by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-59af640165484a4366dd80534a722397":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-36634deeac7086925407e82764880d16":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-e925fd5553cc80755573aa9d1edb471a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-4557fee6dc7c802c18e4da6b7db99985":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Interpretation-2133a1f9d43f00414d81e93962e96413":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has a diagnosis of-d2017b8afb227480e66fea2554a8cf06":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection [Cervicitis Urethritis LGV] (Disorders)"
)

define "Patient has a problem of-1b154e0567892ff0f526dd4e4caf5027":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection [Cervicitis Urethritis LGV] (Disorders)"
)

define "Problem has status of-ee1c8505e65954d1ab8b13eca89548ab":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-29f66fbbdde200b0f23ef3ce5cc8c5e8":
   "Problem has status of-ee1c8505e65954d1ab8b13eca89548ab"
   and ( "Patient has a problem of-1b154e0567892ff0f526dd4e4caf5027")

define "Patient has a diagnosis of-73bc5379a6c8afbeb8fb265dbecb9368":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection [Conjunctivitis] (Disorders)"
)

define "Patient has a problem of-cda5d0fb92eed59fa5446bacfae3144d":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection [Conjunctivitis] (Disorders)"
)

define "Problem has status of-a9e88706676cdbae6f2af5eb9178f067":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-1bd05d1848cdad2cc3d093c54a88d4b7":
   "Problem has status of-a9e88706676cdbae6f2af5eb9178f067"
   and ( "Patient has a problem of-cda5d0fb92eed59fa5446bacfae3144d")

define "Patient has a diagnosis of-7712dd616df3bd1de0b99ca9eec795e4":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection [Secondary Sites Complications] (Disorders)"
)

define "Patient has a problem of-3f39b65d23ab3329cfea39f0134da69b":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection [Secondary Sites Complications] (Disorders)"
)

define "Problem has status of-679442a8de5dd3c46ada39c9251b3995":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-ddcbe9e6ca9e167cb51bf80aa097aea3":
   "Problem has status of-679442a8de5dd3c46ada39c9251b3995"
   and ( "Patient has a problem of-3f39b65d23ab3329cfea39f0134da69b")

define "Patient has lab result with test name (specific for an organism or substance)-714353fcfbac2acc990c9acf43f489e6":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia species Antigen)"
)

define "Lab Result Value (ordinal)-dcb36dd93e7242a582674601a2d83031":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-e3b9495b44651f9999de1b1af5d0c508":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-f90930cfa8b5195184bcb7f4a4497c7c":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Lab Result Value (other nominal)-279317f34c5fa1617d5ea502492c1e73":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia species (Organism or Substance in Lab Results)"
)

define "Group 1.1-dc38732243628cc547d83c9363f49be0":
   "Lab Result Value (other nominal)-279317f34c5fa1617d5ea502492c1e73"
   or ( "Lab Result Interpretation-f90930cfa8b5195184bcb7f4a4497c7c")
   or ( "Lab Result Value (nominal)-e3b9495b44651f9999de1b1af5d0c508")
   or ( "Lab Result Value (ordinal)-dcb36dd93e7242a582674601a2d83031")

define "Group 1-f7cf704e5037d89bbb7359128b8c745d":
   "Group 1.1-dc38732243628cc547d83c9363f49be0"
   and ( "Patient has lab result with test name (specific for an organism or substance)-714353fcfbac2acc990c9acf43f489e6")

define "Patient has lab result with test name (specific for an organism or substance)-5c75a5012b1005801fe9ef873aa0d993":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia species Nucleic Acid)"
)

define "Lab Result Value (ordinal)-a8ec1a2c7980d8d41d1b43dd44f5b49c":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-53e6ce58396930cd3ae40b5cb359b5df":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-9a0166b4434a87e8193b63fe637aeb4c":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Lab Result Value (other nominal)-fb6d131ef3b1151fa912cdf1a1d92efc":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia species (Organism or Substance in Lab Results)"
)

define "Group 1.1-69314f998213f8fa0eb25be2e165c283":
   "Lab Result Value (other nominal)-fb6d131ef3b1151fa912cdf1a1d92efc"
   or ( "Lab Result Interpretation-9a0166b4434a87e8193b63fe637aeb4c")
   or ( "Lab Result Value (nominal)-53e6ce58396930cd3ae40b5cb359b5df")
   or ( "Lab Result Value (ordinal)-a8ec1a2c7980d8d41d1b43dd44f5b49c")

define "Group 1-ecfd4281493b01edcecb6b772c278dc6":
   "Group 1.1-69314f998213f8fa0eb25be2e165c283"
   and ( "Patient has lab result with test name (specific for an organism or substance)-5c75a5012b1005801fe9ef873aa0d993")

define "Patient has lab result with test name (NOT specific for an organism or substance)-305e600beddbc76a46a4fd029e00e99e":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-b7870588f1206190fcae2364a3405c44":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Value (other nominal)-aef1fd3d320f6792bfc032d6a923563b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia species (Organism or Substance in Lab Results)"
)

define "Group 2.1-4110a2a8a8fa0497a28fbf9f4d0b65f4":
   "Lab Result Value (other nominal)-aef1fd3d320f6792bfc032d6a923563b"
   or ( "Lab Result Value (nominal)-b7870588f1206190fcae2364a3405c44")

define "Group 2-5830f09983d6efa5587d34c499c3f658":
   "Group 2.1-4110a2a8a8fa0497a28fbf9f4d0b65f4"
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-305e600beddbc76a46a4fd029e00e99e")

define "Patient has lab result with test name (specific for an organism or substance)-2c16a2163b921d662cae73ed255a35c0":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis Antigen)"
)

define "Lab Result Value (ordinal)-8be6c2199a345f2a8070bf93785faa0a":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-56e9de0426adf6a6d899cc7d679e957e":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-be064ee58debdae3549bc2c722a12aa6":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-c6a50ca88efd86457377838f8f8a0861":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis Nucleic Acid)"
)

define "Lab Result Value (ordinal)-c824c76da72f4c97aae7b1b951ce5c43":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-c77be3269c97a0b7ac81cb681b0dbad5":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-b3c300971a80c71f39dab4ab67e61f4b":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-31958601187043baf45d43ab4da76302":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-c76780819aff12196ef4069bca455eb2":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-e419c33f52b98d1a924dcac82700e171":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia species by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-9093f2ea5eb9b20a77f9ecd32fcce7d0":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-5d38a77365d8d1881ed35b3bc63012ed":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-c5d86ee595740d4819800dcf8927c2c7":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Lab Result Value (other nominal)-fce4869f1f2c7500b0c0279d3b1ac0a3":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia species (Organism or Substance in Lab Results)"
)

define "Group 1.1-a829771b5943807990b76d2bdb5bde0d":
   "Lab Result Value (other nominal)-fce4869f1f2c7500b0c0279d3b1ac0a3"
   or ( "Lab Result Interpretation-c5d86ee595740d4819800dcf8927c2c7")
   or ( "Lab Result Value (nominal)-5d38a77365d8d1881ed35b3bc63012ed")
   or ( "Lab Result Value (ordinal)-9093f2ea5eb9b20a77f9ecd32fcce7d0")

define "Group 1-7e2848372e0f7fca6f1fedfc8d56aa6f":
   "Group 1.1-a829771b5943807990b76d2bdb5bde0d"
   and ( "Patient has lab result with test name (specific for an organism or substance)-e419c33f52b98d1a924dcac82700e171")

define "Patient has lab result with test name (NOT specific for an organism or substance)-11c4b420704df3e2a3a0f456619b84df":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-44740449f7ebb4bb514c68883dc84934":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Value (other nominal)-ac2f139ff903cf49de97d689207b783c":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia species (Organism or Substance in Lab Results)"
)

define "Group 2.1-e221ba6966b50cbbf61b44158319e722":
   "Lab Result Value (other nominal)-ac2f139ff903cf49de97d689207b783c"
   or ( "Lab Result Value (nominal)-44740449f7ebb4bb514c68883dc84934")

define "Group 2-4f49cd354512a19020000587c69e8d8c":
   "Group 2.1-e221ba6966b50cbbf61b44158319e722"
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-11c4b420704df3e2a3a0f456619b84df")

define "Patient has lab result with test name (specific for an organism or substance)-fde09bf4a60fa79bfb56c11f65541e0e":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-a6be054280f9738d9b2ddc9dbee138db":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-3b7b0e657f436cb0581f503e33d8b65c":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-ddaf733862ea33053205e4e1952ddbd9":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-761d0576280c690a3f110b4ad2e3251a":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Bacteria (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-74d1d577792af48f61c6f9e2b184f41b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-61bef094563a83a4d5192e2d94e45702":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis Antigen)"
)

define "Lab Result Value (ordinal)-fd118a6a64d30b0b20c71b3131431f13":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Indeterminate or Equivocal Lab Result Value"
)

define "Lab Result Interpretation-86956006e4aaf749eac1cf61767a15b4":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-584b4c0bace00d086f033895767e60f3":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis Nucleic Acid)"
)

define "Lab Result Value (ordinal)-ac491f2539a0b4b6cb7e9572dec20e16":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Indeterminate or Equivocal Lab Result Value"
)

define "Lab Result Interpretation-a9256bd5d17f515e8275e15396e6ae3b":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-79905b734a3b4c8328c1435726c10066":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-35e079b86af54c4b19c687aa30c43dc8":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Indeterminate or Equivocal Lab Result Value"
)

define "Lab Result Interpretation-cb827f4d4acf09cc117dbba2b9ead8ab":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-7fa8c0711efd47422d1a8ec9cd406a75":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia species Antigen)"
)

define "Lab result value of (ordinal)-12b9df6ff2f152bd3c5ba2e72aaff0a2":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Patient has lab result with test name (specific for an organism or substance)-235a097039434c10802b03bd12c24250":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia species Nucleic Acid)"
)

define "Lab result value of (ordinal)-681a968317f921ac8852c8d783f31edb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Patient has lab result with test name (specific for an organism or substance)-f687047482a9d7ed7b97e50245092d01":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis Antigen)"
)

define "Lab result value of (ordinal)-b17a4109e56457bceba68bd74c4f5eab":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Patient has lab result with test name (specific for an organism or substance)-7957e9c7dfcfdbfaf516dcbd94dc36cf":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis Nucleic Acid)"
)

define "Lab result value of (ordinal)-d7d88f9c64874c0a64b8e9ce9b3f9d91":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Patient has lab result with test name (specific for an organism or substance)-52515c63613668094d452ec0418a2826":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia species by Culture and Identification Method)"
)

define "Lab result value of (ordinal)-f30fbeb9f50cedb1243ff10bd63c1ceb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Patient has lab result with test name (specific for an organism or substance)-593526149ce7ee9a3250ec262d55d35d":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Chlamydia trachomatis Infection (Tests for Chlamydia trachomatis by Culture and Identification Method)"
)

define "Lab result value of (ordinal)-66d97d3587c06d65d15b813bde2ed8f6":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Negative or Undetected Lab Result Value"
)

define "Units:-81c2ddf471455503cd0e601e8f83331b":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) < 1 'years'

define "Units:-93c0d4585ea2869854acb780b002b2cb":
  ToQuantity( CalculateAgeAt( "Patient".birthDate,  Today())) CalculateAgeAt( "Patient".birthDate,  Today()) >= 1 'years'

define "Patient has a diagnosis of-8e40f4a04842833f835fbb579ae6c935":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Trachoma (Disorders)"
)

define "Patient has a problem of-be4853cab42ea6c14ba85eb0766cc437":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Trachoma (Disorders)"
)

define "Problem has status of-e3af11292af3cfe0fcce0e9667bd4508":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-69889598fec82cde3ac64ecffe849665":
   "Problem has status of-e3af11292af3cfe0fcce0e9667bd4508"
   and ( "Patient has a problem of-be4853cab42ea6c14ba85eb0766cc437")

define "ConditionCriteriaMet":
   "Group 1-69889598fec82cde3ac64ecffe849665"
   or ( "Patient has a diagnosis of-8e40f4a04842833f835fbb579ae6c935")
   and ( "Units:-93c0d4585ea2869854acb780b002b2cb")
   and ( "Units:-81c2ddf471455503cd0e601e8f83331b")
   and ( "Lab result value of (ordinal)-66d97d3587c06d65d15b813bde2ed8f6")
   and ( "Patient has lab result with test name (specific for an organism or substance)-593526149ce7ee9a3250ec262d55d35d")
   and ( "Lab result value of (ordinal)-f30fbeb9f50cedb1243ff10bd63c1ceb")
   and ( "Patient has lab result with test name (specific for an organism or substance)-52515c63613668094d452ec0418a2826")
   and ( "Lab result value of (ordinal)-d7d88f9c64874c0a64b8e9ce9b3f9d91")
   and ( "Patient has lab result with test name (specific for an organism or substance)-7957e9c7dfcfdbfaf516dcbd94dc36cf")
   and ( "Lab result value of (ordinal)-b17a4109e56457bceba68bd74c4f5eab")
   and ( "Patient has lab result with test name (specific for an organism or substance)-f687047482a9d7ed7b97e50245092d01")
   and ( "Lab result value of (ordinal)-681a968317f921ac8852c8d783f31edb")
   and ( "Patient has lab result with test name (specific for an organism or substance)-235a097039434c10802b03bd12c24250")
   and ( "Lab result value of (ordinal)-12b9df6ff2f152bd3c5ba2e72aaff0a2")
   and ( "Patient has lab result with test name (specific for an organism or substance)-7fa8c0711efd47422d1a8ec9cd406a75")
   or ( "Lab Result Interpretation-cb827f4d4acf09cc117dbba2b9ead8ab")
   or ( "Lab Result Value (ordinal)-35e079b86af54c4b19c687aa30c43dc8")
   and ( "Patient has lab result with test name (specific for an organism or substance)-79905b734a3b4c8328c1435726c10066")
   or ( "Lab Result Interpretation-a9256bd5d17f515e8275e15396e6ae3b")
   or ( "Lab Result Value (ordinal)-ac491f2539a0b4b6cb7e9572dec20e16")
   and ( "Patient has lab result with test name (specific for an organism or substance)-584b4c0bace00d086f033895767e60f3")
   or ( "Lab Result Interpretation-86956006e4aaf749eac1cf61767a15b4")
   or ( "Lab Result Value (ordinal)-fd118a6a64d30b0b20c71b3131431f13")
   and ( "Patient has lab result with test name (specific for an organism or substance)-61bef094563a83a4d5192e2d94e45702")
   or ( "Lab Result Value (nominal)-74d1d577792af48f61c6f9e2b184f41b")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-761d0576280c690a3f110b4ad2e3251a")
   or ( "Lab Result Interpretation-ddaf733862ea33053205e4e1952ddbd9")
   or ( "Lab Result Value (nominal)-3b7b0e657f436cb0581f503e33d8b65c")
   or ( "Lab Result Value (ordinal)-a6be054280f9738d9b2ddc9dbee138db")
   and ( "Patient has lab result with test name (specific for an organism or substance)-fde09bf4a60fa79bfb56c11f65541e0e")
   or ( "Group 2-4f49cd354512a19020000587c69e8d8c")
   or ( "Group 1-7e2848372e0f7fca6f1fedfc8d56aa6f")
   or ( "Lab Result Value (nominal)-c76780819aff12196ef4069bca455eb2")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-31958601187043baf45d43ab4da76302")
   or ( "Lab Result Interpretation-b3c300971a80c71f39dab4ab67e61f4b")
   or ( "Lab Result Value (nominal)-c77be3269c97a0b7ac81cb681b0dbad5")
   or ( "Lab Result Value (ordinal)-c824c76da72f4c97aae7b1b951ce5c43")
   and ( "Patient has lab result with test name (specific for an organism or substance)-c6a50ca88efd86457377838f8f8a0861")
   or ( "Lab Result Interpretation-be064ee58debdae3549bc2c722a12aa6")
   or ( "Lab Result Value (nominal)-56e9de0426adf6a6d899cc7d679e957e")
   or ( "Lab Result Value (ordinal)-8be6c2199a345f2a8070bf93785faa0a")
   and ( "Patient has lab result with test name (specific for an organism or substance)-2c16a2163b921d662cae73ed255a35c0")
   or ( "Group 2-5830f09983d6efa5587d34c499c3f658")
   or ( "Group 1-ecfd4281493b01edcecb6b772c278dc6")
   or ( "Group 1-f7cf704e5037d89bbb7359128b8c745d")
   and ( "Group 1-ddcbe9e6ca9e167cb51bf80aa097aea3")
   or ( "Patient has a diagnosis of-7712dd616df3bd1de0b99ca9eec795e4")
   and ( "Group 1-1bd05d1848cdad2cc3d093c54a88d4b7")
   or ( "Patient has a diagnosis of-73bc5379a6c8afbeb8fb265dbecb9368")
   and ( "Group 1-29f66fbbdde200b0f23ef3ce5cc8c5e8")
   or ( "Patient has a diagnosis of-d2017b8afb227480e66fea2554a8cf06")
   or ( "Lab Result Interpretation-2133a1f9d43f00414d81e93962e96413")
   or ( "Lab Result Value (ordinal)-4557fee6dc7c802c18e4da6b7db99985")
   and ( "Patient has lab result with test name (specific for an organism or substance)-e925fd5553cc80755573aa9d1edb471a")
   or ( "Lab Result Interpretation-36634deeac7086925407e82764880d16")
   or ( "Lab Result Value (ordinal)-59af640165484a4366dd80534a722397")
   and ( "Patient has lab result with test name (specific for an organism or substance)-3186363ba02360d86e149fd8882ba6f1")
   or ( "Lab Result Interpretation-35103f6e5dc04b639a2d97d862bf5eca")
   or ( "Lab Result Value (ordinal)-a679e18398d78d174a3acfa6e29765e4")
   and ( "Patient has lab result with test name (specific for an organism or substance)-e75d770da0376e8594fea8fe7b8e1df6")
   or ( "Lab Result Interpretation-82fec239e022522bf9dd18d560f413d3")
   or ( "Lab Result Value (ordinal)-c392c969dfbc62c5db5e2162a799a754")
   and ( "Patient has lab result with test name (specific for an organism or substance)-8a71972d4db2a23275a1b7d3f2d24f11")
   or ( "Lab Result Interpretation-3a03af04ef1d8749826d96ee0d67ab2c")
   or ( "Lab Result Value (ordinal)-9c09b43e7bbac398f5122a0fa00d6cf8")
   and ( "Patient has lab result with test name (specific for an organism or substance)-449d57739d7053ff4bab75913b00edaa")
   or ( "Lab Result Interpretation-2f5185d71a9937fe531157942a913f37")
   or ( "Lab Result Value (ordinal)-b941a484379bd7fd89e6be067cf66f47")
   and ( "Patient has lab result with test name (specific for an organism or substance)-e7199e8c181aa9461f4584ec810468e9")
