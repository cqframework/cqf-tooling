package org.opencds.cqf.tooling.exception;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class ExceptionClassesTest {

    @Test
    public void testIGInitializationException() {
        IGInitializationException e1 = new IGInitializationException();
        assertNotNull(e1);

        IGInitializationException e2 = new IGInitializationException("msg");
        assertEquals(e2.getMessage(), "msg");

        RuntimeException cause = new RuntimeException("root");
        IGInitializationException e3 = new IGInitializationException("msg", cause);
        assertEquals(e3.getMessage(), "msg");
        assertSame(e3.getCause(), cause);

        IGInitializationException e4 = new IGInitializationException(cause);
        assertSame(e4.getCause(), cause);
    }

    @Test
    public void testNpmPackageManagerException() {
        NpmPackageManagerException e1 = new NpmPackageManagerException();
        assertNotNull(e1);

        NpmPackageManagerException e2 = new NpmPackageManagerException("msg");
        assertEquals(e2.getMessage(), "msg");

        RuntimeException cause = new RuntimeException("root");
        NpmPackageManagerException e3 = new NpmPackageManagerException("msg", cause);
        assertEquals(e3.getMessage(), "msg");
        assertSame(e3.getCause(), cause);

        NpmPackageManagerException e4 = new NpmPackageManagerException(cause);
        assertSame(e4.getCause(), cause);
    }

    @Test
    public void testInvalidOperationInitialization() {
        InvalidOperationInitialization e1 = new InvalidOperationInitialization();
        assertNotNull(e1);

        InvalidOperationInitialization e2 = new InvalidOperationInitialization("msg");
        assertEquals(e2.getMessage(), "msg");

        RuntimeException cause = new RuntimeException("root");
        InvalidOperationInitialization e3 = new InvalidOperationInitialization("msg", cause);
        assertEquals(e3.getMessage(), "msg");
        assertSame(e3.getCause(), cause);
    }

    @Test
    public void testInvalidOperationArgs() {
        InvalidOperationArgs e1 = new InvalidOperationArgs();
        assertNotNull(e1);

        InvalidOperationArgs e2 = new InvalidOperationArgs("bad args");
        assertEquals(e2.getMessage(), "bad args");
    }

    @Test
    public void testOperationNotFound() {
        OperationNotFound e1 = new OperationNotFound();
        assertNotNull(e1);

        OperationNotFound e2 = new OperationNotFound("not found");
        assertEquals(e2.getMessage(), "not found");
    }

    @Test
    public void testInvalidCanonical() {
        InvalidCanonical e1 = new InvalidCanonical();
        assertNotNull(e1);

        InvalidCanonical e2 = new InvalidCanonical("bad canonical");
        assertEquals(e2.getMessage(), "bad canonical");
    }
}
