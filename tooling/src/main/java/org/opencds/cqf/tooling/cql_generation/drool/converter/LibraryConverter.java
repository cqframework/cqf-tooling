package org.opencds.cqf.tooling.cql_generation.drool.converter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.ConditionDTO;
import org.hl7.elm.r1.ContextDef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.tooling.cql_generation.builder.VmrToModelElmBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Provides adapter functionality for creating Elm Libraries
 * @author Joshua Reynolds
 * @since 2021-02-24
 */
public class LibraryConverter {

    private static final Logger logger = LoggerFactory.getLogger(LibraryConverter.class);
    private Map<String, Marker> markers = new HashMap<String, Marker>();

    public LibraryConverter() {
        markers.put("Library", MarkerFactory.getMarker("Library"));
    }

    /**
     * Interrogates the {@link ConditionDTO Condition} in order to infer the Library name and a possible header
     * @param conditionDTO conditionDTO
     * @param modelBuilder modelBuilder
     * @param libraryIndex library index to include in the name
     * @return Pair of (VersionedIdentifier, ContextDef)
     */
    public Pair<VersionedIdentifier, ContextDef> adapt(ConditionDTO conditionDTO, VmrToModelElmBuilder modelBuilder, Integer libraryIndex) {
        String libraryName = null;
        String header = null;
        if (conditionDTO.getCdsCodeDTO() != null) {
            logger.debug(markers.get("Library"), "Resolve Library name...");
            libraryName = inferLibraryName(conditionDTO.getCdsCodeDTO().getDisplayName(), libraryIndex);
            header = conditionDTO.getCdsCodeDTO().getDisplayName();
        } else {
            logger.error(markers.get("Library"), "conditionCriteriaRel.getCriteriaDTO() was null.");
            throw new RuntimeException("Unable to infer library name for condition " + conditionDTO.getUuid().toString());
        }
        logger.debug("Resolving context for library {}", libraryName);
        return resolveContext(libraryName, header, modelBuilder);
    }

    /**
     * Interrogates the {@link ConditionCriteriaRelDTO conditionCriteriaRel} in order to infer the Library name and a possible header
     * @param conditionCriteriaRel conditionCriteriaRel
     * @param modelBuilder modelBuilder
     * @param libraryIndex library index to include in the name
     * @return Pair of (VersionedIdentifier, ContextDef)
     */
    public Pair<VersionedIdentifier, ContextDef> adapt(ConditionCriteriaRelDTO conditionCriteriaRel, VmrToModelElmBuilder modelBuilder, Integer libraryIndex) {
        String libraryName = inferLibraryName(conditionCriteriaRel.getLabel(), libraryIndex);
        String header = conditionCriteriaRel.getLabel();
        if (conditionCriteriaRel.getCriteriaDTO() != null) {
            logger.debug(markers.get("Library"), "Resolve Library name...");
            libraryName = libraryName.concat( "_" + conditionCriteriaRel.getUuid().toString().substring(0, 5));
            header = header.concat(" " + conditionCriteriaRel.getCriteriaDTO().getCriteriaType().toString());
        } else {
            logger.error(markers.get("Library"), "conditionCriteriaRel.getCriteriaDTO() was null.");
            throw new RuntimeException("Unable to infer library name for condition " + conditionCriteriaRel.getUuid().toString());
        }
        logger.debug("Resolving context for library {}", libraryName);
        return resolveContext(libraryName, header, modelBuilder);
    }

    private String inferLibraryName(String libraryName, Integer libraryIndex) {
        try {
            if (libraryName.replaceAll(" ", "").length() < 40 && !libraryName.contains("=")) {
                libraryName = libraryName.replaceAll(" ", "_").replaceAll("<", "")
                        .replaceAll(">=", "").replaceAll(",", "").replaceAll(":", "").replaceAll("#", "")
                        .replaceAll("TEST", "").replaceAll("TEST2", "").replaceAll("TEST3", "")
                        .replaceAll("TESTExample-Daryl", "").replaceAll("[()]", "");
            } else {
                logger.info("Descriptive name was too long, generating library name for {}", libraryName);
                libraryName = "GeneratedCql" + libraryIndex;
            }
        } catch (Exception e) {
            logger.error(markers.get("Library"), "Could not infer library name for {}", libraryName);
            libraryName = "ErrorWhileGenerated" + libraryIndex;
        }
        return libraryName;
    }

    private Pair<VersionedIdentifier, ContextDef> resolveContext(String libraryName, String header, VmrToModelElmBuilder modelBuilder) {
        VersionedIdentifier versionedIdentifier = new VersionedIdentifier().withId(libraryName).withVersion("1.0.0");
        ContextDef contextDef = modelBuilder.of.createContextDef().withName("Patient");
        return Pair.of(versionedIdentifier, contextDef);
    }
}
