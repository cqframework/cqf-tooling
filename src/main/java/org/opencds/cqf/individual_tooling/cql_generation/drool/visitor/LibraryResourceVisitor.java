package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cdsframework.dto.ConditionDTO;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.tooling.npm.LibraryLoader;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.processor.CqlProcessor.CqlSourceFileInformation;

public class LibraryResourceVisitor extends CqlFileVisitor implements IWorkerContext.ILoggingService {
    private CqlProcessor cqlProcessor;
    private List<String> binaryPaths = null;

    public LibraryResourceVisitor(String outputDirectoryPath) {
        super(outputDirectoryPath);
        try {
            this.binaryPaths = extractBinaryPaths(outputDirectoryPath);
        } catch (IOException e) {
            logMessage(String.format("Errors occurred extracting binary path from IG: ", e.getMessage()));
            throw new IllegalArgumentException("Could not obtain binary path from IG");
        }
        String fhirVersion = "4.0.1";
        LibraryLoader reader = new LibraryLoader(fhirVersion);
        UcumService ucumService = null;
        try {
            ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
        } catch (UcumException e) {
            System.err.println("Could not create UCUM validation service:");
            e.printStackTrace();
        }
        this.cqlProcessor = new CqlProcessor(null, binaryPaths, reader, this, ucumService, null, null);
    }

    private List<String> extractBinaryPaths(String outputDirectoryPath) throws IOException {
        List<String> result = new ArrayList<String>();
        File input = new File(outputDirectoryPath);
        if (input.exists() && input.isDirectory()) {
            result.add(input.getAbsolutePath());
        }
        return result;
    }
    
    @Override
    public void visit(List<ConditionDTO> conditionDTO) {
        super.visit(conditionDTO);
        cqlProcessor.execute();
        for (String path : binaryPaths) {
            Library library = new Library();
            attachContent(library, cqlProcessor, path);
        }
    }

    // Base64 encode content
    private void attachContent(Library library, CqlProcessor cqlProcessor, String path) {
        CqlSourceFileInformation cqlInfo = cqlProcessor.getFileInformation(path);
        if (cqlInfo != null) {
            library.addContent(
                    new Attachment()
                            .setContentType("application/elm+xml")
                            .setData(cqlInfo.getElm())
            );
        } else {
            System.out.println("cqlInfo does not exist, check for translation errors" + path);
        }
    }

    @Override
    public void logMessage(String msg) {
        System.out.println(msg);
    }

    @Override
    public void logDebugMessage(IWorkerContext.ILoggingService.LogCategory category, String msg) {
        logMessage(msg);
    }
}
