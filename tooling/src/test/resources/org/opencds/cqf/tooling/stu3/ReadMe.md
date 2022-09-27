# Refresh IG
Refreshing the IG includes two steps:

(note that the first step is only required periodically, to get the latest version of the tooling that refreshes the IG)
* run _updateRefreshIG: as noted, this will download the latestest version of the CQF Tooling to the input-cache directory, in the form of a java jar.
* run _refresh: by default, this uses the CQF Tooling to perform the following steps:
    * for each .cql and Measure resource combination in the IG:
        * create or update the Library resource
        * bundle the associated Patient Scenario resources
        * create a "master" bundle that includes:
            * primary Library resource
            * Measure resource
            * Library resource dependencies
            * ValueSet resource dependencies
            * associated Patient Scenario resources bundles
        * post the "master" bundle to the Connectathon FHIR Server (http://cqm-sandbox.alphora.com/)
        * save the following to the bundles directory of the IG:
           * "master" bundle
           * .cql file
           * primary Library resource
           * Measure resource
           * Library resource dependencies bundle
           * ValueSet resource dependencies bundle
           * a separate bundle for each set of associated Patient Scenario resources
           * a separate MeasureReport resource for each assocaited Patient Scenario (if present)

* The default behavior of running _refresh can be adjusted by changing the options in the _refresh file.  One such usage is to post to a differnt FHIR Server (ex: local).  To do so, modify the -fs (FHIR Server) option in _refresh.  For more details on other options, run the CQF Tooling from the command line with -RefreshIG  -help.

## Caveats:
* If new ValueSet resources are posted to a CQF Ruler FHIR Server, they must be manually updated with {{serverurl}}$updateCodeSystems.  Due to the potentially long duration of invoking $updateCodeSystems, the tooling does not run it as part of posting new ValueSet resources.
* Updating generated content for Measures is not currenlty implemented in the tooling.  If a new cql file is introduced the tooling will NOT create a Measure resource, nor will it refresh existing Measure resources.  Those operations need to be done manually.