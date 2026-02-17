library LibraryEvaluationTestDependency version '1.0.000'

using FHIR version '4.0.1'

include LibraryEvaluationTestConcepts version '1.0.000' called CommonCx

context Patient

define "Encounters":
  [Encounter]

define "Bone Narrowing Conditions":
  [Condition: category in CommonCx."Problem List Condition Category"]
