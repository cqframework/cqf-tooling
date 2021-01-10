package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.util.List;

import org.cdsframework.dto.ConditionDTO;
import org.hl7.fhir.r5.model.Library;
import org.opencds.cqf.tooling.processor.LibraryProcessor;

public class LibraryResourceVisitor extends CqlFileVisitor {


    public LibraryResourceVisitor(String outputDirectoryPath, Enum<CQLTYPES> type) {
        super(outputDirectoryPath, type);
    }
    
    @Override
    public void visit(List<ConditionDTO> conditionDTO) {
        super.visit(conditionDTO);
        LibraryProcessor libraryProcessor = new LibraryProcessor();
        List<Library> libraries = libraryProcessor.refreshGeneratedContent(this.outputDirectoryPath, "4.0.1");
    }
}
