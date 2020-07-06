package org.opencds.cqf.operation;

import org.opencds.cqf.Operation;
import org.opencds.cqf.parameter.RefreshLibraryParameters;
import org.opencds.cqf.processor.LibraryProcessor;
import org.opencds.cqf.processor.R4LibraryProcessor;
import org.opencds.cqf.processor.STU3LibraryProcessor;
import org.opencds.cqf.processor.argument.RefreshLibraryArgumentProcessor;
import org.opencds.cqf.utilities.IOUtils;
import org.opencds.cqf.utilities.LogUtils;

public class RefreshLibraryOperation extends Operation {

    public RefreshLibraryOperation() {    
    } 

    @Override
    public void execute(String[] args) {
        RefreshLibraryParameters params = null;
        try {
            params = new RefreshLibraryArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        String refreshedLibraryName = null;
        LibraryProcessor libraryProcessor;

        switch (params.fhirContext.getVersion().getVersion()) {
        case DSTU3:
            libraryProcessor = new STU3LibraryProcessor();
            refreshedLibraryName = refreshLibraryContent(params, libraryProcessor);
            break;
        case R4:
            libraryProcessor = new R4LibraryProcessor();
            refreshedLibraryName = refreshLibraryContent(params, libraryProcessor);
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown fhir version: " + params.fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        if (refreshedLibraryName == null) {
            LogUtils.info("No libraries successfully refreshed.");
            LogUtils.warn(params.cqlContentPath);
        }
        else if (refreshedLibraryName.isEmpty() || refreshedLibraryName.equals("")) {
            System.out.println("Library successfully generated");
        }
        else {
            System.out.println("Library successfully refreshed");
        }
    }  
    
    public static String refreshLibraryContent(RefreshLibraryParameters params, LibraryProcessor libraryProcessor) {
        try {
            if(params.libraryPath.isEmpty()) {
                try {
                    params.libraryPath = IOUtils.getLibraryPathAssociatedWithCqlFileName(params.cqlContentPath, params.fhirContext);
                } catch (Exception e) {
                    LogUtils.putException(params.cqlContentPath, e);
                    LogUtils.warn(params.cqlContentPath);
                }
            }
            libraryProcessor.refreshLibraryContent(params);
            return params.libraryPath;
        } catch (Exception e) {
            LogUtils.putException(params.cqlContentPath, e);
        }
        LogUtils.warn(params.cqlContentPath);
        return null;
    }
}

