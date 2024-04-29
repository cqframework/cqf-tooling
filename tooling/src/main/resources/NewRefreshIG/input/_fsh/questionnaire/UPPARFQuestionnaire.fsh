Instance: UPPARFQuestionnaire
InstanceOf: Questionnaire
Usage: #example
* extension[+]
  * url = "http://hl7.org/fhir/StructureDefinition/cqf-library"
  * valueCanonical = "http://fhir.org/guides/cqf/us/common/Library/UPPARFInitialExpressions"
* url = "http://fhir.org/guides/cqf/us/common/Questionnaire/UPPARFQuestionnaire"
* version = "0.1.0"
* name = "UPPARFQuestionnaire"
* title = "Humana Uniform Pharmacy Prior Authorization Request Form"
* status = #active
* experimental = true
* date = "2023-10-19T00:00:00+00:00"
* publisher = "Smile Digital Health"
* description = "Humana Uniform Pharmacy Prior Authorization Request Form"
* item[+]
  * insert QuestionnaireItemInitialExpression("Urgency")
  * insert QuestionnaireItem(#choice, "urgency", "Urgency")
  * answerOption[+]
    * valueCoding.code = #urgent
    * valueCoding.display = "Urgent"
  * answerOption[+]
    * valueCoding.code = #routine
    * valueCoding.display = "Non-Urgent"
* item[+]
  * insert QuestionnaireItem(#group, "drug-info", "Drug Information")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Most Recent Medication Name")
    * insert QuestionnaireItem(#string, "drug-info|drug-name", "Requested Drug Name")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Is Opioid Treatment")
    * insert QuestionnaireItem(#boolean, "drug-info|drug-intention-opioid", "Is this drug intended to treat opioid dependence?")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Is First Prior Authentication")
      * insert QuestionnaireItem(#boolean, "drug-info|drug-intention-opioid|first-prior-authentication", "Is this a first request for prior authorization for this drug?")
      * insert QuestionnaireEnableWhenBoolean("drug-info|drug-intention-opioid", true)
    * item[+]
      * insert QuestionnaireItemInitialExpression("Initial Request Date")
      * insert QuestionnaireItem(#date, "drug-info|drug-intention-opioid|date-first-request", "What was the date of the first request?")
      * insert QuestionnaireEnableWhenBoolean("drug-info|drug-intention-opioid|first-prior-authentication", false)
    * item[+]
      * insert QuestionnaireItemInitialExpression("Is First Request > 12 Months")
      * insert QuestionnaireItem(#boolean, "drug-info|drug-intention-opioid|twelve-months-since-request", "Has the date of the first request been greater than twelve months ago?")
      * insert QuestionnaireEnableWhenBoolean("drug-info|drug-intention-opioid|first-prior-authentication", false)
* item[+]
  * insert QuestionnaireItem(#group, "completing-form", "Prior authentication is required and this form needs to be completed")   
    * enableBehavior = #any
  * insert QuestionnaireEnableWhenBoolean("drug-info|drug-intention-opioid", false)
  * insert QuestionnaireEnableWhenBoolean("drug-info|drug-intention-opioid|twelve-months-since-request", false)
  * item[+]
    * insert QuestionnaireItem(#group, "completing-form|patient-info", "Patient Info")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Patient Name")
      * insert QuestionnaireItem(#string, "completing-form|patient-info|patient-name", "Patient Name")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Member ID")
      * insert QuestionnaireItem(#string, "completing-form|patient-info|member-number", "Member/Subscriber Number")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Policy Number")
      * insert QuestionnaireItem(#string, "completing-form|patient-info|policy-number", "Policy/Group Number")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Date of Birth")
      * insert QuestionnaireItem(#date, "completing-form|patient-info|dob", "Patient Date of Birth")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Address")
      * insert QuestionnaireItem(#string, "completing-form|patient-info|address", "Patient Address")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Phone Number")
      * insert QuestionnaireItem(#string, "completing-form|patient-info|phone", "Patient Phone")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Email Address")
      * insert QuestionnaireItem(#string, "completing-form|patient-info|email", "Patient Email Address")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescription Date")
      * insert QuestionnaireItem(#string, "completing-form|patient-info|prescription-date", "Prescription Date")
  * item[+]
    * insert QuestionnaireItem(#group, "completing-form|prescriber-info", "Prescriber Info")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber Name")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-name", "Prescriber Name")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber Fax")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-fax", "Prescriber Fax")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber Phone")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-phone", "Prescriber Phone")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber Pager")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-pager", "Prescriber Pager")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber Address")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-address", "Prescriber Address")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber Contact")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-contact", "Prescriber Office Contact")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber NPI")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-npi", "Prescriber NPI")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber DEA")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-dea", "Prescriber DEA")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber Tax ID")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-tax-id", "Prescriber Tax ID")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber Specialty")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-specialty", "Specialty/Facility Name If applicable")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Prescriber Email Address")
      * insert QuestionnaireItem(#string, "completing-form|prescriber-info|prescriber-email", "Email Address")
  * item[+]
    * insert QuestionnaireItem(#group, "completing-form|drug-benefit", "Prior Authorization Request for Drug Benefit")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Request Type")
      * insert QuestionnaireItem(#choice, "completing-form|drug-benefit|request-type", "New Request")
        * answerOption[+]
          * valueString = "New Request"
        * answerOption[+]
          * valueString = "Reauthorization"
    * item[+]
      * insert QuestionnaireItemInitialExpression("ICD-10 Codes")
      * insert QuestionnaireItem(#open-choice, "completing-form|drug-benefit|diagnosis-codes", "Patient ICD Diagnostic Codes")
      * repeats = true
      * answerValueSet = "http://hl7.org/fhir/sid/icd-10"
    * item[+]
      * insert QuestionnaireItemInitialExpression("Diagnosis Descriptions")
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|diagnosis-descriptions", "Patient Diagnosis")
      * repeats = true
    * item[+]
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|drugs", "Drugs Requested with J-Code; if applicable")
      * repeats = true
    * item[+]
      * insert QuestionnaireItemInitialExpression("Unit Volume of Named Drugs")
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|unit-volume", "Unit/Volume of Named Drugs")
    * item[+]
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|start-length", "Start Date and Length of Therapy")
    * item[+]
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|location", "Location of Treatment e.g. provider office; facility; home health; etc. including name; Type 2 NPI if applicable; address and tax ID:")
    * item[+]
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|criteria", "Clinical Criteria for Approval; Including other Pertinent Information to Support the Request; other Medications Tried; Their Names; Duration; and Patient Response:")
    * item[+]
      * insert QuestionnaireItem(#boolean, "completing-form|drug-benefit|for-trial", "For use in clinical trial?")
    * item[+]
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|for-trial|registration-number", "Provide trial name and registration number")
      * insert QuestionnaireEnableWhenBoolean("completing-form|drug-benefit|for-trial", true)
    * item[+]
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|drug-name", "Drug Name Brand Name and Scientific Name/Strength:")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Dose")
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|dose", "Dose")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Route")
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|route", "Route")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Frequency")
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|frequency", "Frequency")
    * item[+]
      * insert QuestionnaireItem(#quantity, "completing-form|drug-benefit|quantity", "Quantity")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Refill")
      * insert QuestionnaireItem(#integer, "completing-form|drug-benefit|refills", "Number of Refills")
    * item[+]
      * insert QuestionnaireItem(#choice, "completing-form|drug-benefit|delivery", "Product will be delivered to:")
        * answerOption[+]
          * valueString = "Patient’s Home"
        * answerOption[+]
          * valueString = "Physician Office"
        * answerOption[+]
          * valueString = "Other"
    * item[+]
      * insert QuestionnaireItem(#attachment, "completing-form|drug-benefit|signature", "Prescriber or Authorized Signature:")
    * item[+]
      * insert QuestionnaireItem(#string, "completing-form|drug-benefit|pharmacy", "Dispensing Pharmacy Name and Phone Number:")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Today")
      * insert QuestionnaireItem(#date, "completing-form|drug-benefit|date", "Date")

