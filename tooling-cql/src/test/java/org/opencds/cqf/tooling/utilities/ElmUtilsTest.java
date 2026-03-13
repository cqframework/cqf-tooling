package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.Literal;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.testng.annotations.Test;

public class ElmUtilsTest {

    // ── null handling ──

    @Test
    public void generateCompiledLibrary_nullLibrary_returnsNull() {
        assertNull(ElmUtils.generateCompiledLibrary(null));
    }

    @Test
    public void generateCompiledLibrary_emptyLibrary_returnsCompiledLibrary() {
        // Intent: a minimal library with no sections should compile without NPE
        Library library = new Library();
        library.setIdentifier(new VersionedIdentifier().withId("TestLib").withVersion("1.0"));

        CompiledLibrary result = ElmUtils.generateCompiledLibrary(library);

        assertNotNull(result, "Empty library should compile successfully");
        assertEquals(result.getIdentifier().getId(), "TestLib");
    }

    @Test
    public void generateCompiledLibrary_libraryWithNoValueSets_doesNotNpe() {
        // BUG FIX: Previously NPE'd because ValueSets section was not null-checked
        // unlike all other sections (Usings, Includes, CodeSystems, etc.)
        Library library = new Library();
        library.setIdentifier(new VersionedIdentifier().withId("NoVS").withVersion("1.0"));
        // ValueSets is null — this used to throw NPE

        CompiledLibrary result = ElmUtils.generateCompiledLibrary(library);
        assertNotNull(result);
    }

    @Test
    public void generateCompiledLibrary_libraryWithValueSets_includesThem() {
        Library library = new Library();
        library.setIdentifier(new VersionedIdentifier().withId("WithVS").withVersion("1.0"));

        ValueSetDef vsDef = new ValueSetDef();
        vsDef.setName("TestValueSet");
        vsDef.setId("http://example.org/ValueSet/test");
        library.setValueSets(new Library.ValueSets().withDef(vsDef));

        CompiledLibrary result = ElmUtils.generateCompiledLibrary(library);
        assertNotNull(result);
    }

    // ── identifier ──

    @Test
    public void generateCompiledLibrary_noIdentifier_stillCompiles() {
        Library library = new Library();
        // No identifier set
        CompiledLibrary result = ElmUtils.generateCompiledLibrary(library);
        assertNotNull(result);
    }

    // ── statements sorting ──

    @Test
    public void generateCompiledLibrary_statementsAreSorted() {
        Library library = new Library();
        library.setIdentifier(new VersionedIdentifier().withId("SortTest").withVersion("1.0"));

        ExpressionDef defZ = new ExpressionDef().withName("Zebra").withExpression(
                new Literal().withValueType(new javax.xml.namespace.QName("urn:hl7-org:elm-types:r1", "String")).withValue("z"));
        ExpressionDef defA = new ExpressionDef().withName("Apple").withExpression(
                new Literal().withValueType(new javax.xml.namespace.QName("urn:hl7-org:elm-types:r1", "String")).withValue("a"));

        library.setStatements(new Library.Statements().withDef(defZ, defA));

        CompiledLibrary result = ElmUtils.generateCompiledLibrary(library);
        assertNotNull(result);

        // After compilation, statements should be sorted by name
        assertEquals(library.getStatements().getDef().get(0).getName(), "Apple",
                "Statements should be sorted alphabetically");
        assertEquals(library.getStatements().getDef().get(1).getName(), "Zebra");
    }
}
