library EXM161_FHIR3 version '8.0.0'
/*CMS61 Adult MDD*/
//All measures need a library//
//Each measure needs to have a library specified//

using FHIR version '3.0.0'
//include library MATGlobalCommonFunctions_FHIR version '4.0.000' called Global//
//includes do not use the term 'library'//
include MATGlobalCommonFunctions_FHIR3 version '4.0.000' called Global
include FHIRHelpers version '3.0.0' called FHIRHelpers

codesystem SNOMEDCT: 'https://snomed.info/sct'

valueset "Emergency Department Visit": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1010'
valueset "Ethnicity": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.837'
valueset "Major Depressive Disorder-Active": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.526.3.1491'
valueset "Office Visit": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1001'
valueset "ONC Administrative Sex": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1'
valueset "Outpatient Consultation": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1008'
valueset "Payer": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.3591'
valueset "Psych Visit - Diagnostic Evaluation": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.526.3.1492'
valueset "Psych Visit - Psychotherapy": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.526.3.1496'
valueset "Psychoanalysis": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.526.3.1141'
valueset "Race": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.836'
valueset "Telehealth Services": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1031'

code "Suicide risk assessment (procedure)": '225337009' from "SNOMEDCT" display 'Suicide risk assessment (procedure)'

parameter "Measurement Period" Interval<DateTime> default Interval[@2019-01-01T00:00:00.0, @2020-01-01T00:00:00.0)

context Patient

define "Denominator":
"Initial Population"

define "Initial Population":
"New or Recurrent Major Depressive Disorder Encounter" NewOrRecurrentMDDEncounter
	where Global."CalendarAgeInYearsAt"(FHIRHelpers.ToDate(Patient.birthDate), start of "Measurement Period")>= 17

define "Major Depressive Disorder Encounter":
( ["Encounter": "Psych Visit - Diagnostic Evaluation"]
	union ["Encounter": "Emergency Department Visit"]
	union ["Encounter": "Office Visit"]
	union ["Encounter": "Outpatient Consultation"]
	union ["Encounter": "Psych Visit - Psychotherapy"]
	union ["Encounter": "Psychoanalysis"]
	union ["Encounter": "Telehealth Services"] ) ValidEncounter
	where ValidEncounter.status = 'finished'
    and Global.EncounterDiagnosis(ValidEncounter).code in "Major Depressive Disorder-Active"

define "New or Recurrent Major Depressive Disorder Encounter":
"Major Depressive Disorder Encounter" NewOrRecurrentMDDEncounter
	without "Major Depressive Disorder Encounter" PriorMDDEpisodeEncounter
		such that PriorMDDEpisodeEncounter !~ NewOrRecurrentMDDEncounter
			and PriorMDDEpisodeEncounter.period ends 104 days or less before day of start of NewOrRecurrentMDDEncounter.period
	where NewOrRecurrentMDDEncounter.period during "Measurement Period"

define "Numerator":
  "New or Recurrent Major Depressive Disorder Encounter" NewOrRecurrentMDDEncounter
  	with ["Procedure": "Suicide risk assessment (procedure)"] SuicideRiskAssessment
	  	such that SuicideRiskAssessment.performed during NewOrRecurrentMDDEncounter.period
        and SuicideRiskAssessment.status = 'completed'
