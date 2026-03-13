package org.opencds.cqf.tooling.cql_generation.drool.serialization;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.testng.annotations.Test;

public class SerializerTest {

    @XmlRootElement(name = "testObj")
    public static class TestXmlObject {
        public String value = "hello";
    }

    // ── constructor ──

    @Test
    public void constructor_storesClass() {
        Serializer serializer = new Serializer(TestXmlObject.class);
        assertNotNull(serializer);
    }

    // ── getJaxbContext ──

    @Test
    public void getJaxbContext_validClass_returnsContext() {
        Serializer serializer = new Serializer(TestXmlObject.class);
        JAXBContext ctx = serializer.getJaxbContext();
        assertNotNull(ctx);
    }

    // ── convertToXml ──

    @Test
    public void convertToXml_producesXmlOutput() throws JAXBException {
        Serializer serializer = new Serializer(TestXmlObject.class);
        JAXBContext ctx = serializer.getJaxbContext();
        TestXmlObject obj = new TestXmlObject();

        String xml = serializer.convertToXml(obj, ctx);
        assertNotNull(xml);
        assertTrue(xml.contains("testObj"), "Output should contain root element name");
        assertTrue(xml.contains("hello"), "Output should contain field value");
    }

    @Test
    public void convertToXml_replacesXmlVersion_afterFix() throws JAXBException {
        // BUG FIX: The original code used "<xml version" which doesn't match
        // the actual XML declaration "<?xml version". After fix, the replacement
        // should work correctly on valid XML output.
        Serializer serializer = new Serializer(TestXmlObject.class);
        JAXBContext ctx = serializer.getJaxbContext();
        TestXmlObject obj = new TestXmlObject();

        String xml = serializer.convertToXml(obj, ctx);
        // JAXB output starts with <?xml version="1.0" ...?>
        // After the fix, version should be replaced to 1.1
        assertTrue(xml.contains("1.1") || !xml.contains("<?xml"),
                "If XML declaration is present, version should be updated to 1.1");
    }

    @Test
    public void convertToXml_replacesFormFeed() throws JAXBException {
        // The serializer replaces \f (form feed) with &#xc;
        Serializer serializer = new Serializer(TestXmlObject.class);
        JAXBContext ctx = serializer.getJaxbContext();
        TestXmlObject obj = new TestXmlObject();
        obj.value = "before\fafter";

        String xml = serializer.convertToXml(obj, ctx);
        // Form feed should be replaced
        assertTrue(!xml.contains("\f") || xml.contains("&#xc;"),
                "Form feed characters should be escaped");
    }
}
