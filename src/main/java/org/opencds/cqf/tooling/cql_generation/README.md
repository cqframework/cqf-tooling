The CqlGenerator Interface provides an Interface for generation Cql given a URI or String input for the Vmr/Drools data and the output for the location of the generated Cql and Elm Libraries.  There is an Operation that is wired up at GenerateCQLFromDroolOperation.java  it takes an output path, encoding, fhirversion, input path, and the type for the granularity of the cql you want either from the Condition level or the ConditionRel level.  CqlTypes.java has the two options there.
The CqlGenerator was built with that interface in order to allow for Transformations from other data representations not just Drools / Vmr.  And you will see that consistently throughout this project.  Everything is Abstracted and separated in order to allow for different Modeling and Logic representations.  So the the DroolCqlGenerator takes those paths or URI's and deserialized the Conditions.  Then it sets up the VmrToModelElmBuilder sets up the traverser with the proper visitor builds the libraries with the context.  As of right now it writes out the libraries in the method, but this should really be pulled out and put into the Operation or something like that.  Everything is built up in the ElmContext, so you should be able to grab the Cql Libraries and the Elm Libraries from the resulting ElmContext.



DroolToCqlGenerator
**Builds ElmContext using a DroolTraverser registered with a DroolToElmVisitor, 
**then for each library in the ElmContext set up a ElmToCqlVisitor building cql string context then writes the cql and elm to file

	DepthFirstDroolTraverser
	**Traverses the Vmr/Drools Object graph Depth First and calls Visitor returns an ElmContext containing the 
	**ElmLibraries and other relavent information.  May toggle Library Granularity by 1 library per 
	**ConditionRel, or 1 library per Condition
		DroolToElmVisitor
		**Visits each node of the Vmr/Drools Object graph and calls appropriate Converters and builds ElmContext
			Converters
			**Converts Vmr/Drools Object information into the equivalent Elm representation
				VmrToFhirElmBuilder builds Fhir Modeling from Vmr Patient Data Information
				VmrToModelElmBuilder builds Elm Objects from Vmr Object Information

	ElmToCqlVisitor
	**Traverses the ElmLibrary object graph building up an ElmContext with the String representation of a CqlLibrary