package org.opencds.cqf.individual_tooling.cql_generation.drool.adapter;

import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.ConditionDTO;
import org.hl7.elm.r1.ContextDef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.ObjectFactory;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;
import org.hl7.cql_annotations.r1.Annotation;

/**
 * Provides adapter functionality for creating Elm Libraries from List<{@link ConditionDTO ConditionDTO}> object graph
 * @author  Joshua Reynolds
 * @since   2021-02-24 
 */
public class LibraryAdapter {

    /**
     * converts every Elm Library Object in the context to the String representation and adds it to the elmLibraryMap
     * @param rootNode rootNode
     * @param context elmContext
     */
    public void adapt(List<ConditionDTO> rootNode, ElmContext context) {
        context.libraries.entrySet().stream().forEach(entry -> {
            System.out.println(entry.getKey());
            try {
                String elm = convertToXml(entry.getValue().getLibrary());
                context.elmLibraryMap.put(entry.getKey(), Pair.of(elm, entry.getValue().getLibrary()));
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Interrogates the {@link ConditionDTO Condition} in order to infer the Library name and a possible header
     * @param conditionDTO
     * @param context
     */
    public void adapt(ConditionDTO conditionDTO, ElmContext context) {
        String libraryName = null;
        String header = null;
        if (conditionDTO.getCdsCodeDTO() != null) {
            libraryName = inferLibraryName(conditionDTO.getCdsCodeDTO().getDisplayName(), context);
            header = conditionDTO.getCdsCodeDTO().getDisplayName();
        } else {
            throw new RuntimeException("Unable to infer library name for condition " + conditionDTO.getUuid().toString());
        }
        resolveContext(libraryName, header, context);
    }

    /**
     * Interrogates the {@link ConditionCriteriaRelDTO conditionCriteriaRel} in order to infer the Library name and a possible header
     * @param conditionCriteriaRel
     * @param context
     */
    public void adapt(ConditionCriteriaRelDTO conditionCriteriaRel, ElmContext context) {
        String libraryName = inferLibraryName(conditionCriteriaRel.getLabel(), context);
        String header = conditionCriteriaRel.getLabel();
        if (conditionCriteriaRel.getCriteriaDTO() != null) {
            libraryName = libraryName.concat( "_" + conditionCriteriaRel.getUuid().toString().substring(0, 5));
            header = header.concat(" " + conditionCriteriaRel.getCriteriaDTO().getCriteriaType().toString());
        }
        resolveContext(libraryName, header, context);
    }

    private String inferLibraryName(String libraryName, ElmContext context) {
        try {
            if (libraryName.replaceAll(" ", "").length() < 50 && !libraryName.contains("=")) {
                libraryName = libraryName.replaceAll(" ", "_").replaceAll("<", "")
                        .replaceAll(">=", "").replaceAll(",", "").replaceAll(":", "").replaceAll("#", "")
                        .replaceAll("TEST", "").replaceAll("TEST2", "").replaceAll("TEST3", "")
                        .replaceAll("TESTExample-Daryl", "").replaceAll("[()]", "");
            } else {
                libraryName = "GeneratedCql" + ElmContext.elmLibraryIndex;
            }
        } catch (Exception e) {
            libraryName = "ErrorWhileGenerated" + ElmContext.elmLibraryIndex;
        }
        return libraryName;
    }

    private void resolveContext(String libraryName, String header, ElmContext context) {
        VersionedIdentifier versionedIdentifier = new VersionedIdentifier().withId(libraryName).withVersion("1.0.0");
        ContextDef contextDef = context.modelBuilder.of.createContextDef().withName("Patient");
        context.newLibraryBuilder(versionedIdentifier, contextDef);
    }

    // pulled from translator
    private String convertToXml(Library library) throws JAXBException {
        Marshaller marshaller = getJaxbContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter writer = new StringWriter();
        marshaller.marshal(new ObjectFactory().createLibrary(library), writer);
        return writer.getBuffer().toString().replace("<xml version=\"1.0\"", "<xml version=\"1.1\"").replace("\f", "&#xc;");
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
