package org.opencds.cqf.tooling.cql.exception;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.testng.annotations.Test;

public class CqlTranslatorExceptionTest {

    // ── Constructor: Exception ──

    @Test
    public void constructorWithException_messageContainsOriginal() {
        Exception cause = new RuntimeException("parse failed");
        CqlTranslatorException ex = new CqlTranslatorException(cause);
        assertTrue(ex.getMessage().contains("parse failed"));
        assertTrue(ex.getMessage().contains("CQL Translation Error(s)"));
    }

    // ── Constructor: String ──

    @Test
    public void constructorWithString_messageContainsInput() {
        CqlTranslatorException ex = new CqlTranslatorException("bad syntax");
        assertTrue(ex.getMessage().contains("bad syntax"));
    }

    // ── Constructor: List<CqlCompilerException> ──

    @Test
    public void constructorWithErrors_storesErrors() {
        List<CqlCompilerException> errors = new ArrayList<>();
        errors.add(new CqlCompilerException("error1"));
        errors.add(new CqlCompilerException("error2"));

        CqlTranslatorException ex = new CqlTranslatorException(errors);
        assertEquals(ex.getErrors().size(), 2);
    }

    // ── Constructor: List<String> with severity ──

    @Test
    public void constructorWithStringsAndSeverity_createsCompilerExceptions() {
        List<String> messages = new ArrayList<>();
        messages.add("missing valueset");
        messages.add("unknown type");

        CqlTranslatorException ex = new CqlTranslatorException(messages, CqlCompilerException.ErrorSeverity.Warning);
        assertEquals(ex.getErrors().size(), 2);
    }

    // ── getErrors mutation-on-read ──

    @Test
    public void getErrors_whenConstructedWithString_hasErrorFromConstructor() {
        // Fixed: errors are populated in the constructor, not lazily in getErrors()
        CqlTranslatorException ex = new CqlTranslatorException("test error");

        List<CqlCompilerException> firstCall = ex.getErrors();
        assertEquals(firstCall.size(), 1);

        // Second call returns the same list — no mutation
        List<CqlCompilerException> secondCall = ex.getErrors();
        assertEquals(secondCall.size(), 1, "getErrors should be idempotent");
    }

    @Test
    public void getErrors_whenConstructedWithException_hasErrorFromConstructor() {
        CqlTranslatorException ex = new CqlTranslatorException(new RuntimeException("oops"));

        List<CqlCompilerException> errors = ex.getErrors();
        assertEquals(errors.size(), 1);
        assertTrue(errors.get(0).getMessage().contains("CQL Translation Error(s)"));
    }

    @Test
    public void getErrors_whenConstructedWithErrorList_doesNotAddExtra() {
        List<CqlCompilerException> original = new ArrayList<>();
        original.add(new CqlCompilerException("real error"));

        CqlTranslatorException ex = new CqlTranslatorException(original);

        List<CqlCompilerException> errors = ex.getErrors();
        assertEquals(errors.size(), 1, "Should not add extra errors when list is non-empty");
        assertEquals(errors.get(0).getMessage(), "real error");
    }

    // ── Serializable ──

    @Test
    public void isSerializable() {
        CqlTranslatorException ex = new CqlTranslatorException("test");
        assertTrue(ex instanceof java.io.Serializable);
    }

    // ── Empty error list ──

    @Test
    public void constructorWithEmptyErrorList_getErrorsReturnsEmpty() {
        // Fixed: getErrors no longer mutates — empty list stays empty
        List<CqlCompilerException> empty = new ArrayList<>();
        CqlTranslatorException ex = new CqlTranslatorException(empty);

        List<CqlCompilerException> errors = ex.getErrors();
        assertTrue(errors.isEmpty(), "Empty error list should remain empty");
    }
}
