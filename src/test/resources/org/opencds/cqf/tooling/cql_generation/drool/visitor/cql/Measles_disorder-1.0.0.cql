library Measles_disorder version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'
codesystem "Condition Category Codes" : 'http://terminology.hl7.org/CodeSystem/condition-category'
codesystem "Condition Clinical Status Codes" : 'http://terminology.hl7.org/CodeSystem/condition-clinical'

valueset "Measles (Tests for measles virus Antigen)" : 'https://hln.com/fhir/ValueSet/MEA006'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Measles (Organism or Substance in Lab Results)" : 'https://hln.com/fhir/ValueSet/MEA001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Measles (Tests for measles virus IgM Antibody)" : 'https://hln.com/fhir/ValueSet/MEA007'
valueset "Measles (Tests for measles virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/MEA005'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Measles (Tests for measles virus by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/MEA004'
valueset "Viruses (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST002'
valueset "Measles (Koplik Spots) (Disorders)" : 'https://hln.com/fhir/ValueSet/MEA003'
valueset "Measles (Test Panels for measles virus IgM Antibody)" : 'https://hln.com/fhir/ValueSet/MEA012'
valueset "Measles (Test Panels for Measles virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/MEA011'
valueset "Measles (Disorders)" : 'https://hln.com/fhir/ValueSet/MEA002'
valueset "Rash" : 'https://hln.com/fhir/ValueSet/SYMP024'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'
code "Encounter Diagnosis" : 'encounter-diagnosis' from "Condition Category Codes" display 'Encounter Diagnosis'
code "Problem" : 'problem-list-item' from "Condition Category Codes" display 'Problem List Item'
code "Active" : 'active' from "Condition Clinical Status Codes" display 'Active'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-71654da21305e80df607b6c143e834e3":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Measles (Tests for measles virus Antigen)"
)

define "Lab Result Value (ordinal)-e1ff62c9a3da3c4578a81dfe7daf8007":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-577a206ae16166ced0fcea04bfbcfe4c":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Measles (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-bc663896250f9ec8193cde13788de451":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-941d7ef8b9faffe28bb51b9a2eb326cc":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Measles (Tests for measles virus IgM Antibody)"
)

define "Lab Result Value (ordinal)-3f312445c0790a69f3b9b6b21be40715":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-4ee2c91512bc905ae3f9afa630806c63":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Measles (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-1782a7cd49d4bb518421c3cabd7bca94":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-1e7e88b444cf9e0dd911112fc1494807":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Measles (Tests for measles virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-3e5b7c64e1c006d27a7aba9c4826844f":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-9c6f6c17950cd788c47a1979ec2f8725":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Measles (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-cdc3aa971c2ae74216c179d0c8816b6a":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-d16e386ac8392a0f19c982171b1cccc3":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-7b6d0fb4f2a924951cc391f7a0b14428":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Measles (Organism or Substance in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-7e837167f171372d8be572a3168aee53":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Measles (Tests for measles virus by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-2052708b73d3481dbbb82d682332825b":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-e1c86c97be03ed09b44a4380b7804bc0":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Measles (Organism or Substance in Lab Results)"
)

define "Lab Result Interpretation-ab704367334b80efb171a8573c130cb8":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-607793deddd6083cbbe6315710ace6c6":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viruses (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-8a236892c0487f5298f0b4e3f1e7cb1a":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Measles (Organism or Substance in Lab Results)"
)

define "Patient has a diagnosis of-f9f6402b869b4e76bf9dacf793f54448":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Measles (Koplik Spots) (Disorders)"
)

define "Patient has a problem of-869b296bacfb123f4461e6f1b31d4822":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Measles (Koplik Spots) (Disorders)"
)

define "Problem has status of-881fcd77b64e35760dcbff7a051cb29f":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-31db75ed6e3e63d48ed5e82ec5cd336d":
   "Problem has status of-881fcd77b64e35760dcbff7a051cb29f"
   and ( "Patient has a problem of-869b296bacfb123f4461e6f1b31d4822")

define "Patient has a lab test order with test name-d97c13f6bd8056e860884389adff666d":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Measles (Tests for measles virus Antigen)"
)

define "Patient has a lab test order with test name-7640a38e5df98a404f64d143496be4f0":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Measles (Tests for measles virus IgM Antibody)"
)

define "Patient has a lab test order with test name-2d0ad79ed883a1b41e42991aa2c3992c":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Measles (Test Panels for measles virus IgM Antibody)"
)

define "Patient has a lab test order with test name-3ffa17f491aa2103c86feadaa3797c79":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Measles (Tests for measles virus Nucleic Acid)"
)

define "Patient has a lab test order with test name-a349f42622302148d41c45829b2c3ff3":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Measles (Test Panels for Measles virus Nucleic Acid)"
)

