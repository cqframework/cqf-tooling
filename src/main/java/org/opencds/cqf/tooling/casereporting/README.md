# eRSD Transformer

### Overview
The eRSD Transformer is meant for transforming an eRSD v1 bundle into an eRSD v2 bundle. It is implemented 
as an Operation and can be invoked, like other Operations, via commandline on the CQF Tooling jar. The Transformer reads 
in an eRSD v1 bundle from the file specified via the "pathtobundle" input parameter. The contents of that bundle are 
modified, or "transformed" slightly to make them conformant with the eRSD v2 specification. They are then added to a 
new bundle which gets written to the directory specified via the "outputpath" input parameter. 

### Expectations
The expected structure of the eRSD version 1 bundle is a bundle with a single entry that is the bundle containing that 
artifacts (PlanDefinition, ValueSet Library, ValueSets - grouping and leaf). An input bundle that does not conform to 
this expected structure will result in a runtime error. Also, if the input file is JSON encoded, any "fhir_comment" 
elements will need to be manually removed before processing. Both XML and JSON are supported for the input file.

### Invocation and Arguments
command: mvn exec: java -Dexec.args="-TransformErsd (-pathtobundle | -ptb) (-outputpath | -op) [-pathtoplandefinition | -ptpd] [-encoding | -e]"

The following parameters are supported:
- The operation expects -ptb is a path to a file containing the source eRSD v1 bundle
- The -op is the output directory for output v2 bundle
  - The default output path is:
        <location of the CQF Tooling jar being invoked> + "src/main/resources/org/opencds/cqf/tooling/casereporting/output"
- The -ptpd is the path to a file that contains the eRSD-v2-compliant PlanDefinition that should be used to replace the 
  plan definition in the input eRSD v1 bundle. If the argument is not specified, the PlanDefinition in the input bundle
  will be preserved.
- The -e is the desired output encoding(s) for the output bundle. The supported output encodings are: { "json", "xml" }. 
  - The "encoding" argument can be specified multiple times and the transformer will output a bundle for each encoding.
  So if you want both json and xml bundles, you would specify both -e=json and -e=xml. If no encoding argument
  is supplied, the transformer will assume "json" as the default and output a single JSON-encoded bundle.

### Summary Steps
Steps to transform eRSDv1 to eRSDV2

1. Download the eRSDv1 bundle at https://ersd.aimsplatform.org/#/home
2. If transforming the JSON bundle, remove all "fhir_comment" elements.
3. Replace the PlanDefinition resource in the source bundle with the PlanDefinition (either the xml or json version, 
   depending on which encoding you're using for your source bundle) found in the 
   'plandefinition-us-ecr-specification."json"|"xml"' file located here:
          (https://github.com/cqframework/cqf-tooling/master/src/test/resources/org/opencds/cqf/tooling/casereporting/transformer/eRSDv2PlanDefinition)
4. Run the ErsdTransformer by invoking the "TransformErsd" Operation as described above. 
5. Upon completion of a successful transform run, the output eRSDv2 bundle should be written to the file path specified
   in the -op argument. 