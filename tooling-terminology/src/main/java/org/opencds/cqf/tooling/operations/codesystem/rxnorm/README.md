# RxMixWorkflow Operation

The purpose of this operation is to generate a simple FHIR ValueSet resource given a text narrative (rules text), XML 
workflow library, input values, include and exclude filters using the RxMix API (https://mor.nlm.nih.gov/RxMix/).

The returned ValueSet resource will be very simple; including the rules text as an extension and the resulting API call 
and post-processed codes in the ValueSet.compose element.

## Arguments:
- -rulestext | -rt (required) - An expression that provides an alternative definition of the content of the value set in some form that is not computable - e.g. instructions that could only be followed by a human.
- -workflow | -wf (required) - The workflow library expressed in XML that identifies that API functions needed to produce the desired output.
- -input | -in (required) - The input values needed to run the workflow as a comma-delimited list of strings if multiple inputs are needed.
- -includefilter | -if (required) - The filter(s) that must be present within the RXCUI names for inclusion in the final result. Provide a comma-delimited list of strings for multiple filters.
- -excludefilter | -ef (required) - The filter(s) that must not be present within the RXCUI names for inclusion in the final result. Provide a comma-delimited list of strings for multiple filters.
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
    - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting FHIR ValueSet resource { json, xml }.
    - Default encoding: json
- -outputpath | -op (optional) - The directory path to which the resulting FHIR ValueSet resource should be written.
    - Default output path: src/main/resources/org/opencds/cqf/tooling/terminology/output