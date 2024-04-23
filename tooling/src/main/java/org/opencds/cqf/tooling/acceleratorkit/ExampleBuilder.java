package org.opencds.cqf.tooling.acceleratorkit;

import ca.uhn.fhir.context.*;
import org.hl7.fhir.r4.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ExampleBuilder {
    private FhirContext fc;
    private CanonicalResourceAtlas atlas;
    public CanonicalResourceAtlas getAtlas() {
        return this.atlas;
    }
    public ExampleBuilder setAtlas(CanonicalResourceAtlas atlas) {
        this.atlas = atlas;
        return this;
    }

    private String idScope = "example";
    public String getIdScope() {
        return idScope;
    }
    public ExampleBuilder setIdScope(String idScope) {
        this.idScope = idScope;
        return this;
    }

    public ExampleBuilder() {
        fc = FhirContext.forCached(FhirVersionEnum.R4);
    }

    public Resource build(StructureDefinition sd) {
        return build(sd, null);
    }

    public Resource build(StructureDefinition sd, Map<String, Object> elementValues) {
        if (sd == null) {
            throw new IllegalArgumentException("sd required");
        }

        if (sd.getType() == null) {
            throw new IllegalArgumentException("type required");
        }

        Resource r = (Resource)fc.getResourceDefinition(sd.getType()).newInstance();
        r.setId(sd.getId() + "-" + idScope);
        r.setMeta(new Meta().addProfile(sd.getUrl()));
        buildElements(sd, r, elementValues);
        includeContext(sd, r);
        return r;
    }

    // Patient context
    private String patientContext;
    public String getPatientContext() {
        return patientContext;
    }
    public ExampleBuilder setPatientContext(String patientContext) {
        this.patientContext = patientContext;
        return this;
    }
    private Patient patient;
    private Reference getPatientReference() {
        if (patientContext != null) {
            return new Reference().setReference("Patient/" + patientContext);
        }
        if (patient != null) {
            // TODO: Add display and identifier if present
            return new Reference().setReference("Patient/" + patient.getId());
        }
        return null;
    }

    // Encounter context
    private String encounterContext;
    public String getEncounterContext() {
        return encounterContext;
    }
    public ExampleBuilder setEncounterContext(String encounterContext) {
        this.encounterContext = encounterContext;
        return this;
    }
    private Encounter encounter;
    private Reference getEncounterReference() {
        if (encounterContext != null) {
            return new Reference().setReference("Encounter/" + encounterContext);
        }
        if (encounter != null) {
            // TODO: Add display and identifier if present
            return new Reference().setReference("Encounter/" + encounter.getId());
        }
        return null;
    }

    // Location context: Id of a location. Resources with references to a location will have it set to this location
    private String locationContext;
    public String getLocationContext() {
        return locationContext;
    }
    public ExampleBuilder setLocationContext(String locationContext) {
        this.locationContext = locationContext;
        return this;
    }
    private Reference getLocationReference() {
        if (locationContext != null) {
            return new Reference().setReference("Location/" + locationContext);
        }
        return null;
    }

    private String practitionerContext;
    public String getPractitionerContext() {
        return practitionerContext;
    }
    public ExampleBuilder setPractitionerContext(String practitionerContext) {
        this.practitionerContext = practitionerContext;
        return this;
    }
    private Reference getPractitionerReference() {
        if (practitionerContext != null) {
            return new Reference().setReference("Practitioner/" + practitionerContext);
        }
        return null;
    }

    private String practitionerRoleContext;
    public String getPractitionerRoleContext() {
        return practitionerRoleContext;
    }
    public ExampleBuilder setPractitionerRoleContext(String practitionerRoleContext) {
        this.practitionerRoleContext = practitionerRoleContext;
        return this;
    }
    private Reference getPractitionerRoleReference() {
        if (practitionerRoleContext != null) {
            return new Reference().setReference("PractitionerRole/" + practitionerRoleContext);
        }
        return null;
    }

    private Reference getPractitionerOrPractitionerRoleReference() {
        Reference reference = getPractitionerRoleReference();
        if (reference == null) {
            reference = getPractitionerReference();
        }
        return reference;
    }

    private void includeContext(StructureDefinition sd, Resource r) {
        if (r instanceof Patient) {
            this.patient = (Patient)r;
            includePatientContext((Patient)r);
        }
        else if (r instanceof Encounter) {
            this.encounter = (Encounter)r;
            includeEncounterContext((Encounter)r);
        }
        else if (r instanceof Observation) {
            includeObservationContext((Observation)r);
        }
        else if (r instanceof MedicationRequest) {
            includeMedicationRequestContext((MedicationRequest)r);
        }
        else if (r instanceof Procedure) {
            includeProcedureContext((Procedure)r);
        }
        else if (r instanceof ServiceRequest) {
            includeServiceRequestContext((ServiceRequest)r);
        }
        else if (r instanceof Immunization) {
            includeImmunizationContext((Immunization)r);
        }
        else if (r instanceof Condition) {
            includeConditionContext((Condition)r);
        }
    }

    private void includePatientContext(Patient p) {
        if (!p.hasGeneralPractitioner()) {
            p.addGeneralPractitioner(getPractitionerOrPractitionerRoleReference());
        }
    }

    private void includeEncounterContext(Encounter e) {
        e.setSubject(getPatientReference());
        Reference locationReference = getLocationReference();
        if (locationReference != null) {
            e.addLocation().setLocation(locationReference);
        }
    }

    private void includeObservationContext(Observation o) {
        if (!o.hasStatus()) {
            o.setStatus(Observation.ObservationStatus.FINAL);
        }

        if (!o.hasSubject()) {
            o.setSubject(getPatientReference());
        }

        if (!o.hasEncounter()) {
            o.setEncounter(getEncounterReference());
        }

        if (!o.hasPerformer()) {
            o.addPerformer(getPractitionerOrPractitionerRoleReference());
        }
    }

    private void includeMedicationRequestContext(MedicationRequest mr) {
        if (!mr.hasStatus()) {
            mr.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
        }

        if (!mr.hasSubject()) {
            mr.setSubject(getPatientReference());
        }

        if (!mr.hasRequester()) {
            mr.setRequester(getPractitionerOrPractitionerRoleReference());
        }

        if (!mr.hasEncounter()) {
            mr.setEncounter(getEncounterReference());
        }
    }

    private void includeProcedureContext(Procedure p) {
        if (!p.hasStatus()) {
            p.setStatus(Procedure.ProcedureStatus.COMPLETED);
        }

        if (!p.hasSubject()) {
            p.setSubject(getPatientReference());
        }

        if (!p.hasEncounter()) {
            p.setEncounter(getEncounterReference());
        }

        if (!p.hasPerformer()) {
            Reference performer = getPractitionerOrPractitionerRoleReference();
            if (performer != null) {
                p.addPerformer().setActor(performer);
            }
        }

        if (!p.hasLocation()) {
            p.setLocation(getLocationReference());
        }
    }

    private void includeServiceRequestContext(ServiceRequest sr) {
        if (!sr.hasStatus()) {
            sr.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
        }

        if (!sr.hasSubject()) {
            sr.setSubject(getPatientReference());
        }

        if (!sr.hasEncounter()) {
            sr.setEncounter(getEncounterReference());
        }

        if (!sr.hasLocationReference()) {
            sr.addLocationReference(getLocationReference());
        }
    }

    private void includeImmunizationContext(Immunization i) {
        if (!i.hasStatus()) {
            i.setStatus(Immunization.ImmunizationStatus.COMPLETED);
        }

        if (!i.hasPatient()) {
            i.setPatient(getPatientReference());
        }

        if (!i.hasEncounter()) {
            i.setEncounter(getEncounterReference());
        }
    }

    private void includeConditionContext(Condition c) {
        if (!c.hasClinicalStatus()) {
            c.setClinicalStatus(new CodeableConcept().addCoding(new Coding().setCode("active").setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")));
        }

        if (!c.hasVerificationStatus()) {
            c.setVerificationStatus(new CodeableConcept().addCoding(new Coding().setCode("confirmed").setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status")));
        }

        if (!c.hasSubject()) {
            c.setSubject(getPatientReference());
        }

        if (!c.hasEncounter()) {
            c.setEncounter(getEncounterReference());
        }

        if (!c.hasAsserter()) {
            c.setAsserter(getPractitionerOrPractitionerRoleReference());
        }
    }

    // TODO: Handle slicing and extensions...
    private String getElementName(String elementId) {
        String[] names = elementId.split("\\.");
        if (names.length == 0) {
            throw new IllegalArgumentException(String.format("Could not determine element name for element id %s", elementId));
        }
        return names[names.length - 1];
    }

    private boolean isContinuation(String id, String nextId) {
        return nextId.startsWith(id) && nextId.length() > id.length()
                && (nextId.charAt(id.length()) == '.' || nextId.charAt(id.length()) == ':');
    }

    private Element constructValue(ElementDefinition ed) {
        return null;
    }

    private void visitElement(StructureDefinition sd, List<ElementDefinition> eds, AtomicReference<Integer> index, Base value, Map<String, Object> elementValues, BaseRuntimeElementDefinition<?> parentDefinition) {
        // if the element has children
            // construct a target and call visit on the children
        // otherwise
            // construct a "primitive"
            // set the value of the element on the target
        ElementDefinition ed = eds.get(index.get());
        String elementName = getElementName(ed.getPath());

        BaseRuntimeChildDefinition brcd = parentDefinition.getChildByName(elementName);
        if (brcd == null) {
            throw new IllegalArgumentException(String.format("Could not resolve BaseRuntimeChildDefinition for element id %s, name %s", ed.getId(), elementName));
        }

        ElementDefinition.TypeRefComponent trc = ed.getTypeFirstRep();
        String typeCode = "CodeableConcept"; // Default to a codeableconcept when type is unknown
        String typeProfile = null;
        if (trc != null && trc.getCode() != null && !trc.getCode().isEmpty()) {
            typeCode = trc.getCode();
            if (trc.getProfile() != null && !trc.getProfile().isEmpty()) {
                typeProfile = trc.getProfile().get(0).getValue();
            }
        }

        // If this is a choice type, the children will be the possible values of the choice
        if (brcd instanceof RuntimeChildChoiceDefinition && !elementName.equals("extension")) {
            if (elementName.endsWith("[x]")) {
                elementName = elementName.substring(0, elementName.indexOf("[x]"));
            }
            elementName = elementName + Character.toUpperCase(typeCode.charAt(0)) + typeCode.substring(1);
        }

        // Resolve to the actual data type-level element definition
        BaseRuntimeElementDefinition<?> bred = brcd.getChildByName(elementName);
        if (bred == null) {
            throw new IllegalArgumentException(String.format("Could not resolve BaseRuntimeElementDefinition for element id %s, name %s", ed.getId(), elementName));
        }


        Element elementValue = (Element)bred.newInstance();
        Object givenElementValue = elementValues != null ? elementValues.get(ed.getId()) : null;
        generateValue(sd, ed, elementValue, givenElementValue);
        if (elementName.equals("extension")) {
            Extension extension = new Extension();
            extension.setUrl(typeProfile);
            extension.setValue((Type)elementValue);
            elementValue = extension;
        }
        if (!elementName.equals("extension")) {
            value.setProperty(getElementName(ed.getPath()), elementValue);
        }
        else {
            Extension subExtension = (Extension) ((Extension) elementValue).getValue();
            subExtension.setUrl(((Extension) elementValue).getUrl());
            value.setProperty(getElementName(ed.getPath()), subExtension);
        }

        index.set(index.get() + 1);
        while (index.get() < eds.size()) {
            ElementDefinition e = eds.get(index.get());
            if (isContinuation(ed.getId(), e.getId())) {
                visitElement(sd, eds, index, elementValue, elementValues, bred);
            }
            else {
                break;
            }
        }
    }

    private CodeableConcept getFirstConcept(ValueSet vs, String givenValue) {
        CodeableConcept concept = null;

        // If the value set has an expansion, get the first code in the expansion
        if (vs.hasExpansion() && vs.getExpansion().hasContains()) {
            for (ValueSet.ValueSetExpansionContainsComponent c : vs.getExpansion().getContains()) {
                if (givenValue == null || givenValue.equalsIgnoreCase(c.getCode()) || givenValue.equalsIgnoreCase(c.getDisplay())) {
                    concept = new CodeableConcept().addCoding(new Coding()
                            .setCode(c.getCode())
                            .setDisplay(c.getDisplay())
                            .setSystem(c.getSystem())
                            .setVersion(c.getVersion()));
                }
            }
        }

        // If it has a compose, get the first code in any include
        if (concept == null && vs.hasCompose()) {
            for (ValueSet.ConceptSetComponent c : vs.getCompose().getInclude()) {
                for (ValueSet.ConceptReferenceComponent r : c.getConcept()) {
                    if (givenValue == null || givenValue.equalsIgnoreCase(r.getCode()) || givenValue.equalsIgnoreCase(r.getDisplay())) {
                        concept = new CodeableConcept().addCoding(new Coding()
                                .setCode(r.getCode())
                                .setDisplay(r.getDisplay())
                                .setSystem(c.getSystem())
                                .setVersion(c.getVersion()));
                        break;
                    }
                }
                if (concept != null) {
                    break;
                }
            }
        }

        // If the includes are value set based, get the first concept in any of the value sets
        if (concept == null && vs.hasCompose()) {
            for (ValueSet.ConceptSetComponent c : vs.getCompose().getInclude()) {
                for (CanonicalType r : c.getValueSet()) {
                    ValueSet svs = atlas.getValueSets().getByCanonicalUrlWithVersion(r.getValue());
                    if (svs != null) {
                        concept = getFirstConcept(svs, givenValue);
                        if (concept != null) {
                            break;
                        }
                    }
                }

                if (concept != null) {
                    break;
                }
            }
        }

        // If the includes are system based, get the first concept in any of the code systems
        if (concept == null && vs.hasCompose()) {
            for (ValueSet.ConceptSetComponent c : vs.getCompose().getInclude()) {
                if (c.hasSystem() && atlas.getCodeSystems() != null) {
                    // TODO: Handle code system version
                    CodeSystem cs = atlas.getCodeSystems().getByCanonicalUrlWithVersion(c.getSystem());
                    if (cs != null && cs.hasConcept()) {
                        for (CodeSystem.ConceptDefinitionComponent cd : cs.getConcept()) {
                            if (givenValue == null || givenValue.equalsIgnoreCase(cd.getCode()) || givenValue.equalsIgnoreCase(cd.getDisplay())) {
                                concept = new CodeableConcept().addCoding(new Coding()
                                        .setCode(cd.getCode())
                                        .setDisplay(cd.getDisplay())
                                        .setSystem(c.getSystem())
                                        .setVersion(c.getVersion()));
                                break;
                            }
                        }
                    }
                }

                if (concept != null) {
                    break;
                }
            }
        }

        // Add mappings from any conceptmap that has the coding
        if (concept != null && atlas.getConceptMaps() != null) {
            Coding primaryCoding = concept.getCodingFirstRep();
            if (primaryCoding != null) {
                for (ConceptMap cm : atlas.getConceptMaps().get()) {
                    for (ConceptMap.ConceptMapGroupComponent g : cm.getGroup()) {
                        if (g.hasSource() && g.getSource().equals(primaryCoding.getSystem())) {
                            // TODO: Handle system version...
                            for (ConceptMap.SourceElementComponent e : g.getElement()) {
                                if (e.getCode().equals(primaryCoding.getCode())) {
                                    for (ConceptMap.TargetElementComponent t : e.getTarget()) {
                                        Coding alternateCoding =
                                                new Coding().setCode(t.getCode())
                                                        .setDisplay(t.getDisplay())
                                                        .setSystem(g.getTarget())
                                                        .setVersion(g.getTargetVersion());
                                        concept.addCoding(alternateCoding);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return concept;
    }

    private String toString(Object value) {
        if (value instanceof String) {
            return (String)value;
        }
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        else if (value instanceof String) {
            return Boolean.parseBoolean((String)value);
        }
        return null;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer)value;
        }
        else if (value instanceof String) {
            return Integer.parseInt((String)value);
        }
        return null;
    }

    private BigDecimal toDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal)value;
        }
        else if (value instanceof Double) {
            return new BigDecimal((Double)value);
        }
        else if (value instanceof String) {
            return new BigDecimal((String)value);
        }
        return null;
    }

    // See https://errorprone.info/bugpattern/FromTemporalAccessor
    // Some conversions of LocalDate, LocalDateTime, and Instant to Instant are not supported
    @SuppressWarnings("FromTemporalAccessor")
    private Instant toInstant(Object value) {
        if (value instanceof Instant) {
            return (Instant) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toInstant(ZoneOffset.UTC);
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay(ZoneOffset.UTC).toInstant();
        } else if (value instanceof String) {
            return Instant.parse((String) value);
        }
        return null;
    }

    private BigDecimal toQuantityValue(Object value) {
        if (value instanceof String) {
            return parseQuantityValue((String)value);
        }
        else if (value instanceof BigDecimal) {
            return (BigDecimal)value;
        }
        else if (value instanceof Double) {
            return new BigDecimal((Double)value);
        }
        else if (value instanceof Integer) {
            return new BigDecimal((Integer)value);
        }
        return null;
    }

    private String toQuantityUnit(Object value) {
        if (value instanceof String) {
            return parseQuantityUnit((String)value);
        }
        return null;
    }

    private BigDecimal parseQuantityValue(String value) {
        int spaceIndex = value.indexOf(' ');
        if (spaceIndex > 0) {
            return new BigDecimal(value.substring(0, spaceIndex - 1));
        }
        return new BigDecimal(value);
    }

    private String parseQuantityUnit(String value) {
        int spaceIndex = value.indexOf(' ');
        if (spaceIndex > 0) {
            return value.substring(spaceIndex + 1);
        }
        return null;
    }

    private void generateValue(StructureDefinition sd, ElementDefinition ed, Element value, Object givenValue) {
        if (value instanceof StringType) {
            ((StringType)value).setValue(givenValue != null ? toString(givenValue) : "Asdf");
        }
        else if (value instanceof BooleanType) {
            ((BooleanType)value).setValue(givenValue != null ? toBoolean(givenValue) : true);
        }
        else if (value instanceof IntegerType) {
            ((IntegerType)value).setValue(givenValue != null ? toInteger(givenValue) : 1);
        }
        else if (value instanceof DecimalType) {
            ((DecimalType)value).setValue(givenValue != null ? toDecimal(givenValue) : new BigDecimal(12.5));
        }
        else if (value instanceof DateType) {
            if (givenValue instanceof Date) {
                ((DateType)value).setValue((Date)givenValue);
            }
            else if (givenValue instanceof String) {
                ((DateType)value).setValueAsString((String)givenValue);
            }
            else {
                ((DateType)value).setValue(Date.from(givenValue != null ? toInstant(givenValue) : Instant.now()));
            }
        }
        else if (value instanceof DateTimeType) {
            if (givenValue instanceof Date) {
                ((DateTimeType)value).setValue((Date)givenValue);
            }
            else if (givenValue instanceof String) {
                ((DateTimeType)value).setValueAsString((String)givenValue);
            }
            else {
                ((DateTimeType)value).setValue(Date.from(givenValue != null ? toInstant(givenValue) : Instant.now()));
            }
        }
        else if (value instanceof Quantity) {
            ((Quantity)value).setValue(givenValue != null ? toQuantityValue(givenValue) : new BigDecimal(12.5));
            ((Quantity)value).setUnit(givenValue != null ? toQuantityUnit(givenValue) : "g");
        }
        else if (value instanceof Period) {
            // TODO: GivenValue as a period
            if (givenValue != null) {
                throw new IllegalArgumentException("Not implemented: Given value cannot be a period");
            }
            ((Period)value).setStart(Date.from(Instant.now()));
            ((Period)value).setEnd(Date.from(Instant.now()));
        }
        else if (value instanceof CodeableConcept) {
            // TODO: Look up a value from the binding for this element
            if ((ed.getBinding() != null) && atlas != null && atlas.getValueSets() != null) {
                ValueSet vs = atlas.getValueSets().getByCanonicalUrlWithVersion(ed.getBinding().getValueSet());
                if (vs != null) {
                    CodeableConcept concept = getFirstConcept(vs, toString(givenValue));
                    if (concept != null) {
                        ((CodeableConcept)value).setText(concept.getText());
                        ((CodeableConcept)value).setCoding(concept.getCoding());
                    }
                    else {
                        ((CodeableConcept)value).addCoding()
                                .setCode("example")
                                .setSystem("http://example.org/fhir/CodeSystem/example-codes")
                                .setDisplay(String.format("Valueset binding %s resolved but contained no codes.", ed.getBinding().getValueSet()));
                    }
                }
                else {
                    ((CodeableConcept)value).addCoding()
                            .setCode("example")
                            .setSystem("http://example.org/fhir/CodeSystem/example-codes")
                            .setDisplay(String.format("Could not resolve binding valueset %s", ed.getBinding().getValueSet()));
                }
            }
            else {
                ((CodeableConcept)value).addCoding()
                        .setCode("example")
                        .setSystem("http://example.org/fhir/CodeSystem/example-codes")
                        .setDisplay("Example");
            }
        }
        else if (value instanceof Extension) {
            if (ed.getType("Extension") != null && ed.getType("Extension").getProfile() != null && atlas != null && atlas.getValueSets() != null && atlas.getExtensions() != null) {
                StructureDefinition sdExtension = atlas.getExtensions().getByCanonicalUrlWithVersion(ed.getType("Extension").getProfile().get(0).getValue());
                ElementDefinition extensionElement = sdExtension.getDifferential().getElement().stream()
                    .filter(x -> x.hasBinding() && x.getBinding().hasValueSetElement()).findFirst().orElse(null);

                if (extensionElement != null) {
                    Type extensionValue = null;
                    if (extensionElement.getType("CodeableConcept") != null) {
                        extensionValue = new CodeableConcept();
                    }

                    generateValue(sdExtension, extensionElement, extensionValue, givenValue);
                    value.addExtension(new Extension().setUrl(extensionElement.getShort()).setValue(extensionValue));
                }
            }
        }
    }

    private void buildElements(StructureDefinition sd, Resource r, Map<String, Object> elementValues) {
        if (sd.getDifferential() == null) {
            throw new IllegalArgumentException("StructureDefinition must have a differential");
        }

        AtomicReference<Integer> index = new AtomicReference<Integer>(1); // skip the root element
        List<ElementDefinition> eds = sd.getDifferential().getElement();
        while (index.get() < eds.size()) {
            ElementDefinition ed = eds.get(index.get());
            if (isContinuation(sd.getType(), ed.getId())) {
                visitElement(sd, eds, index, r, elementValues, fc.getResourceDefinition(sd.getType()));
            }
            else {
                break;
            }
        }
    }
}
