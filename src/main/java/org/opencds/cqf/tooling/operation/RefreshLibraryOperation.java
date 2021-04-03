package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.RefreshLibraryParameters;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.library.r4.R4LibraryProcessor;
import org.opencds.cqf.tooling.library.stu3.STU3LibraryProcessor;
import org.opencds.cqf.tooling.processor.argument.RefreshLibraryArgumentProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.LogUtils;

import java.util.ArrayList;
import java.util.List;

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

        List<String> refreshedLibraryNames = new ArrayList<String>();
        LibraryProcessor libraryProcessor;

        switch (params.fhirContext.getVersion().getVersion()) {
        case DSTU3:
            libraryProcessor = new STU3LibraryProcessor();
            refreshedLibraryNames = refreshLibraryContent(params, libraryProcessor);
            break;
        case R4:
            libraryProcessor = new R4LibraryProcessor();
            refreshedLibraryNames = refreshLibraryContent(params, libraryProcessor);
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown fhir version: " + params.fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        if (refreshedLibraryNames.size() == 0) {
            LogUtils.info("No libraries successfully refreshed.");
            LogUtils.warn(params.cqlContentPath);
        }
        else {
            for (String libraryName : refreshedLibraryNames) {
                LogUtils.info(String.format("Library %s successfully refreshed", libraryName));
            }
        }
    }
    
    public static List<String> refreshLibraryContent(RefreshLibraryParameters params, LibraryProcessor libraryProcessor) {
        try {
            if(params.libraryPath.isEmpty()) {
                try {
                    params.libraryPath = IOUtils.getLibraryPathAssociatedWithCqlFileName(params.cqlContentPath, params.fhirContext);
                } catch (Exception e) {
                    LogUtils.putException(params.cqlContentPath, e);
                    LogUtils.warn(params.cqlContentPath);
                }
            }
            return libraryProcessor.refreshLibraryContent(params);
        } catch (Exception e) {
            LogUtils.putException(params.cqlContentPath, e);
        }
        LogUtils.warn(params.cqlContentPath);
        return null;
    }
}

