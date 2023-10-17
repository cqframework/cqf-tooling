library SimpleLibrary version '0.0.1'
using FHIR version '4.0.1'
include "FHIRHelpers" version '4.0.1' called FHIRHelpers
valueset "Hypertension VS": 'https://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1032.9'

context Patient

define "Patient is >=18 years old":
  AgeInYears() >= 18

define "Hypertension":
  [Condition: "Hypertension VS"]
