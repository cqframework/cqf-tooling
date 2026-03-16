package org.opencds.cqf.tooling.common;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class SoftwareSystemTest {

    @Test
    public void testAllFieldsProvided() {
        SoftwareSystem ss = new SoftwareSystem("MyTool", "1.0.0", "MyOrg");
        assertEquals(ss.getName(), "MyTool");
        assertEquals(ss.getVersion(), "1.0.0");
        assertEquals(ss.getManufacturer(), "MyOrg");
    }

    @Test
    public void testNullVersionFallsBackToSpecificationVersion() {
        SoftwareSystem ss = new SoftwareSystem("MyTool", null, "MyOrg");
        assertNotNull(ss.getVersion(), "Version should fall back, never be null");
        // In a test environment, Package.getSpecificationVersion() returns null,
        // so the second fallback to "unspecified" should kick in
        assertEquals(ss.getVersion(), "unspecified");
    }

    @Test
    public void testNullManufacturerDefaultsToCQFramework() {
        SoftwareSystem ss = new SoftwareSystem("MyTool", "2.0.0", null);
        assertEquals(ss.getManufacturer(), "CQFramework");
    }

    @Test
    public void testAllNullsGetDefaults() {
        SoftwareSystem ss = new SoftwareSystem(null, null, null);
        assertNull(ss.getName(), "Name has no default");
        assertEquals(ss.getVersion(), "unspecified");
        assertEquals(ss.getManufacturer(), "CQFramework");
    }

    @Test
    public void testExplicitVersionNotOverridden() {
        SoftwareSystem ss = new SoftwareSystem("Tool", "3.0.0", "Org");
        assertEquals(ss.getVersion(), "3.0.0",
                "Explicit non-null version should not be overridden by fallback");
    }
}
