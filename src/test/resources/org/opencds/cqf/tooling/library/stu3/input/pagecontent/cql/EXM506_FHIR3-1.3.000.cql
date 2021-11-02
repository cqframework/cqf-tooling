library EXM506_FHIR3 version '1.3.000'

/*
Based on CMS506v1 - Safe Use of Opioids - Concurrent Prescribing
*/

using FHIR version '3.0.0'

include MATGlobalCommonFunctions_FHIR3 version '4.0.000' called Global
include FHIRHelpers version '3.0.0' called FHIRHelpers
include SupplementalDataElements_FHIR3 version '1.0.0' called SDE

codesystem "Diagnosis Role": 'http://hl7.org/fhir/diagnosis-role'
codesystem "LOINC": 'http://loinc.org'
codesystem "SNOMEDCT": 'http://snomed.org'
codesystem "RequestIntent": 'http://hl7.org/fhir/request-intent'

valueset "All Primary and Secondary Cancer":  'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1111.161'
valueset "Encounter ED and Observation Stay": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.117.1.7.1.292'
valueset "Encounter Inpatient": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.666.5.307'
valueset "Ethnicity": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.837'
valueset "Hospice Care": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.3157.1004.20'
valueset "ONC Administrative Sex": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1'
valueset "Palliative Care": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1111.162'
valueset "Payer": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.3591'
valueset "Race": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.836'
valueset "Schedule II and Schedule III Opioids":  'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1125.2'
valueset "Schedule IV Benzodiazepines": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1125.1'

code "Birthdate": '21112-8' from "LOINC" display 'Birth date'
code "Order": 'order' from "RequestIntent" display 'Order'

parameter "Measurement Period" Interval<DateTime>

context Patient

define "Inpatient Hospital Based Encounter with Age Greater than or Equal to 18":
	["Encounter": "Encounter Inpatient"] EncounterInpatient //Need to name this//
		where EncounterInpatient.status = 'finished'
		  and Global."LengthInDays"(EncounterInpatient.period) <= 120
			and EncounterInpatient.period ends during "Measurement Period"
      and Global.CalendarAgeInYearsAt(ToDateTime(Patient.birthDate), start of FHIRHelpers.ToInterval(EncounterInpatient.period)) >= 18

define "ED or Observation Based Encounter with Age Greater than or Equal to 18":
["Encounter": "Encounter ED and Observation Stay"] EDorObservationEncounter
	where EDorObservationEncounter.status = 'finished'
   and EDorObservationEncounter.period ends during "Measurement Period"
   and Global.CalendarAgeInYearsAt(ToDateTime(Patient.birthDate), start of FHIRHelpers.ToInterval(EDorObservationEncounter.period)) >= 18

define "Hospital Based Encounters for Adults":
"Inpatient Hospital Based Encounter with Age Greater than or Equal to 18"
	union "ED or Observation Based Encounter with Age Greater than or Equal to 18"

/* define "Encounter Discharges with Opioids or Benzodiazepines":
	"Hospital Based Encounters for Adults" HospitalBasedEncounters
		with [MedicationRequest: medication in "Schedule II and Schedule III Opioids"] OpioidsDischarge
    such that OpioidsDischarge.authoredOn during HospitalBasedEncounters.period
          and OpioidsDischarge.intent = 'order'
					union ("Hospital Based Encounters for Adults" HospitalBasedEncounters
    with [MedicationRequest: medication in "Schedule IV Benzodiazepines"] BenzodiazepineDischarge
    such that BenzodiazepineDischarge.authoredOn during HospitalBasedEncounters.period
           and BenzodiazepineDischarge.intent = 'order') */

define "Encounter Discharges with Opioids or Benzodiazepines":
	from "Hospital Based Encounters for Adults" HospitalBasedEncounters,
		[MedicationRequest: medication in "Schedule II and Schedule III Opioids"] OpioidsDischarge,
		[MedicationRequest: medication in "Schedule IV Benzodiazepines"] BenzodiazepineDischarge
			where OpioidsDischarge.authoredOn during HospitalBasedEncounters.period
				and OpioidsDischarge.intent = 'order'
				and BenzodiazepineDischarge.authoredOn during HospitalBasedEncounters.period
	      and BenzodiazepineDischarge.intent = 'order'
			return HospitalBasedEncounters

define "Initial Population":
"Encounter Discharges with Opioids or Benzodiazepines"

define "Denominator":
"Initial Population"

