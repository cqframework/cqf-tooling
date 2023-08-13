library ANCDT01

using FHIR version '4.0.1'

include FHIRHelpers version '4.0.1'

include ANCConfig called Config
include ANCConcepts called Cx
include ANCDataElements called PatientData

context Patient

/*
"Danger signs" = "No danger signs"
*/
define "Should Proceed with ANC contact ":
  false

/*
"Danger signs" = "Central cyanosis"
*/
define "Should Proceed with ANC contact OR Referral for Central cyanosis":
  false

/*
((((((((((("Danger signs" = "Bleeding vaginally")
  OR ("Danger signs" = "Convulsing"))
  OR ("Danger signs" = "Fever"))
  OR ("Danger signs" = "Severe headache"))
  OR ("Danger signs" = "Visual disturbance"))
  OR ("Danger signs" = "Imminent delivery"))
  OR ("Danger signs" = "Labour"))
  OR ("Danger signs" = "Looks very ill"))
  OR ("Danger signs" = "Severe vomiting"))
  OR ("Danger signs" = "Severe pain"))
  OR ("Danger signs" = "Severe abdominal pain"))
  OR ("Danger signs" = "Unconscious")
*/
define "Should Proceed with ANC contact OR Referral":
  false

