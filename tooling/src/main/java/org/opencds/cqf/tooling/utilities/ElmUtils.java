package org.opencds.cqf.tooling.utilities;

import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.hl7.elm.r1.CodeDef;
import org.hl7.elm.r1.CodeSystemDef;
import org.hl7.elm.r1.ConceptDef;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.ParameterDef;
import org.hl7.elm.r1.UsingDef;
import org.hl7.elm.r1.ValueSetDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ElmUtils {

   private ElmUtils() {}

   public static CompiledLibrary generateCompiledLibrary(Library library) {
      if (library == null) {
         return null;
      }
      boolean compilationSuccess = true;
      CompiledLibrary compiledLibrary = new CompiledLibrary();
      try {
         compiledLibrary.setLibrary(library);
         if (library.getIdentifier() != null) {
            compiledLibrary.setIdentifier(library.getIdentifier());
         }

         if (library.getUsings() != null && library.getUsings().getDef() != null) {
            for (UsingDef usingDef : library.getUsings().getDef()) {
               compiledLibrary.add(usingDef);
            }
         }
         if (library.getIncludes() != null && library.getIncludes().getDef() != null) {
            for (IncludeDef includeDef : library.getIncludes().getDef()) {
               compiledLibrary.add(includeDef);
            }
         }
         if (library.getCodeSystems() != null && library.getCodeSystems().getDef() != null) {
            for (CodeSystemDef codeSystemDef : library.getCodeSystems().getDef()) {
               compiledLibrary.add(codeSystemDef);
            }
         }
         for (ValueSetDef valueSetDef : library.getValueSets().getDef()) {
            compiledLibrary.add(valueSetDef);
         }

         if (library.getCodes() != null && library.getCodes().getDef() != null) {
            for (CodeDef codeDef : library.getCodes().getDef()) {
               compiledLibrary.add(codeDef);
            }
         }
         if (library.getConcepts() != null && library.getConcepts().getDef() != null) {
            for (ConceptDef conceptDef : library.getConcepts().getDef()) {
               compiledLibrary.add(conceptDef);
            }
         }
         if (library.getParameters() != null && library.getParameters().getDef() != null) {
            for (ParameterDef parameterDef : library.getParameters().getDef()) {
               compiledLibrary.add(parameterDef);
            }
         }
         if (library.getStatements() != null && library.getStatements().getDef() != null) {
            // Sort the defs - this is needed because the translator uses the binary search algorithm to resolve expressions
            List<ExpressionDef> expressionDefs = new ArrayList<>(library.getStatements().getDef());
            expressionDefs.sort(Comparator.comparing(ExpressionDef::getName));
            expressionDefs.forEach(compiledLibrary::add);
            library.setStatements(new Library.Statements().withDef(expressionDefs));
         }
      } catch (Exception e) {
         compilationSuccess = false;
      }

      if (compilationSuccess) {
         return compiledLibrary;
      }

      return null;
   }
}
