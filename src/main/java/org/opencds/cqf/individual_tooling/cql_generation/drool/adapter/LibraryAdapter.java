package org.opencds.cqf.individual_tooling.cql_generation.drool.adapter;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.ConditionDTO;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.cqframework.cql.cql2elm.model.serialization.LibraryWrapper;
import org.hl7.elm.r1.ContextDef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.ObjectFactory;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;
import org.hl7.cql_annotations.r1.Annotation;

public class LibraryAdapter {
    private static int index = 0;
    //libraryName , libraryBuilder
    private Map<String, LibraryBuilder> libraries = new HashMap<String, LibraryBuilder>();
    private final ObjectFactory of = new ObjectFactory();
    private final org.hl7.cql_annotations.r1.ObjectFactory af = new org.hl7.cql_annotations.r1.ObjectFactory();

    public void adapt(List<ConditionDTO> rootNode, ElmContext context) {
        libraries.entrySet().stream().forEach(entry -> {
            System.out.println(entry.getKey());
            try {
                context.elmLibraryMap.put(entry.getKey(), Pair.of(convertToXml(entry.getValue().getLibrary()), entry.getValue().getLibrary()));
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        });
    }

    public void adapt(ConditionDTO conditionDTO, ElmContext context) {
        String libraryName = null;
        String header = null;
        if (conditionDTO.getCdsCodeDTO() != null) {
            libraryName = inferLibraryName(conditionDTO.getCdsCodeDTO().getDisplayName());
            header = conditionDTO.getCdsCodeDTO().getDisplayName();
        } else {
            throw new RuntimeException("Unable to infer library name for condition " + conditionDTO.getUuid().toString());
        }
        resolveContext(libraryName, header, context);
    }

    public void adapt(ConditionCriteriaRelDTO conditionCriteriaRel, ElmContext context) {
        String libraryName = inferLibraryName(conditionCriteriaRel.getLabel());
        String header = conditionCriteriaRel.getLabel();
        if (conditionCriteriaRel.getCriteriaDTO() != null) {
            libraryName = libraryName.concat( "_" + conditionCriteriaRel.getUuid().toString().substring(0, 5));
            header = header.concat(" " + conditionCriteriaRel.getCriteriaDTO().getCriteriaType().toString());
        }
        resolveContext(libraryName, header, context);
    }

    public void buildLibrary(ElmContext context) {
        LibraryBuilder libraryBuilder = context.libraryBuilder;
        // May need to set this earlier
        // libraryBuilder.addInclude(includeDef); I think fhirhelpers can be inferred....
        context.buildLibrary();
        libraries.put(libraryBuilder.getLibraryIdentifier().getId(), libraryBuilder);
        index++;
    }

    private String inferLibraryName(String libraryName) {
        try {
            if (libraryName.replaceAll(" ", "").length() < 50 && !libraryName.contains("=")) {
                libraryName = libraryName.replaceAll(" ", "_").replaceAll("<", "")
                        .replaceAll(">=", "").replaceAll(",", "").replaceAll(":", "").replaceAll("#", "")
                        .replaceAll("TEST", "").replaceAll("TEST2", "").replaceAll("TEST3", "")
                        .replaceAll("TESTExample-Daryl", "").replaceAll("[()]", "");
            } else {
                libraryName = "GeneratedCql" + index;
            }
        } catch (Exception e) {
            libraryName = "ErrorWhileGenerated" + index;
        }
        return libraryName;
    }

    private void resolveContext(String libraryName, String header, ElmContext context) {
        VersionedIdentifier versionedIdentifier = new VersionedIdentifier().withId(libraryName).withVersion("4.0.1");
        ContextDef contextDef = of.createContextDef().withName("Patient");
        context.newLibraryBuilder(versionedIdentifier, contextDef);
    }

    private String convertToJxson(Library library) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        JaxbAnnotationModule annotationModule = new JaxbAnnotationModule();
        mapper.registerModule(annotationModule);
        LibraryWrapper wrapper = new LibraryWrapper();
        wrapper.setLibrary(library);
        return mapper.writeValueAsString(wrapper);
    }

    private String convertToXml(Library library) throws JAXBException {
        Marshaller marshaller = getJaxbContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter writer = new StringWriter();
        marshaller.marshal(library, writer);
        // The marshaller is not encoding the form feed character (presumably because it's not valid in XML 1.0 at all (even encoded)).
        // Tried to get it to write 1.1 XML, but JAXB can't apparently? ()
        // So hacking it after the fact...
        // NOTE: Even after doing this and getting a valid XML 1.1 document with the form feed as a character reference, the JAXB unmarshaller still complains
        // So... basically, form feeds are not supported in ELM XML
        return writer.getBuffer().toString().replace("<xml version=\"1.0\"", "<xml version=\"1.1\"").replace("\f", "&#xc;");

        /*
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        XMLOutputFactory xof = XMLOutputFactory.newFactory();
        try {
            XMLStreamWriter xsr = xof.createXMLStreamWriter(writer);
            xsr.writeStartDocument("1.1");
            marshaller.marshal(new ObjectFactory().createLibrary(library), xsr);
            xsr.writeEndDocument();
            return writer.getBuffer().toString();
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(String.format("Errors occurred attempting to serialize library: %s", e.getMessage()));
        }
        */
    }

    private static JAXBContext getJaxbContext() {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Library.class, Annotation.class);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating JAXBContext - " + e.getMessage());
        }
        return jaxbContext;
    }
}
