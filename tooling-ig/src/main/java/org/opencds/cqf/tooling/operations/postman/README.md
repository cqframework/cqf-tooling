# Postman Operations

The operations defined in this package provide support for generating Postman collections from FHIR resources.

## PostmanCollection Operation

This operation generates a Postman collection from measure Bundle resources. The generated collection includes
requests for evaluating measures via the $evaluate-measure operation.

### Arguments:
- -pathtobundledir | -ptbd (required) - Path to the directory containing bundle files
- -version | -v (required) - FHIR version { dstu3, r4 }
- -outputpath | -op (optional) - The directory path to which the Postman collection should be written
- -protocol (optional) - The URL protocol { http, https }
  - Default: http
- -host (optional) - The URL host (e.g. 'localhost:8080')
  - Default: {server-base}
- -path (optional) - The URL path (e.g. 'fhir')
  - Default: {path}
- -name (optional) - Name for the Postman collection
  - Default: timestamped name

### Example:

    java -jar tooling-cli-3.0.0-SNAPSHOT.jar -PostmanCollection -v=r4 -ptbd="/path/to/bundles" -host="localhost:8080" -path="fhir"