/* define "Denominator Exclusions":
	"Hospital Based Encounters for Adults" HospitalBasedEncounter
	with [Condition: code in "All Primary and Secondary Cancer"]Cancer
       such that Cancer.clinicalStatus = 'active'
       and Cancer.verificationStatus = 'confirmed'
       and Cancer.onset overlaps HospitalBasedEncounter.period)
     union ((["ProcedureRequest": "Palliative Care"]
			union ["ProcedureRequest": "Hospice Care"] ) PalliativeOrHospiceCareOrder
			where PalliativeOrHospiceCareOrder.intent = 'order'
      and PalliativeOrHospiceCareOrder.authoredOn during HospitalBasedEncounter.period)
    union ("Hospital Based Encounters for Adults" HospitalBasedEncounter
    (["Procedure": "Palliative Care"]
    	union ["Procedure": "Hospice Care"] ) PalliativeOrHospiceCarePerformed
    					where PalliativeOrHospiceCarePerformed.period overlaps HospitalBasedEncounter.period) */

define "Denominator Exclusions":
	from "Hospital Based Encounters for Adults" HospitalBasedEncounter,
		[Condition: code in "All Primary and Secondary Cancer"] Cancer,
		(
			["ProcedureRequest": "Palliative Care"]
				union ["ProcedureRequest": "Hospice Care"]
		) PalliativeOrHospiceCareOrder,
		(
			["Procedure": "Palliative Care"] union ["Procedure": "Hospice Care"]
		) PalliativeOrHospiceCarePerformed
			where Cancer.clinicalStatus = 'active'
				and Cancer.verificationStatus = 'confirmed'
				and Cancer.onset overlaps HospitalBasedEncounter.period
				and PalliativeOrHospiceCareOrder.intent = 'order'
	      and PalliativeOrHospiceCareOrder.authoredOn during HospitalBasedEncounter.period
				and PalliativeOrHospiceCarePerformed.performed overlaps HospitalBasedEncounter.period
			return HospitalBasedEncounter

/* define "Encounter with Opiates and Benzodiazepines at Discharge":
 (("Hospital Based Encounters for Adults" HospitalBasedEncounter
   with ["MedicationRequest"]: OpioidsDischarge
   such that (OpioidsDischarge.medication in "Schedule II and Schedule III Opioids"
   where OpioidsDischarge.authoredOn during HospitalBasedEncounters.period
   and OpioidsDischarge.intent = 'order')
     with ["MedicationRequest"]: BenzodiazepineDischarge
      such that (BenzodiazepineDischarge.medication in "Schedule IV Benzodiazepines"
       where BenzodiazepineDischarge.authoredOn during HospitalBasedEncounters.period
        and BenzodiazepineDischarge.intent = 'order')) */

define "Encounter with Opiates and Benzodiazepines at Discharge":
	from "Hospital Based Encounters for Adults" HospitalBasedEncounter,
		[MedicationRequest: "Schedule II and Schedule III Opioids"] OpioidsDischarge,
		[MedicationRequest: "Schedule IV Benzodiazepines"] BenzodiazepineDischarge
			where OpioidsDischarge.authoredOn during HospitalBasedEncounter.period
	    	and OpioidsDischarge.intent = 'order'
				and BenzodiazepineDischarge.authoredOn during HospitalBasedEncounter.period
        and BenzodiazepineDischarge.intent = 'order'
			return HospitalBasedEncounter

/* define "Has Two or More Opioids at Discharge":
  "Hospital Based Encounters for Adults" HospitalBasedEncounter
    with Count(["MedicationRequest"]:  Opioids
  such that Opioids.medication in "Schedule II and Schedule III Opioids"
    	where Opioids.authorDatetime during HospitalBasedEncounter.relevantPeriod
    	)>= 2 */

define "Has Two or More Opioids at Discharge":
	"Hospital Based Encounters for Adults" HospitalBasedEncounter
		where Count(
			[MedicationRequest: "Schedule II and Schedule III Opioids"] Opioids
				where Opioids.authoredOn during HospitalBasedEncounter.period
		) >= 2

define "Numerator":
	"Has Two or More Opioids at Discharge"
		union "Encounter with Opiates and Benzodiazepines at Discharge"

/* define function "LengthInDays"(Value Interval<DateTime>):
	difference in days between start of Value and end of Value

define function "Global.ToDate"(Value DateTime):
DateTime(year from Value, month from Value, day from Value, 0, 0, 0, 0, timezone from Value)

define function "Global.CalendarAgeInYearsAt"(BirthDateTime DateTime, AsOf DateTime):
      years between ToDate(BirthDateTime)and ToDate(AsOf) */
