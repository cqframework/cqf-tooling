library EXM125_FHIR4 version '7.2.000'

/*
Based on CMS125v7 - Breast Cancer Screening
*/

/*
This example is a work in progress and should not be considered a final specification
or recommendation for guidance. This example will help guide and direct the process
of finding conventions and usage patterns that meet the needs of the various stakeholders
in the measure development community.
*/

using FHIR version '4.0.0'

include FHIRHelpers version '4.0.0' called FHIRHelpers
include Hospice_FHIR4 version '1.0.000' called Hospice
include AdultOutpatientEncounters_FHIR4 version '1.1.000' called AdultOutpatientEncounters
include MATGlobalCommonFunctions_FHIR4 version '4.0.000' called Global
include SupplementalDataElements_FHIR4 version '1.0.0' called SDE

valueset "ONC Administrative Sex": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1'
valueset "Race": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.836'
valueset "Ethnicity": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.837'
valueset "Payer": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.114222.4.11.3591'
valueset "Bilateral Mastectomy": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1005'
valueset "Female": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.560.100.2'
valueset "Mammography": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.108.12.1018'
valueset "Unilateral Mastectomy": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1020'
valueset "History of bilateral mastectomy": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1068'
valueset "Status Post Left Mastectomy": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1069'
valueset "Status Post Right Mastectomy": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1070'
valueset "Left": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.122.12.1036'
valueset "Right": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.122.12.1035'
valueset "Unilateral Mastectomy, Unspecified Laterality": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1071'

parameter "Measurement Period" Interval<DateTime>
  default Interval[@2019-01-01T00:00:00.0, @2020-01-01T00:00:00.0)

context Patient

define "SDE Ethnicity":
  SDE."SDE Ethnicity"

define "SDE Payer":
  SDE."SDE Payer"

define "SDE Race":
  SDE."SDE Race"

define "SDE Sex":
  SDE."SDE Sex"

define "Denominator":
		"Initial Population"

define "Unilateral Mastectomy Procedure":
		[Procedure: "Unilateral Mastectomy"] UnilateralMastectomyProcedure
				where Global."Normalize Interval"(UnilateralMastectomyProcedure.performed) ends before day of end of "Measurement Period"
						and UnilateralMastectomyProcedure.status = 'completed'

define "Right Mastectomy":
		(
				( [Condition: "Status Post Right Mastectomy"] C
          where C.clinicalStatus ~ ToConcept(Global."active")
        )
				union (
						[Condition: "Unilateral Mastectomy, Unspecified Laterality"] UnilateralMastectomyDiagnosis
								where UnilateralMastectomyDiagnosis.bodySite in "Right"
                  and UnilateralMastectomyDiagnosis.clinicalStatus ~ ToConcept(Global."active")
				)
		) RightMastectomy
				where Global."Normalize Onset"(RightMastectomy.onset) starts before day of end of "Measurement Period"

define "Left Mastectomy":
		(
		    ( [Condition: "Status Post Left Mastectomy"] C
          where C.clinicalStatus ~ ToConcept(Global."active")
        )
				union (
						[Condition: "Unilateral Mastectomy, Unspecified Laterality"] UnilateralMastectomyDiagnosis
								where UnilateralMastectomyDiagnosis.bodySite in "Left"
                  and UnilateralMastectomyDiagnosis.clinicalStatus ~ ToConcept(Global."active")
				)
		) LeftMastectomy
				where Global."Normalize Onset"(LeftMastectomy.onset) starts before day of end of "Measurement Period"

define "History Bilateral Mastectomy":
		[Condition: "History of bilateral mastectomy"] BilateralMastectomyHistory
				where Global."Normalize Onset"(BilateralMastectomyHistory.onset) starts before day of end of "Measurement Period"
          and BilateralMastectomyHistory.clinicalStatus ~ ToConcept(Global."active")

define "Bilateral Mastectomy Procedure":
		[Procedure: "Bilateral Mastectomy"] BilateralMastectomyPerformed
				where Global."Normalize Interval"(BilateralMastectomyPerformed.performed) ends before day of end of "Measurement Period"
						and BilateralMastectomyPerformed.status = 'completed'

define "Numerator":
		exists (
				[DiagnosticReport: "Mammography"] Mammogram
						where ( Global."Normalize Interval"(Mammogram.effective) ends 27 months or less before day of end of "Measurement Period" )
								and Mammogram.status in { 'final', 'amended', 'corrected', 'appended' }
		)

define "Denominator Exclusion":
		Hospice."Has Hospice"("Measurement Period")
				or ( Count("Unilateral Mastectomy Procedure") = 2 )
				or ( exists "Right Mastectomy" and exists "Left Mastectomy" )
				or exists "History Bilateral Mastectomy"
				or exists "Bilateral Mastectomy Procedure"

define "Initial Population":
		Patient.gender = 'female'
				and Global."CalendarAgeInYearsAt"(FHIRHelpers.ToDate(Patient.birthDate), start of "Measurement Period") in Interval[51, 74]
				and exists AdultOutpatientEncounters."Qualifying Encounters"("Measurement Period")
