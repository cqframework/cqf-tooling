library UPPARFInitialExpressions version '0.1.0'

using USCore version '3.1.1'
using FHIR version '4.0.1'

include fhir.cqf.common.FHIRHelpers version '4.0.1'
include USCoreCommon version '0.1.0' called UC
include USCoreElements version '0.1.0' called UCE

codesystem "Identifier Type": 'http://terminology.hl7.org/CodeSystem/v2-0203'
codesystem "Verification Status": 'http://terminology.hl7.org/CodeSystem/condition-ver-status'
codesystem "Condition Clinical": 'http://terminology.hl7.org/CodeSystem/condition-clinical'
codesystem "ICD10": 'http://hl7.org/fhir/sid/icd-10-cm'
code "Member Number": 'MB' from "Identifier Type"
code "Subscriber Number": 'SN' from "Identifier Type"
code "Confirmed": 'confirmed' from "Verification Status"
code "Active": 'active' from "Condition Clinical"


context Patient

define "Urgency":
"Most Recent Medication Request".priority P
  return (
    if P != 'routine' then 'Urgent'
    else  'Not Urgent'
  )

//TODO: name could also be retrieved by MedicationRequest as a codeable concept if there is no reference
define "Most Recent Medication Name":
  "Most Recent Medication" M
    return M.code.display

define "Is Opioid Treatment":
null //TBD

//TODO: can we assume that with a previous prior authentication there also has been a prior prescription?
define "Is First Prior Authentication":
IsNull("Prior Prescription")

//TODO: does the initial request date mean the exact same medication was requested before?
define "Initial Request Date":
if not "Is First Prior Authentication" then
    singleton from (
    UCE."All Medication Requests" M
      where EndsWith(("Most Recent Medication Request".priorPrescription as USCore.Reference).reference, M.id)
      return M.authoredOn
    )
  else 
    null

define "Is First Request > 12 Months":
if IsNull("Initial Request Date") then null else 
(months between "Initial Request Date" and Now() > 12)

define "Patient Name":
  UCE."Name - First Middle(s) Last"

//TODO: is there a need to check for coverage period and order? also, do we need to check that the payor organization is the same as the source of the prior auth form? 
define "Member ID":
  singleton from
    (
      (
        (
          ([FHIR.Coverage] C 
              where EndsWith(C.beneficiary.reference, Patient.id))
          .identifier) I
        return {value: I.value.value, type: I.type.coding.code}
      ) valuesByTypes
    where "Member Number".code in valuesByTypes.type
    return valuesByTypes.value
    )

//TODO: is there a need to check for coverage period and order? also, do we need to check that the payor organization is the same as the source of the prior auth form? 
define "Policy Number":
singleton from
    (
      (
        (
          ([FHIR.Coverage] C 
              where EndsWith(C.beneficiary.reference, Patient.id))
          .identifier) I
        return {value: I.value.value, type: I.type.coding.code}
      ) valuesByTypes
    where "Subscriber Number".code in valuesByTypes.type
    return valuesByTypes.value
    )

define "Date of Birth":
  Patient.birthDate

define "Address":
  { 'TODO: remove hard-coded address' }

define "Phone Number":
  UC.Mobile(Patient.telecom).value

define "Email Address":
  Email(Patient.telecom).value

define "Prescription Date":
"Most Recent Medication Request".authoredOn

define "Prescriber Name":
   First(Requester.name).family
   //TODO: prefix, suffix and given do not work as expected.

define "Prescriber Fax":
  Fax(Requester.telecom).value

define "Prescriber Phone":
  UC.Mobile(Requester.telecom).value

define "Prescriber Pager":
  Pager(Requester.telecom).value

define "Prescriber Address":
'TODO: remove hard coded address'

define "Prescriber Contact":
'TODO: remove hard coded contact'
//TBD: would this be a work phone or more something like an address?

define "Prescriber NPI":
First (
  "Requester".identifier I
  where I.system = 'http://hl7.org.fhir/sid/us-npi'
  ).value

//TODO: what is DEA?
define "Prescriber DEA":
  null

define "Prescriber Tax ID":
First (
  "Requester".identifier I
  where I.system = 'urn:oid:2.16.840.1.113883.4.4'
  ).value

define "Prescriber Specialty":
First(RequesterRole.specialty.codes).display

define "Prescriber Email Address":
Email(Requester.telecom).value

define "Request Type":
//TODO: is this logic correct?
if "Is First Prior Authentication" then
'New Request' else 'Reauthorization'

define "ICD-10 Codes":
 "All Patients Confirmed Active Diagnosis".code.codes C
 where C.system = "ICD10".id
 return C.code

define "Diagnosis Descriptions":
 "All Patients Confirmed Active Diagnosis".code.display

define "Unit Volume of Named Drugs":
"Dosage Value as String" + "Dosage Unit"

define "Dose": 
"Dosage Value as String"

define "Route": 
First("Most Recent Medication Request".dosageInstruction).route.display

define "Frequency": 
 ToString(First("Most Recent Medication Request".dosageInstruction).timing.repeat.frequency) + ' times per ' +
 ToString(First("Most Recent Medication Request".dosageInstruction).timing.repeat.period) + 
 First("Most Recent Medication Request".dosageInstruction).timing.repeat.periodUnit

define "Refills": 
"Most Recent Medication Request".dispenseRequest.numberOfRepeatsAllowed

define "Today":
Today()

define "Dosage Value as String":
  ToString(("Most Recent Medication Request".dosageInstruction.doseAndRate[0].dose as System.Quantity).value)

define "Dosage Unit":
  ("Most Recent Medication Request".dosageInstruction.doseAndRate[0].dose as System.Quantity).unit

//helper definitions
//TODO: medications are not necessarily a reference but could also be represented by a codeeable concept
define "Most Recent Medication":
singleton from (
  UCE."All Medications" M
    where EndsWith((UCE."Most Recent Medication Request".medication as USCore.Reference).reference, M.id))

define "Most Recent Medication Request":
  UCE."Most Recent Medication Request"

define "Prior Prescription":
  "Most Recent Medication Request".priorPrescription

define "All Patients Confirmed Active Diagnosis":
[USCore.Condition] C
  where EndsWith(C.subject.reference, Patient.id)
  and "Confirmed" in C.verificationStatus.codes
  and "Active" in C.clinicalStatus.codes

//TBD: is this better as part of us common?
define function Email(contactPoints List<USCore.ContactPoint>):
  singleton from (contactPoints P where P.system = 'email')

  //TBD: is this better as part of us common?
define function Fax(contactPoints List<USCore.ContactPoint>):
  singleton from (contactPoints P where P.system = 'fax')

    //TBD: is this better as part of us common?
define function Pager(contactPoints List<USCore.ContactPoint>):
  singleton from (contactPoints P where P.system = 'pager')

  define "Requester":
singleton from (
[USCore.PractitionerProfile] P
              where EndsWith("Most Recent Medication Request".requester.reference, P.id))

define "RequesterRole":
singleton from (
[USCore.PractitionerRoleProfile] R
              where EndsWith(R.practitioner.reference, "Requester".id))

