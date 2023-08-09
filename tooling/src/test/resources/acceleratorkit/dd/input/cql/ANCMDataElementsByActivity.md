
#### ANC.A4 Gather client details

Data elements for this activity can be collected using the [ANCA4](Questionnaire-ANCA4.html)

|Id|Label|Description|Type|Profile Path|
|---|---|---|---|---|
|ANC.A.DE4|Contact date|The date and time of the client's contact|dateTime|[Encounter.period.start](StructureDefinition-anc-encounter.html)|
|ANC.B5.DE1|Reason for coming to facility|Records the reason why the woman came to the health-care facility today|CodeableConcept|[Encounter.reasonCode](StructureDefinition-anc-encounter.html)|
|ANC.A.DE12|ANC contact number|The ANC contact or visit number – recommended minimum is 8 contacts|Integer|[Encounter.contactNumber](StructureDefinition-anc-encounter.html)|
|ANC.A.DE1|Unique identification|Unique identifier generated for new clients or a universal ID, if used in the country|string|[Patient.identifier.value](StructureDefinition-anc-patient.html)|
|ANC.A.DE2|First name|Client's first name|string|[Patient.name.given](StructureDefinition-anc-patient.html)|
|ANC.A.DE3|Last name|Client's family name or last name|string|[Patient.name.family](StructureDefinition-anc-patient.html)|
|ANC.A.DE5|Date of birth|The client's date of birth (DOB), if known|dateTime|[Patient.birthDate](StructureDefinition-anc-patient.html)|
|ANC.A.DE7|Address|Client's home address or address that the client is consenting to disclose|string|[Patient.address.line](StructureDefinition-anc-patient.html)|
|ANC.A.DE8|Mobile phone number|Client's mobile phone number|string|[Patient.telecom.value](StructureDefinition-anc-patient.html)|
|ANC.A.DE10|Alternative contact's name|Name of an alternative contact, which could be next of kin (e.g. partner, mother, sibling); the alternative contact would be used in the case of an emergency situation|string|[Patient.contact.name](StructureDefinition-anc-patient.html)|
|ANC.A.DE11|Alternative contact's phone number|Phone number of the alternative contact|string|[Patient.contact.telecom.value](StructureDefinition-anc-patient.html)|
|ANC.A.DE9|Woman wants to receive reminders during pregnancy|Whether or not the woman wants to receive SMS or other messages regarding her ANC contacts and health status during pregnancy|boolean|[Patient.reminder](StructureDefinition-anc-patient.html)|

#### ANC.A7 Create client record?OR?ANC.A8. Validate client details

Data elements for this activity can be collected using the [ANCA7](Questionnaire-ANCA7.html)

|Id|Label|Description|Type|Profile Path|
|---|---|---|---|---|
|ANC.A.DE13|Co-habitants|Who does the client live with? It is important to know whether client lives with parents, other family members, a partner, friends, etc.|CodeableConcept|[Observation.value[x]](StructureDefinition-anc-a-de13.html)|

#### ANC.B4 Confirm pregnancy

Data elements for this activity can be collected using the [ANCB4](Questionnaire-ANCB4.html)

|Id|Label|Description|Type|Profile Path|
|---|---|---|---|---|
|ANC.B4.DE1|Pregnancy confirmed|Pregnancy has been confirmed|boolean|[Observation.value[x]](StructureDefinition-anc-b4-de1.html)|

#### ANC.B5 Quick check

Data elements for this activity can be collected using the [ANCB5](Questionnaire-ANCB5.html)

|Id|Label|Description|Type|Profile Path|
|---|---|---|---|---|
|ANC.B5.DE48|Danger signs|Before each contact, the health worker should check whether the woman has any of the danger signs listed here – if yes, she should refer to the hospital urgently; if no, she should continue to the normal contact|CodeableConcept|[Observation.value[x]](StructureDefinition-anc-b5-de48.html)|
|ANC.B5.DE5|Specific health concern(s)|If the woman came to the facility with a specific health concern, select the health concern(s) from the list|CodeableConcept|[Observation.value[x]](StructureDefinition-anc-b5-de5.html)|

#### ANC.End End

Data elements for this activity can be collected using the [ANCEnd](Questionnaire-ANCEnd.html)

|Id|Label|Description|Type|Profile Path|
|---|---|---|---|---|
|ANC.End.1|Reason for closing ANC record|Select the reason why you are closing the woman's ANC record|CodeableConcept|[Observation.value[x]](StructureDefinition-anc-end-1.html)|
|ANC.End.12|Delivery date|Date on which the woman delivered|dateTime|[Observation.value[x]](StructureDefinition-anc-end-12.html)|
|ANC.End.13|Place of delivery|Place where the woman delivered|CodeableConcept|[Encounter.location.location](StructureDefinition-anc-end-13.html)|
|ANC.End.17|Preterm Birth|The woman gave birth when the gestational age was less than 37 weeks|boolean|[Observation.value[x]](StructureDefinition-anc-end-17.html)|
|ANC.End.18|Delivery mode|How the woman gave birth/delivered|CodeableConcept|[Observation.value[x]](StructureDefinition-anc-end-18.html)|
|ANC.End.23|Birth weight|Enter the birth weight of the baby in kg|Quantity|[Observation.value[x]](StructureDefinition-anc-end-23.html)|
