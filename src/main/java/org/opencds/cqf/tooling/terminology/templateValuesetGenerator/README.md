#OpioidValueSetGenerator

##Introduction
This operation is designed to take a spreadsheet template with definitions created by **_MD Partners, Inc._** and produce valuesets. The operation does not deduplicate valuesets. If one is entered into the spreadsheet tiwce, it will appear in the resulting valueset twice.

add part to set up environment get info on how to do that and put in *HERE*

##Setup
[Cqf-tooling](https://github.com/cqframework/cqf-tooling.git) is a set of opensource tools that provide various useful operations for CQL and IG authors.

###Dependencies
* Go to [OpenJDK](https://adoptopenjdk.net) and download the latest (version 8 or higher) JDK for your platform, and install it.
* Go to [Sample-content-ig](https://github.com/cqframework/sample-content-ig.git) and get the _updateCQFTooling.bat (Windows) or .sh (Linux and MacMac) file. Place this script file in whatever directory you want to work with. Run this script, and it will create an input-cache directory and place the latest tooling jar there. For example from a command prompt:
```
mkdir \sandbox\valuesets
cd \sandbox\valuesets
cp \downloads\_updateCQFTooling.bat .
_updateCQGTooling

```


##Operation Description

###Command Line and Arguments 
The operation can be called from the command line:
```
JAVA -jar \sandbox\valuesets\input-cache\tooling-1.3.1-SNAPSHOT-jar-with-dependencies.jar -OpioidXlsxToValueSet -pts="D:\sandbox\valuesets\input\CDC Opioid Terminology Template.xlsx" -op="D:\sandbox\valuesets\output"
```
tooling-1.3.1-SNAPSHOT-jar-with-dependencies.jar is the path and actual name of the current version of the cqf-tooling jar. The possible command line arguments are:

* -pts Which is the path to the spreadsheet containing the valuesets. i.e. -pts="D:\sandbox\vsac_xlsx\input\CDC Opioid Terminology Template.xlsx"
* -op Which is the output path directory for operation to put the created valuesets. i.e. -op="D:\sandbox\vsac_xlsx\output"
* -e Which is the output format. If not provided this defaults to *.json format. i.e. -e=json
* -opp Which is the output file prefix. If not provided it defaults to "valueset-".
* -fhv Which is the output FHIR version. If not provided this defaults to r4.

###Expansion and Compose
The expansion and compose sections of the valuesets are created versions dstu3 and r4. The cpg extensions are added for only the r4 version. The operation parses the first spreadsheet gathering information and using the Value Sets section to determine which sheets to use corresponding to specific valuesets. There are 2 sheets for each valueset.

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
