## ProcessAcceleratorKit
The ProcessAcceleratorKit function of [CQF Tooling](https://github.com/cqframework/cqf-tooling) will
read in an process a Data Dictionary that is in the form of a spreadsheet - in a particular/expected
format - and generate the Profile, Extensions and vocabulary (i.e., CodeSystem and ValueSet) resources
for the data elements defined in the data dictionary.

The \_processDataDictionary script is configured to invoke that functionality. To do that,
follow the steps below.

### To process a Data Dictionary:

1. Copy Data Dictionary spreadsheet to this directory (./input/datadictionary).
2. Modify the ./scripts/\_processDataDictionary.sh script
    1. Change the datadictionary_filename value to the name of your data dictionary file, including the extension, or rename your file as DD.xlsx.
    2. Change the datadictionary_sheetname value to the name of the sheet in your data dictionary that you want to process or rename that sheet as "Master-1.0”.
    3. Change the scope variable to the set of scope(s) (comma-delimited list) in the Data Dictionary that you would like to process - this is specified in the ‘Core, FP, STI’ column of the expected Data Dictionary format.
3. Run the \_updateCQFTooling script to get the latest CQF Tooling jar.
4. Run the \_processDataDictionary script to process the Data Dictionary file.
   This should produce the profiles, extensions and vocabulary defined in the
   data dictionary and output the results to the respective folders in the IG.