define "Patient has a lab test order with test name-7907aee4e9bd431f98335f03121db23e":
  exists (
    ([ServiceRequest] SR
      return SR.code
  ) Alias
    where Alias in "Measles (Tests for measles virus by Culture and Identification Method)"
)

define "Patient has a diagnosis of-35b12fd444be908151e5b627e683c0a0":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Measles (Disorders)"
)

define "Patient has a problem of-27d31f35ffbd82ada520444add52dfd5":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Measles (Disorders)"
)

define "Problem has status of-d322a74468dc9338af6b68836c39ed61":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-61b69c926c44e5b0146af5d577677619":
   "Problem has status of-d322a74468dc9338af6b68836c39ed61"
   and ( "Patient has a problem of-27d31f35ffbd82ada520444add52dfd5")

define "Patient has a diagnosis of-2ff9d22b6bb8a42410f04a693e934a3e":
  exists (
    ([Condition: category ~ { "Encounter Diagnosis" }] C
      return C.code
  ) Alias
    where Alias in "Rash"
)

define "Patient has a problem of-6d0b5d391cf2df0db545b51e5dc21fc7":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.code
  ) Alias
    where Alias in "Rash"
)

define "Problem has status of-8bd861cad11db7841d7c005d6a1b88fe":
  exists (
    ([Condition: category ~ { "Problem" }] C
      return C.clinicalStatus
  ) Alias
    where Alias ~ ToConcept("Active")
)

define "Group 1-7791b59e136200d971ab7af22dde2e01":
   "Problem has status of-8bd861cad11db7841d7c005d6a1b88fe"
   and ( "Patient has a problem of-6d0b5d391cf2df0db545b51e5dc21fc7")

define "ConditionCriteriaMet":
   "Group 1-7791b59e136200d971ab7af22dde2e01"
   or ( "Patient has a diagnosis of-2ff9d22b6bb8a42410f04a693e934a3e")
   and ( "Group 1-61b69c926c44e5b0146af5d577677619")
   or ( "Patient has a diagnosis of-35b12fd444be908151e5b627e683c0a0")
   or ( "Patient has a lab test order with test name-7907aee4e9bd431f98335f03121db23e")
   or ( "Patient has a lab test order with test name-a349f42622302148d41c45829b2c3ff3")
   or ( "Patient has a lab test order with test name-3ffa17f491aa2103c86feadaa3797c79")
   or ( "Patient has a lab test order with test name-2d0ad79ed883a1b41e42991aa2c3992c")
   or ( "Patient has a lab test order with test name-7640a38e5df98a404f64d143496be4f0")
   or ( "Patient has a lab test order with test name-d97c13f6bd8056e860884389adff666d")
   and ( "Group 1-31db75ed6e3e63d48ed5e82ec5cd336d")
   or ( "Patient has a diagnosis of-f9f6402b869b4e76bf9dacf793f54448")
   or ( "Lab Result Value (nominal)-8a236892c0487f5298f0b4e3f1e7cb1a")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-607793deddd6083cbbe6315710ace6c6")
   or ( "Lab Result Interpretation-ab704367334b80efb171a8573c130cb8")
   or ( "Lab Result Value (nominal)-e1c86c97be03ed09b44a4380b7804bc0")
   or ( "Lab Result Value (ordinal)-2052708b73d3481dbbb82d682332825b")
   and ( "Patient has lab result with test name (specific for an organism or substance)-7e837167f171372d8be572a3168aee53")
   or ( "Lab Result Value (nominal)-7b6d0fb4f2a924951cc391f7a0b14428")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-d16e386ac8392a0f19c982171b1cccc3")
   or ( "Lab Result Interpretation-cdc3aa971c2ae74216c179d0c8816b6a")
   or ( "Lab Result Value (nominal)-9c6f6c17950cd788c47a1979ec2f8725")
   or ( "Lab Result Value (ordinal)-3e5b7c64e1c006d27a7aba9c4826844f")
   and ( "Patient has lab result with test name (specific for an organism or substance)-1e7e88b444cf9e0dd911112fc1494807")
   or ( "Lab Result Interpretation-1782a7cd49d4bb518421c3cabd7bca94")
   or ( "Lab Result Value (nominal)-4ee2c91512bc905ae3f9afa630806c63")
   or ( "Lab Result Value (ordinal)-3f312445c0790a69f3b9b6b21be40715")
   and ( "Patient has lab result with test name (specific for an organism or substance)-941d7ef8b9faffe28bb51b9a2eb326cc")
   or ( "Lab Result Interpretation-bc663896250f9ec8193cde13788de451")
   or ( "Lab Result Value (nominal)-577a206ae16166ced0fcea04bfbcfe4c")
   or ( "Lab Result Value (ordinal)-e1ff62c9a3da3c4578a81dfe7daf8007")
   and ( "Patient has lab result with test name (specific for an organism or substance)-71654da21305e80df607b6c143e834e3")
