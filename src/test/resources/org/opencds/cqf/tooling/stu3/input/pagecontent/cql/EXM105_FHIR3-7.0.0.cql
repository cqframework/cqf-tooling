library EXM105_FHIR3 version '7.0.0'

/*
Based on CMS105v7 - Discharged on Statin Medication
*/

using FHIR version '3.0.0'

include FHIRHelpers version '3.0.0'
include MATGlobalCommonFunctions_FHIR3 version '4.0.000' called Global
include TJCOverall_FHIR3 version '3.6.000' called TJC
include SupplementalDataElements_FHIR3 version '1.0.0' called SDE

codesystem "LOINC": 'http://loinc.org'
codesystem "SNOMEDCT": 'http://snomed.info/sct/731000124108'
codesystem "Diagnosis Role": 'http://hl7.org/fhir/diagnosis-role'
codesystem "RequestIntent": 'http://hl7.org/fhir/request-intent'

valueset "Encounter Inpatient": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.666.5.307'
valueset "Non-Elective Inpatient Encounter": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.424'
valueset "Ischemic Stroke": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.247'
valueset "Hemorrhagic Stroke": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.212'
valueset "Comfort Measures": 'http://cts.nlm.nih.gov/fhir/ValueSet/1.3.6.1.4.1.33895.1.3.0.45'
valueset "Emergency Department Visit": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.292'
valueset "Left Against Medical Advice": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.308'
valueset "Discharged to Health Care Facility for Hospice Care": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.207'
valueset "Patient Expired": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.309'
valueset "Discharge To Acute Care Facility": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.87'
valueset "Discharged to Home for Hospice Care": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.209'
valueset "ONC Administrative Sex": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1'
valueset "Race": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.836'
valueset "Ethnicity": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.837'
valueset "Payer": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.3591'
valueset "LDL-c": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.215'
valueset "Medical Reason": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.473'
valueset "Patient Refusal": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.93'
valueset "Statin Allergen": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.423'
valueset "Statin Grouper": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1110.19'

// TODO: Use NLM VSAC Canonical URLs
code "Birthdate": '21112-8' from "LOINC" display 'Birth date'
code "Dead": '419099009' from "SNOMEDCT" display 'Dead'
//code "Medication-dischargeMedication": '8654-6' from "LOINC" display 'Hospital Discharge medications'

code "Billing": 'billing' from "Diagnosis Role" display 'Billing'
code "Order": 'order' from "RequestIntent" display 'Order'

parameter "Measurement Period" Interval<DateTime> default Interval[@2019-01-01T00:00:00.0, @2020-01-01T00:00:00.0)

context Patient

define "Initial Population":
	"Encounter with Principal Diagnosis and Age"

define "Encounter with Principal Diagnosis and Age":
	"All Stroke Encounter" AllStrokeEncounter
		with ["Patient"] BirthDate
			such that AgeInYearsAt(AllStrokeEncounter.period.start) >= 18

define "All Stroke Encounter":
	"Non Elective Inpatient Encounter" NonElectiveEncounter
		/* where "PrincipalDiagnosis"(NonElectiveEncounter).code in "Hemorrhagic Stroke"
		or "PrincipalDiagnosis"(NonElectiveEncounter).code in "Ischemic Stroke" */

define "Non Elective Inpatient Encounter":
	["Encounter": "Non-Elective Inpatient Encounter"] NonElectiveEncounter
		//where "LengthInDays"(NonElectiveEncounter.period)<= 120
		where days between NonElectiveEncounter.period."start" and NonElectiveEncounter.period."end" <= 120
			and NonElectiveEncounter.period ends during "Measurement Period"

/* define function "EncounterDiagnosis"(Encounter Encounter):
  Encounter.diagnosis D
    return singleton from ([Condition: id in "GetId"(D.condition.reference)]) */

/* define function "PrincipalDiagnosis"(Encounter Encounter):
	(singleton from (Encounter.diagnosis D where D.role ~ ToConcept("Billing") and D.rank = 1)) PD
		return singleton from ([Condition: id in "GetId"(PD.condition.reference)]) */

/* define function "LengthInDays"(Value Interval<DateTime>):
	difference in days between start of Value and end of Value

define function "GetId"(uri String):
	Last(Split(uri, '/')) */

