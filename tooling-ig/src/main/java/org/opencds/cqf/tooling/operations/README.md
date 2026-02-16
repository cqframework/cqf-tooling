# Operations

This package contains all the operations defined in the CQF Tooling project. Care has been taken to organize the 
operations into relevant sub-packages. Operations are defined using the @Operation and @OperationParam annotations to 
provide the necessary information and metadata for operation discovery and functionality. Additionally, operations should 
implement the appropriate interface for execution (e.g. ExecutableOperation).

## @Operation Annotation

The @Operation annotation is a type annotation - meaning it is used at the class level. The annotation is used to tag 
operations with the operation name and make it discoverable by the tooling (see the tooling-cli module Main and 
OperationFactory classes to see how operations are discovered and initialized).

### Example

```java
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperation;

@Operation(name = "MyOperation")
class MyOperation implements ExecutableOperation {
   @Override
   public void execute() {
      // Operation logic goes here
   }
}
```

## @OperationParam Annotation

The @OperationParam annotation is a field annotation - meaning it is used at the class data member level. The annotation 
is used to tag class data members with necessary metadata needed to initialize and validate operation input parameters. 
The @OperationParam annotation has the following elements:
- alias - the name(s) used to reference the parameter
- required - determines whether the parameter is required (default is false or not required)
- setter - identifies the method defined in the operation class to set the corresponding parameter value
- defaultValue - defines the default value of the parameter if one is not provided during invocation

### Example

```java
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.OperationParam;

@Operation(name = "MyOperation")
class MyOperation implements ExecutableOperation {

   @OperationParam(alias = {"ptr", "pathtoresources"}, setter = "setPathToResources", required = true)
   private String pathToResources;
   
   @OperationParam(alias = {"e", "encoding"}, setter = "setEncoding", defaultValue = "json")
   private String encoding;

   @Override
   public void execute() {
      // Operation logic goes here
   }

   public void setPathToResources(String pathToResources) {
      this.pathToResources = pathToResources;
   }

   public void setEncoding(String encoding) {
      this.encoding = encoding;
   }
}
```
