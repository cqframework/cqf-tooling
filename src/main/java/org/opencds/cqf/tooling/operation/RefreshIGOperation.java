package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.processor.CDSHooksProcessor;
import org.opencds.cqf.tooling.processor.IGBundleProcessor;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.processor.PlanDefinitionProcessor;
import org.opencds.cqf.tooling.processor.argument.RefreshIGArgumentProcessor;

public class RefreshIGOperation extends Operation {

    public RefreshIGOperation() {    
    } 

    @Override
    public void execute(String[] args) {
    	
    	if (args == null) {
    		throw new IllegalArgumentException();
    	}
    	
        RefreshIGParameters params = null;
        try {
            params = new RefreshIGArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        MeasureProcessor measureProcessor = new MeasureProcessor();
        LibraryProcessor libraryProcessor = new LibraryProcessor();
        CDSHooksProcessor cdsHooksProcessor = new CDSHooksProcessor();
        PlanDefinitionProcessor planDefinitionProcessor = new PlanDefinitionProcessor(libraryProcessor, cdsHooksProcessor);
        IGBundleProcessor igBundleProcessor = new IGBundleProcessor(measureProcessor, planDefinitionProcessor);
        IGProcessor processor = new IGProcessor(igBundleProcessor, libraryProcessor, measureProcessor);
        processor.publishIG(params);
    }
}

