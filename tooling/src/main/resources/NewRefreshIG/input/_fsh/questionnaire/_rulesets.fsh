Alias: $UCUM = http://unitsofmeasure.org
RuleSet: QuestionnaireItem(type, linkId, text)
* linkId = {linkId}
* text = {text}
* type = {type}
RuleSet: QuestionnaireItemRepeats(type, linkId, text)
* linkId = {linkId}
* text = {text}
* type = {type}
* repeats = true
RuleSet: QuestionnaireItemInitialExpression(expression)
* extension[+]
  * url = "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-initialExpression"
  * valueExpression
    * language = #text/cql-identifier
    * expression = {expression}
RuleSet: QuestionnaireItemUnit(code, display)
* extension[+]
  * url = "http://hl7.org/fhir/StructureDefinition/questionnaire-unit"
  * valueCoding
    * system = $UCUM
    * code = {code}
    * display = {display}
RuleSet: QuestionnaireEnableWhenEqualsCoding(question, coding)
* enableWhen[+]
  * question = {question}
  * operator = #=
  * answerCoding = {coding}
RuleSet: QuestionnaireUnitOption(code, display)
* extension[+]
  * url = "http://hl7.org/fhir/StructureDefinition/questionnaire-unitOption"
  * valueCoding
    * system = $UCUM
    * code = {code}
    * display = {display}
RuleSet: QuestionnaireItemSignatureRequired(code, display)
* extension[+]
  * url = "http://hl7.org/fhir/StructureDefinition/questionnaire-signatureRequired"
  * valueCoding
    * system = "urn:iso-astm:E1762-95:2013"
    * code = {code}
    * display = {display}
* extension[+]
  * url = "http://hl7.org/fhir/StructureDefinition/questionnaire-usageMode"
  * valueCode = #capture
RuleSet: QuestionnaireEnableWhenBoolean(question, boolean)
* enableWhen[+]
  * question = {question}
  * operator = #=
  * answerBoolean = {boolean}