Instance: GMTPQuestionnaire
InstanceOf: Questionnaire
Usage: #example
* extension[+]
  * url = "http://hl7.org/fhir/StructureDefinition/cqf-library"
  * valueCanonical = "http://fhir.org/guides/cqf/us/common/Library/GMTPInitialExpressions"
* url = "http://fhir.org/guides/cqf/us/common/Questionnaire/GMTPQuestionnaire"
* version = "0.1.0"
* name = "GMTPQuestionnaire"
* title = "Genetic/Molecular testing preauthorization request"
* status = #active
* experimental = true
* date = "2023-10-12T00:00:00+00:00"
* publisher = "Smile Digital Health"
* description = "Automated online preauthorization is available for some tests. Please log into the Availity Portal at Availity.com for a list of available questionnaires. If the appropriate questionnaire is not available, please complete this form."

* item[+]
  * insert QuestionnaireItem(#group, "patient-info", "Patient Information")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Patient Name")
    * insert QuestionnaireItem(#string, "patient-info|name", "Patient name")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Date of Birth")
    * insert QuestionnaireItem(#date, "patient-info|dob", "DOB")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Humana ID")
    * insert QuestionnaireItem(#string, "patient-info|humana-id", "Humana ID #")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Phone Number")
    * insert QuestionnaireItem(#string, "patient-info|phone-number", "Member phone #")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Address")
    * insert QuestionnaireItem(#string, "patient-info|address", "Address")
* item[+]
  * insert QuestionnaireItem(#group, "provider-info", "Requesting Provider Information")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Requesting Provider Name")
    * insert QuestionnaireItem(#string, "provider-info|name", "Requesting provider name")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Requesting Provider Phone")
    * insert QuestionnaireItem(#string, "provider-info|phone-number", "Phone")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Requesting Provider Address")
    * insert QuestionnaireItem(#string, "provider-info|address", "Ordering provider address")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Requesting Provider NPI")
    * insert QuestionnaireItem(#string, "provider-info|npi", "Ordering provider Tax ID/NPI #")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Requesting Provider Fax")
    * insert QuestionnaireItem(#string, "provider-info|fax", "Fax")
* item[+]
  * insert QuestionnaireItem(#group, "servicing-provider", "Laboratory performing testing/Servicing Provider")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Servicing Provider Name")
    * insert QuestionnaireItem(#string, "servicing-provider|name", "Requesting provider name")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Servicing Provider Phone")
    * insert QuestionnaireItem(#string, "servicing-provider|phone-number", "Phone")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Servicing Provider Address")
    * insert QuestionnaireItem(#string, "servicing-provider|address", "Ordering provider address")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Servicing Provider NPI")
    * insert QuestionnaireItem(#string, "servicing-provider|npi", "Ordering provider Tax ID/NPI #")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Servicing Provider Fax")
    * insert QuestionnaireItem(#string, "servicing-provider|fax", "Fax")
* item[+]
  * insert QuestionnaireItemInitialExpression("Billing Provider different from Servicing Provider")
  * insert QuestionnaireItem(#choice, "billing-provider-question", "Is the billing provider/referring lab different from servicing provider?")
  * answerOption[+]
    * valueCoding.code = #Y
    * valueCoding.display = "Yes"
  * answerOption[+]
    * valueCoding.code = #N
    * valueCoding.display = "No"
  * item[+]
    * insert QuestionnaireEnableWhenEqualsCoding("billing-provider-question", #Y)
    * insert QuestionnaireItem(#group, "billing-provider-info", "Billing provider/referring lab information")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Billing Provider Name")
      * insert QuestionnaireItem(#string, "billing-provider-info|name", "Requesting provider name")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Billing Provider Phone")
      * insert QuestionnaireItem(#string, "billing-provider-info|phone-number", "Phone")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Billing Provider Address")
      * insert QuestionnaireItem(#string, "billing-provider-info|address", "Ordering provider address")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Billing Provider NPI")
      * insert QuestionnaireItem(#string, "billing-provider-info|npi", "Ordering provider Tax ID/NPI #")
    * item[+]
      * insert QuestionnaireItemInitialExpression("Billing Provider Fax")
      * insert QuestionnaireItem(#string, "billing-provider-info|fax", "Fax") 
* item[+]
  * insert QuestionnaireItem(#group, "test-requested", "Test requested")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Date of Service")
    * insert QuestionnaireItem(#date, "test-requested|service-date", "Date of service")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Test ID")
    * insert QuestionnaireItem(#string, "test-requested|test-id", "Test ID")   
  * item[+]
    * insert QuestionnaireItemInitialExpression("Test name")
    * insert QuestionnaireItem(#string, "test-requested|test-name", "Test name")     
  * item[+]
    * insert QuestionnaireItemInitialExpression("Diagnosis Descriptions")
    * insert QuestionnaireItem(#string, "test-requested|diagnosis-description", "Diagnosis description")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Test ICD Codes")
    * insert QuestionnaireItemRepeats(#open-choice, "test-requested|diagnosis-icd-code", "ICD-10 code(s\)")
    * repeats = true
    * answerValueSet = "http://hl7.org/fhir/sid/icd-10"
  * item[+]
    * insert QuestionnaireItemInitialExpression("Test CPT Codes")
    * insert QuestionnaireItemRepeats(#open-choice, "test-requested|diagnosis-cpt-code", "CPT code(s\)")
    * repeats = true
    * answerValueSet = "http://www.ama-assn.org/go/cpt"
* item[+]
  * insert QuestionnaireItem(#group, "clinical-trial", "Clinical Trial Information")
  * item[+]     
    * insert QuestionnaireItemInitialExpression("Part of clinical trial")
    * insert QuestionnaireItem(#choice, "clinical-trial-question", "Is the requested test part of a clinical trial?")
    * answerOption[+]
      * valueCoding.code = #Y
      * valueCoding.display = "Yes"
    * answerOption[+]
      * valueCoding.code = #N
      * valueCoding.display = "No"
    * item[+]
      * insert QuestionnaireItemInitialExpression("Clinical Trial ID")
      * insert QuestionnaireEnableWhenEqualsCoding("clinical-trial-question", #Y)
      * insert QuestionnaireItem(#string, "clinical-trial|yes-clinial-trial", "If Yes\, provide the registration or ID number for the specific trial in which this test is being studied.")
* item[+]
  * insert QuestionnaireItem(#group, "history", "Patient history and related findings")
  * item[+]
    * insert QuestionnaireItem(#string, "history|patient-history", "Patient history including age at diagnosis")
  * item[+]
    * insert QuestionnaireItem(#string, "history|family-history", "Family history\, including age at diagnosis\, availability for testing e.g.\, family member deceased\, refused testing or patient not in contact with affected family member and any genetic testing performed on family member - attach pedigree\, if available")
  * item[+]
    * insert QuestionnaireItemInitialExpression("Previous genetic testing for condition")
    * insert QuestionnaireItem(#string, "history|other-findings", "Other findings/testing completed - previous genetic testing for condition")
  * item[+]
    * insert QuestionnaireItem(#string, "history|treatment-relation", "How will testing be used in relation to treatment or management of the patient?")
* item[+]
  * insert QuestionnaireItem(#group, "person-filling-out-form", "Person filling out form")
  * item[+]
    * insert QuestionnaireItem(#string, "person-filling-out-form|name", "Name")   
  * item[+]
    * insert QuestionnaireItem(#string, "person-filling-out-form|phone-number", "Phone number")
  * item[+]
    * insert QuestionnaireItem(#string, "person-filling-out-form|facility", "Phone number")  