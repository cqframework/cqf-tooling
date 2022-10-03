package org.opencds.cqf.tooling.plandefinition.stu3;

import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.plandefinition.PlanDefinitionProcessor;
import org.opencds.cqf.tooling.processor.CDSHooksProcessor;

public class STU3PlanDefinitionProcessor extends PlanDefinitionProcessor {
   public STU3PlanDefinitionProcessor(LibraryProcessor libraryProcessor, CDSHooksProcessor cdsHooksProcessor) {
      super(libraryProcessor, cdsHooksProcessor);
   }
}
