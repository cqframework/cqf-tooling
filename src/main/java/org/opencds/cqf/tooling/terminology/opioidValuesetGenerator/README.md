#OpioidValueSetGenerator

##Introduction
This operation is designed to take a spreadsheet template with definitions created by **_MD Partners, Inc._** and produce valuesets.

##Operation Description

###Command Line and Arguments 
The operation can be called from the command line:
```
JAVA -jar tooling-1.3.1-SNAPSHOT-jar-with-dependencies.jar -OpioidXlsxToValueSet -pts="D:\sandbox\vsac_xlsx\input\CDC Opioid Terminology Template.xlsx" -op="D:\sandbox\vsac_xlsx\output"
```
tooling-1.3.1-SNAPSHOT-jar-with-dependencies.jar is the path and actual name of the current version of the cqf-tooling jar. The possible command line arguments are:

* -pts Which is the path to the spreadsheet containing the valuesets. i.e. ipts="D:\sandbox\vsac_xlsx\input\CDC Opioid Terminology Template.xlsx"
* -op Which is the output path for the created valuesets. i.e. -op="D:\sandbox\vsac_xlsx\output"
* -e Which is the output format. If not provided this defaults to *.json format. i.e. -e=json
* -opp Which is the output file prefix. If not provided it defaults to "valueset-".
* -fhv Which is the output FHIR version. If not provided this defaults to r4.

###Expansion and Compose
The expansion and compose sections of the valuesets are created only for the version r4. The operation parses the first spreadsheet gathering information and using the Value Sets section to determine which sheets to use corresponding to specific valuesets. There are 2 sheets for each valueset.

The first sheet with the -md suffix is the metadata sheet and may contain the compose section for each valueset. If that line is present the information from that line is used to create the compose portion  of the valueset. If there is no compose section one is created using the system "http://snomed.info/sct". 

The expansion portion is created from the code list found on the second sheet with the "-cl" suffix for the valueset.

If there is an expansion then the valueset is executable and will contain the extension 
```
{
"url": "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability",
"valueCode": "executable"
}
```

If there is a compose section then the valueset is computable and will contain the extension 
```
{
"url": "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability",
"valueCode": "computable"
}
```

These are not mutually exclusive. There can be both or either of them.
