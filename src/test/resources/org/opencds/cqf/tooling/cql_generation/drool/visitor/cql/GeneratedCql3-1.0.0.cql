library GeneratedCql3 version '1.0.0'

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ObservationStatus" : 'http://hl7.org/fhir/observation-status'

valueset "Influenza (Tests for influenza A or B virus Antigen)" : 'https://hln.com/fhir/ValueSet/FLU008'
valueset "Present or Positive Lab Result Value" : 'https://hln.com/fhir/ValueSet/LabRLT001'
valueset "Influenza (influenza A or B virus in Lab Results)" : 'https://hln.com/fhir/ValueSet/FLU001'
valueset "Abnormal Interpretation of an Observation" : 'https://hln.com/fhir/ValueSet/LabRLT002'
valueset "Influenza (Tests for Influenza A or B Virus Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/FLU007'
valueset "Organisms (Tests for Nucleic Acid)" : 'https://hln.com/fhir/ValueSet/LabTST018'
valueset "Influenza (Tests for influenza A or B virus by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/FLU006'
valueset "Viruses (Tests by Culture and Identification Method)" : 'https://hln.com/fhir/ValueSet/LabTST002'

code "Final" : 'final' from "ObservationStatus" display 'Final'
code "Amended" : 'amended' from "ObservationStatus" display 'Amended'

context Patient

define "Patient has lab result with test name (specific for an organism or substance)-64fb91d36ac28d60bb13e2a78d2f3ab8":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Influenza (Tests for influenza A or B virus Antigen)"
)

define "Lab Result Value (ordinal)-8277538a254e40f7619bfbbfd84090b3":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-f38704a1ac0584ae8b176c104cdcea16":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Influenza (influenza A or B virus in Lab Results)"
)

define "Lab Result Interpretation-94ad9e39a755afab3b76557bc117f20d":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (specific for an organism or substance)-4156c3bb697237098e9e310961214ac1":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Influenza (Tests for Influenza A or B Virus Nucleic Acid)"
)

define "Lab Result Value (ordinal)-c64c4c6550a5ff6b3ff187845f44b722":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-dd6853fc2f3a76e177637c2cc57749fb":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Influenza (influenza A or B virus in Lab Results)"
)

define "Lab Result Interpretation-54ca95f34c7629a261929ee4da963516":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-5b4d63c1978e9f82895ff82890e48874":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Organisms (Tests for Nucleic Acid)"
)

define "Lab Result Value (nominal)-c461f16c3fdc1c9e78479338dcc27332":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Influenza (influenza A or B virus in Lab Results)"
)

define "Patient has lab result with test name (specific for an organism or substance)-71073e7afb4e5b827771ddbb9c5fb5f1":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Influenza (Tests for influenza A or B virus by Culture and Identification Method)"
)

define "Lab Result Value (ordinal)-572448849e3961ea93a5140cc767e447":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Present or Positive Lab Result Value"
)

define "Lab Result Value (nominal)-df5346597eaa8f57d655b304b737bfb3":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Influenza (influenza A or B virus in Lab Results)"
)

define "Lab Result Interpretation-9e455aa2c3f584563fdb19b94f269e03":
  exists (
    (flatten (
      [Observation] O
        return O.interpretation
    )
  ) Alias
    where Alias in "Abnormal Interpretation of an Observation"
)

define "Patient has lab result with test name (NOT specific for an organism or substance)-62877505d94007322896910a226a12c2":
  exists (
    ([Observation] O
      return O.code
  ) Alias
    where Alias in "Viruses (Tests by Culture and Identification Method)"
)

define "Lab Result Value (nominal)-346fd0908cb468d25f55af55c80c4882":
  exists (
    ([Observation] O
      where O.status in { "Final", "Amended" }.code
      return O.value as FHIR.CodeableConcept
  ) Alias
    where Alias in "Influenza (influenza A or B virus in Lab Results)"
)

define "ConditionCriteriaMet":
   "Lab Result Value (nominal)-346fd0908cb468d25f55af55c80c4882"
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-62877505d94007322896910a226a12c2")
   or ( "Lab Result Interpretation-9e455aa2c3f584563fdb19b94f269e03")
   or ( "Lab Result Value (nominal)-df5346597eaa8f57d655b304b737bfb3")
   or ( "Lab Result Value (ordinal)-572448849e3961ea93a5140cc767e447")
   and ( "Patient has lab result with test name (specific for an organism or substance)-71073e7afb4e5b827771ddbb9c5fb5f1")
   or ( "Lab Result Value (nominal)-c461f16c3fdc1c9e78479338dcc27332")
   and ( "Patient has lab result with test name (NOT specific for an organism or substance)-5b4d63c1978e9f82895ff82890e48874")
   or ( "Lab Result Interpretation-54ca95f34c7629a261929ee4da963516")
   or ( "Lab Result Value (nominal)-dd6853fc2f3a76e177637c2cc57749fb")
   or ( "Lab Result Value (ordinal)-c64c4c6550a5ff6b3ff187845f44b722")
   and ( "Patient has lab result with test name (specific for an organism or substance)-4156c3bb697237098e9e310961214ac1")
   or ( "Lab Result Interpretation-94ad9e39a755afab3b76557bc117f20d")
   or ( "Lab Result Value (nominal)-f38704a1ac0584ae8b176c104cdcea16")
   or ( "Lab Result Value (ordinal)-8277538a254e40f7619bfbbfd84090b3")
   and ( "Patient has lab result with test name (specific for an organism or substance)-64fb91d36ac28d60bb13e2a78d2f3ab8")
