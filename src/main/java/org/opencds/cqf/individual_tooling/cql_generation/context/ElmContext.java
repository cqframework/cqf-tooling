package org.opencds.cqf.individual_tooling.cql_generation.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.ModelManager;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.ValueSetDef;
import org.opencds.cqf.tooling.library.GenericLibrarySourceProvider;

public class ElmContext {
    private LibraryBuilder libraryBuilder;
    private Map<String, ValueSetDef> valuesetDefs = new HashMap<String, ValueSetDef>();
    public Stack<Expression> expressionStack = new Stack<Expression>();

    public ElmContext() {
        ModelManager modelManager = new ModelManager();
        LibraryManager libraryManager = new LibraryManager(modelManager);
        try {
            UcumService ucumService = new UcumEssenceService(
                    UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
            this.libraryBuilder = new LibraryBuilder(modelManager, libraryManager, ucumService);
        } catch (UcumException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

	public void addContext(ValueSetDef vs) {
        if (valuesetDefs.get(vs.getName()) != null) {
            libraryBuilder.addValueSet(vs);
            valuesetDefs.put(vs.getName(), vs);
        }
	}
    
}
