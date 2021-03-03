package org.opencds.cqf.individual_tooling.cql_generation.drool.serialization;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.hl7.cql_annotations.r1.Annotation;

public class Serializer {
    Class<?> clazz;

    public Serializer(Class<?> clazz) {
        this.clazz = clazz;
	}

	public String convertToXml(Object object, JAXBContext context) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.getBuffer().toString().replace("<xml version=\"1.0\"", "<xml version=\"1.1\"").replace("\f", "&#xc;");
    }
    
    public JAXBContext getJaxbContext() {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(clazz, Annotation.class);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating JAXBContext - " + e.getMessage());
        }
        return jaxbContext;
    }
}
