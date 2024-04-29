Instance: MBODAQuestionnaire
InstanceOf: Questionnaire
Usage: #example
* extension[+]
  * url = "http://hl7.org/fhir/StructureDefinition/cqf-library"
  * valueCanonical = "http://fhir.org/guides/cqf/us/common/Library/MBODAInitialExpressions"
* url = "http://fhir.org/guides/cqf/us/common/Questionnaire/MBODAQuestionnaire"
* version = "0.1.0"
* name = "MBODAQuestionnaire"
* title = "Medical Benefit Outpatient Drug Authorization Form"
* status = #active
* experimental = true
* date = "2023-08-14T00:00:00+00:00"
* publisher = "Smile Digital Health"
* description = "Drugs administered by healthcare professionals in an outpatient setting are covered under the Medical Benefit. Information on drugs requiring prior authorization can be found on NaviNet.net or the For Providers section of the Geisinger Health Plan website."
* item[+]
  * insert QuestionnaireItem(#group, "patient-info", "Patient Information")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Patient Name")
    * insert QuestionnaireItem(#string, "patient-info|name", "Patient name")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Date of Birth")
    * insert QuestionnaireItem(#date, "patient-info|dob", "DOB")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Birth Sex")
    * insert QuestionnaireItem(#choice, "patient-info|gender", "Sex")
    * answerValueSet = "http://hl7.org/fhir/us/core/ValueSet/birthsex"
  * item[+]
    * insert QuestionnaireItemInitialExpression("Member ID")
    * insert QuestionnaireItem(#string, "patient-info|member-id", "Member ID #")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Medical Record Number")
    * insert QuestionnaireItem(#string, "patient-info|medical-record-number", "Medical record #")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Phone Number")
    * insert QuestionnaireItem(#string, "patient-info|phone-number", "Member phone #")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Address")
    * insert QuestionnaireItem(#string, "patient-info|address", "Address")
  * item[+]
    * insert QuestionnaireItemInitialExpression("City")
    * insert QuestionnaireItem(#string, "patient-info|city", "City")
  * item[+]
    * insert QuestionnaireItemInitialExpression("State")
    * insert QuestionnaireItem(#string, "patient-info|state", "State")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Zip")
    * insert QuestionnaireItem(#string, "patient-info|zip-code", "Zip")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Allergies")
    * insert QuestionnaireItemRepeats(#open-choice, "patient-info|drug-allergies", "Drug allergies")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Height in [in_i]")
    * insert QuestionnaireItem(#decimal, "patient-info|height", "Height")
    * insert QuestionnaireItemUnit(#[in_i], "in")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Weight in [lb_av]")
    * insert QuestionnaireItem(#decimal, "patient-info|weight", "Weight")
    * insert QuestionnaireItemUnit(#[lb_av], "lbs")
  * item[+]
    * insert QuestionnaireItemInitialExpression("BSA in m2")
    * insert QuestionnaireItem(#decimal, "patient-info|bsa", "BSA")
    * insert QuestionnaireItemUnit(#[m2], "m2")
* item[+]
  * insert QuestionnaireItem(#group, "provider-info", "Ordering Provider Information")
  * item[+]
    * insert QuestionnaireItem(#string, "provider-info|name", "Ordering provider name")
  * item[+]
    * insert QuestionnaireItem(#string, "provider-info|npi", "Ordering provider NPI #")
  * item[+]
    * insert QuestionnaireItem(#string, "provider-info|address", "Ordering provider address")
  * item[+]
    * insert QuestionnaireItem(#string, "provider-info|city", "City")
  * item[+]
    * insert QuestionnaireItem(#string, "provider-info|state", "State")
  * item[+]
    * insert QuestionnaireItem(#string, "provider-info|zip-code", "Zip")
  * item[+]
    * insert QuestionnaireItem(#group, "person-submitting-request", "Person submitting request")
    * item[+]
      * insert QuestionnaireItem(#string, "person-submitting-request|name", "Name")
    * item[+]
      * insert QuestionnaireItem(#string, "person-submitting-request|phone-number", "Phone")
    * item[+]
      * insert QuestionnaireItem(#string, "person-submitting-request|fax", "Fax")
  * item[+]
    * insert QuestionnaireItem(#group, "office-contact", "Office contact")
    * item[+]
      * insert QuestionnaireItem(#string, "office-contact|name", "Name")
    * item[+]
      * insert QuestionnaireItem(#string, "office-contact|phone-number", "Phone")
    * item[+]
      * insert QuestionnaireItem(#string, "office-contact|fax", "Fax")    
* item[+]
  * insert QuestionnaireItem(#group, "servicing-provider-facility-info", "Servicing Provider/Facility Information")
  * item[+]
    * insert QuestionnaireItem(#choice, "servicing-provider-facility-info|drug-admin", "Who is administering the drug?")
    * answerOption[+]
      * valueCoding.code = #OrderingProvider
      * valueCoding.display = "Ordering Provider"
    * answerOption[+]
      * valueCoding.code = #ServicingProvider
      * valueCoding.display = "Servicing Provider"
    * answerOption[+]
      * valueCoding.code = #HomeHealthAgency
      * valueCoding.display = "Home Health Agency - if yes, name of agency"
  * item[+]
    * insert QuestionnaireItem(#choice, "select-one", "Please select if the medication will be dispensed or administered")
    * answerOption[+]
      * valueCoding.code = #AdministeredFromProviderStock
      * valueCoding.display = "Medication will be administered from provider stock and billed by provider (buy & bill)"
    * answerOption[+]
      * valueCoding.code = #DispensedByPharmacy
      * valueCoding.display = "Medication will be dispensed by a specialty pharmacy and billed by the pharmacy"
    * item[+]
      * insert QuestionnaireItem(#choice, "if-buy-bill", "If buy & bill\, who will be billing for the drug?")
      * insert QuestionnaireEnableWhenEqualsCoding("select-one", #AdministeredFromProviderStock)
      * answerOption[+]
        * valueCoding.code = #OrderingProvider
        * valueCoding.display = "Ordering Provider"
      * answerOption[+]
        * valueCoding.code = #ServicingProvider
        * valueCoding.display = "Servicing Provider"
      * answerOption[+]
        * valueCoding.code = #Facility
        * valueCoding.display = "Facility"
    * item[+]
      * insert QuestionnaireItem(#choice, "provider-participating-ghp", "Is the billing provider participating with GHP?")
      * insert QuestionnaireEnableWhenEqualsCoding("select-one", #AdministeredFromProviderStock)
      * answerOption[+]
        * valueCoding.code = #Y
        * valueCoding.display = "Yes"
      * answerOption[+]
        * valueCoding.code = #N
        * valueCoding.display = "No"
      * item[+]
        * insert QuestionnaireItem(#choice, "no-ghp", "If No\, is this a request for out-of-network services?")
        * insert QuestionnaireEnableWhenEqualsCoding("provider-participating-ghp", #N)
        * answerOption[+]
          * valueCoding.code = #Y
          * valueCoding.display = "Yes"
        * answerOption[+]
          * valueCoding.code = #N
          * valueCoding.display = "No"
* item[+]
  * insert QuestionnaireItem(#group, "servicing-provider", "Servicing provider")
  * item[+]
    * insert QuestionnaireItem(#string, "servicing-provider|name", "Provider name")
  * item[+]
    * insert QuestionnaireItem(#string, "servicing-provider|npi", "NPI #")
  * item[+]
    * insert QuestionnaireItem(#string, "servicing-provider|address", "Address")
  * item[+]
    * insert QuestionnaireItem(#string, "servicing-provider|phone-number", "Phone")
  * item[+]
    * insert QuestionnaireItem(#string, "servicing-provider|fax", "Fax")
  * item[+]
    * insert QuestionnaireItem(#string, "servicing-provider|office-contact", "Office contact")
* item[+]
  * insert QuestionnaireItem(#group, "facility-of-service", "Facility/location of service")
  * item[+]
    * insert QuestionnaireItem(#string, "facility-of-service|name", "Facility/location name")
  * item[+]
    * insert QuestionnaireItem(#string, "facility-of-service|npi", "NPI #")
  * item[+]
    * insert QuestionnaireItem(#string, "facility-of-service|address", "Address")
  * item[+]
    * insert QuestionnaireItem(#string, "facility-of-service|phone-number", "Phone")
  * item[+]
    * insert QuestionnaireItem(#string, "facility-of-service|fax", "Fax")
  * item[+]
    * insert QuestionnaireItem(#string, "facility-of-service|office-contact", "Facility contact")
* item[+]
  * insert QuestionnaireItem(#group, "specialty-vendor", "Specialty vendor (if applicable\)")
  * item[+]
    * insert QuestionnaireItem(#string, "specialty-vendor|name", "Specialty pharmacy name")
  * item[+]
    * insert QuestionnaireItem(#string, "specialty-vendor|npi", "NPI #")
  * item[+]
    * insert QuestionnaireItem(#string, "specialty-vendor|address", "Address")
  * item[+]
    * insert QuestionnaireItem(#string, "specialty-vendor|phone-number", "Phone")
  * item[+]
    * insert QuestionnaireItem(#string, "specialty-vendor|fax", "Fax")
  * item[+]
    * insert QuestionnaireItem(#string, "specialty-vendor|office-contact", "Pharmacy contact")
* item[+]
  * insert QuestionnaireItem(#group, "diagnosis-info", "Diagnosis Information")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Diagnosis Codes")
    * insert QuestionnaireItemRepeats(#choice, "diagnosis-info|diagnosis-code", "Diagnosis/ICD-10 code(s\)")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Diagnosis Descriptions")
    * insert QuestionnaireItemRepeats(#string, "diagnosis-info|diagnosis-description", "Diagnosis description")
* item[+]
  * insert QuestionnaireItem(#group, "medication-info", "Medication Information")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Medication Name")
    * insert QuestionnaireItem(#string, "medication-info|name", "Medication name")
  * item[+]
    * insert QuestionnaireUnitOption(#ug, "mcg")
    * insert QuestionnaireUnitOption(#mg, "mg")
    * insert QuestionnaireUnitOption(#g, "g")
    * insert QuestionnaireUnitOption(#ml, "ml")
    * insert QuestionnaireItemInitialExpression("Medication Dose")
    * insert QuestionnaireItem(#quantity, "medication-info|dose", "Dose")   
  * item[+]
    * insert QuestionnaireItemInitialExpression("Medication Route")
    * insert QuestionnaireItemRepeats(#open-choice, "medication-info|route", "Route")    
  * item[+]
    * insert QuestionnaireUnitOption(#hour, "Per Hour")
    * insert QuestionnaireUnitOption(#day, "Per Day")
    * insert QuestionnaireUnitOption(#week, "Per Week")
    * insert QuestionnaireUnitOption(#month, "Per Month")
    * insert QuestionnaireItemInitialExpression("Medication Frequency")
    * insert QuestionnaireItem(#quantity, "medication-info|frequency", "Frequency")    
  * item[+]
    * insert QuestionnaireUnitOption(#hour, "Hour(s\)")
    * insert QuestionnaireUnitOption(#day, "Day(s\)")
    * insert QuestionnaireUnitOption(#week, "Week(s\)")
    * insert QuestionnaireUnitOption(#month, "Month(s\)") 
    * insert QuestionnaireUnitOption(#year, "Year(s\)")
    * insert QuestionnaireItemInitialExpression("Expected Therapy Length")
    * insert QuestionnaireItem(#quantity, "medication-info|therapy-leng", "Expected length of therapy")    
  * item[+]
    * insert QuestionnaireItem(#integer, "medication-info|num-visits", "Quantity/number of requested visits")    
  * item[+]
    * insert QuestionnaireItemInitialExpression("Anticipated/actual date of service")
    * insert QuestionnaireItem(#date, "medication-info|service-date", "Anticipated/actual date of service")  
  * item[+]
    * insert QuestionnaireItem(#choice, "medication-info|new-med", "New Medication or Continued Therapy")
    * answerOption[+]
      * valueCoding.code = #NewMedication
      * valueCoding.display = "New Medication"
    * answerOption[+]
      * valueCoding.code = #ContinuedTherapy
      * valueCoding.display = "Continuation of therapy"
  * item[+]
    * insert QuestionnaireItemInitialExpression("Initial date of therapy")
    * insert QuestionnaireItem(#date, "medication-info|date-initiallized", "Date therapy initially started")
    * insert QuestionnaireEnableWhenEqualsCoding("medication-info|new-med", #ContinuedTherapy) 
  * item[+]
    * insert QuestionnaireItem(#string, "medication-info|requested-drug-code", "HCPCS/CPT code/J code/NDC code of requested drug")    
  * item[+]
    * insert QuestionnaireItem(#string, "medication-info|associated-procedure", "Associated procedure codes requiring prior auth")
* item[+]
  * insert QuestionnaireItem(#group, "request-for-expedited-review", "Request for Expedited Review")
  * item[+]
    * insert QuestionnaireItem(#group, "request-for-expedited-review|para", "When a request needs to be reviewed in an expedited manner because the standard review time frame may SERIOUSLY JEOPARDIZE THE LIFE OR HEALTH OF THE MEMBER OR THE MEMBER’S ABILITY TO REGAIN MAXIMUM FUNCTION\, note this below by checking URGENT in the space provided\, along with the reason the request is urgent. Requests will not be processed as urgent unless a rationale for urgency is provided.")
    * item[+]
      * insert QuestionnaireItem(#string, "request-for-expedited-review|rationale", "URGENT - rationale")
* item[+]
  * insert QuestionnaireItem(#group, "ordering-provider-signature", "Ordering Provider Signature")
  * item[+]
    * insert QuestionnaireItem(#attachment, "ordering-provider-signature|author-signature", "Signature:")
    * insert QuestionnaireItemSignatureRequired(#1.2.840.10065.1.12.1.1, "Author's Signature")