define "Denominator":
	TJC."Ischemic Stroke Encounter"

//QA
define "Statin Allergy":
	["AllergyIntolerance": "Statin Allergen"]

define "Numerator":
	"Ischemic Stroke Encounter" IschemicStrokeEncounter
		with "Statin at Discharge" DischargeStatin
			such that DischargeStatin.authoredOn during IschemicStrokeEncounter.period

define "Ischemic Stroke Encounter":
	"Encounter with Principal Diagnosis and Age" EncounterWithAge
		//where PrincipalDiagnosis(EncounterWithAge).code in "Ischemic Stroke"

//QA
define "Statin at Discharge":
	["MedicationRequest"] Statin
		where (Statin.medication as CodeableConcept) in "Statin Grouper"
			and Statin.intent = 'order'

//QA
define "Statin Not Given at Discharge":
	["MedicationRequest"] NoStatin
		where (NoStatin.medication as CodeableConcept) in "Statin Grouper"
			and (singleton from NoStatin.reasonCode in "Medical Reason"
				or singleton from NoStatin.reasonCode in "Patient Refusal")

//Folling function copied from MATGlobalCommonFunctions 2.0 in cqf-measures github repo
/* define function "Normalize Onset"(onset Choice<FHIR.dateTime, FHIR.Age, FHIR.Period, FHIR.Range, FHIR.string>):
	if onset is FHIR.dateTime then Interval[onset.value, onset.value]
	else if onset is FHIR.Period then FHIRHelpers.ToInterval(onset as FHIR.Period)
	else null */

define "Encounter with Max LDL less than 70 mg per dL":
	"Ischemic Stroke Encounter" IschemicStrokeEncounter
		where Max(["Observation": "LDL-c"] Ldl
				where Ldl.issued during Interval[IschemicStrokeEncounter.period.start - 30 days,IschemicStrokeEncounter.period.end]
				return (Ldl.value as Quantity)
		) < 70

define "Denominator Exception":
	("Ischemic Stroke Encounter" IschemicStrokeEncounter
		with "Statin Not Given at Discharge" NoDischargeStatin
			such that NoDischargeStatin.authoredOn during IschemicStrokeEncounter.period
	)
	union
	("Ischemic Stroke Encounter" IschemicStrokeEncounter )
		//with "Statin Allergy" StatinAllergy
		//	such that "Normalize Onset"(StatinAllergy.onset) on or before end of FHIRHelpers.ToInterval(IschemicStrokeEncounter.period))

define "Denominator Exclusion":
	"Ischemic Stroke Encounters with Discharge Status"
	union
	"Comfort Measures during Hospitalization"

define "Ischemic Stroke Encounters with Discharge Status":
	("Ischemic Stroke Encounter" IschemicStrokeEncounter
		where IschemicStrokeEncounter.hospitalization.dischargeDisposition in "Discharge To Acute Care Facility"
			or IschemicStrokeEncounter.hospitalization.dischargeDisposition in "Left Against Medical Advice"
			or IschemicStrokeEncounter.hospitalization.dischargeDisposition in "Patient Expired"
			or IschemicStrokeEncounter.hospitalization.dischargeDisposition in "Discharged to Home for Hospice Care"
			or IschemicStrokeEncounter.hospitalization.dischargeDisposition in "Discharged to Health Care Facility for Hospice Care"
	)

//QA
define "Comfort Measures during Hospitalization":
	"Ischemic Stroke Encounter" IschemicStrokeEncounter
		with "Intervention Comfort Measures" ComfortMeasure
			such that ComfortMeasure.authoredOn during Global."Hospitalization"(IschemicStrokeEncounter)

/*
define "Intervention Comfort Measures":
	(["ProcedureRequest": "Comfort Measures"] P
		where P.intent = 'order')
		union ["Procedure": "Comfort Measures"]
*/

//QA
define "Intervention Comfort Measures":
	(["ProcedureRequest": "Comfort Measures"] P
		where P.intent = 'order')
		union
		(["Procedure": "Comfort Measures"] IntervetionPerformed
		where IntervetionPerformed.status = 'completed')
