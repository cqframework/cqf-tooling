#OpioidValueSetGenerator

##Introduction
This operation is designed to take a spreadsheet template with definitions created by **_MD Partners, Inc._** and produce valuesets. The operation does not deduplicate valuesets. If one is entered into the spreadsheet tiwce, it will appear in the resulting valueset twice.

NOTE: If downloading the Excel spreadsheet from Google Sheets, first make sure that the Codes have been formatted as text and not as numbers. Otherwise when downloaded as Excel the larger numbers will appear as numbers with scientific notation and will not be correct.  To do this in Google sheets, select the column of codes, click on "Format/Number" and select "Plain Text". Doing this after the download does not rectify the situation.   

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
JAVA -jar \sandbox\valuesets\input-cache\tooling-2.1.0-SNAPSHOT.jar -TemplateToValueSetGenerator -pts="D:\sandbox\valuesets\input\CDC Opioid Terminology Template.xlsx" -op="D:\sandbox\valuesets\output"
```
tooling-2.1.0-SNAPSHOT.jar is the path and actual name of the current version of the cqf-tooling jar. The possible command line arguments are:

* -pts Which is the path to the spreadsheet containing the valuesets. i.e. -pts="D:\sandbox\vsac_xlsx\input\CDC Opioid Terminology Template.xlsx"
* -op Which is the output path directory for operation to put the created valuesets. i.e. -op="D:\sandbox\vsac_xlsx\output"
* -e Which is the output format. If not provided this defaults to *.json format. i.e. -e=json
* -opp Which is the output file prefix. If not provided it defaults to "valueset-".
* -opv Which is the output FHIR version. If not provided this defaults to r4.

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

##Spreadsheet Requirements
Currently the valueset generator depends on sheet and cell location to determine values.
Sheet 1 is the Common MetaData sheet and contains informational data such as the author, publisher and additional information. It also contains the CodeSystems with url, version and date updated. This is not position or row dependent, beyond the fact that the fist column is the key word that is used to access the corresponding data located in the second column. For example, column one might contain "Publisher" and the second column would contain the text for the publisher. 

Sheet 2 is the Value Set List. Row 1 is the titles. Subsequent rows contain valueset titles and references to the sheets containing metadata and codes for inclusion in the valuesets. 


##Errors
Possible errors that may occur:
1. com.ctc.wstx.exc.WstxUnexpectedCharException: Unexpected character '?' (code 63) in end tag Expected '>'.
    
Control characters are not allowed in spreadsheets. They are replaced with the '?' character and this will cause this error, which will stop processing. This may include such characters as '\n' (form feed). Look up the "cntrl" Character Class in regex for other examples.  