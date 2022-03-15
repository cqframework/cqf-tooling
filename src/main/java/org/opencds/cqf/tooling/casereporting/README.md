Steps to get ErsdV2 from ErsdV1

- Download ErsdV1 bundle at https://ersd.aimsplatform.org/#/home

- remove fhir_comments

- replace the example PlanDefinition with eRSD-instance-example from the case-reporting IG
    (http://build.fhir.org/ig/HL7/case-reporting/PlanDefinition-plandefinition-ersd-instance-example.json.html)

- remove the -output and -input from  the DataRequirements in the actions.
    i.e. "eicr-report-output" -> "eicr-report"

- run the ErsdTransformer