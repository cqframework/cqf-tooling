package org.opencds.cqf.tooling.terminology;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MarkdownType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class EnsureExecutableValueSetOperation extends Operation {
    private static final String USAGE_WARNING = "CAUTION: The compose element in this ValueSet resource was inferred from the expansion element. It is NOT an authoritative definition of the value set and is provided only for convenience for systems that assume a compose will be present.";
    private String valueSetPath;
    private String encoding = IOUtils.Encoding.JSON.toString();
    private boolean declareCPGProfiles = true;
    private boolean ensureExecutable = false;
    private boolean ensureComputable = false;
    private boolean force = false;
    private boolean skipVersion = false;
    private FhirContext fhirContext;

    public FhirContext getFhirContext() {
        if (fhirContext == null) {
            fhirContext = FhirContext.forR4Cached();
        }

        return fhirContext;
    }

    @Override
    public void execute(String[] args) {
        //setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output"); //default

        for (String arg : args) {
            if (arg.equals("-EnsureExecutableValueSet")) {
                ensureExecutable = true;
                force = true; // force behavior defaults to true for ensure executable
                continue;
            }
            if (arg.equals("-EnsureComputableValueSet")) {
                ensureComputable = true;
                continue;
            }
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath": case "op": setOutputPath(value); break; // -outputpath (-op)
                case "valuesetpath": case "path": case "vsp": valueSetPath = value; break; // -valuesetpath (-vsp, -path)
                case "encoding": case "e": encoding = value.toLowerCase(); break;
                case "declarecpg": case "cpg": declareCPGProfiles = value.toLowerCase().equals("true") ? true : false; break;
                case "force": case "f": force = value.toLowerCase().equals("true") ? true : false; break;
                case "skipversion": case "sv": skipVersion = value.toLowerCase().equals("true") ? true : false; break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
            if (null == getOutputPath() || getOutputPath().equals("")) {
                setOutputPath(this.valueSetPath); //default to editing files in place
            }
        }

        if (valueSetPath == null) {
            throw new IllegalArgumentException("The path to the value set directory is required");
        }

        for (File file : new File(valueSetPath).listFiles()) {
            if (file.getName().endsWith(".json") || file.getName().endsWith(".xml")) {
                IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), getFhirContext());
                if (resource instanceof ValueSet) {
                    ValueSet valueSet = (ValueSet)resource;
                    if ((ensureExecutable && refreshExpansion(valueSet)) || (ensureComputable && inferCompose(valueSet))) {
                        IOUtils.writeResource(valueSet, super.getOutputPath(), IOUtils.Encoding.parse(encoding), getFhirContext());
                    }
                }
            }
        }
    }

    public boolean refreshExpansion(ValueSet valueSet) {
        if (hasSimpleCompose(valueSet) && (!valueSet.hasExpansion() || force)) {
            ValueSet.ValueSetExpansionComponent expansion = new ValueSet.ValueSetExpansionComponent();
            expansion.setTimestamp(Date.from(Instant.now()));

            //Expansions via EnsureExecutableValueSet are run independent of terminology servers and should be flagged as such
            ArrayList<ValueSet.ValueSetExpansionParameterComponent> expansionParameters = new ArrayList<>();
            ValueSet.ValueSetExpansionParameterComponent parameterNaive = new ValueSet.ValueSetExpansionParameterComponent();
            parameterNaive.setName("naive");
            parameterNaive.setValue(new BooleanType(true));
            expansionParameters.add(parameterNaive);
            expansion.setParameter(expansionParameters);

            for (ValueSet.ConceptSetComponent csc : valueSet.getCompose().getInclude()) {
                for (ValueSet.ConceptReferenceComponent crc : csc.getConcept()) {
                    expansion.addContains()
                            .setCode(crc.getCode())
                            .setSystem(csc.getSystem())
                            .setVersion(csc.getVersion())
                            .setDisplay(crc.getDisplay());
                }
            }
            valueSet.setExpansion(expansion);
            if (declareCPGProfiles) {
                if (!valueSet.getMeta().hasProfile("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-executablevalueset")) {
                    valueSet.getMeta().addProfile("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-executablevalueset");
                }
                ensureKnowledgeCapability(valueSet,"executable");
                ensureKnowledgeRepresentationLevel(valueSet,"executable");
            }
            return true;
        }

        return false;
    }

    private String getSystemCanonicalReference(String system, String version) {
        if (version != null) {
            return system + "|" + version;
        }

        return system;
    }

    private String getSystemUri(String systemCanonicalReference) {
        if (systemCanonicalReference != null) {
            int i = systemCanonicalReference.indexOf("|");
            if (i > 0) {
                return systemCanonicalReference.substring(0, i);
            }
        }
        return systemCanonicalReference;
    }

    private String getVersion(String systemCanonicalReference) {
        if (systemCanonicalReference != null) {
            int i = systemCanonicalReference.indexOf("|");
            if (i > 0) {
                return systemCanonicalReference.substring(i + 1);
            }
        }
        return null;
    }

    // NOTE: This assumes the ValueSet expansion is complete... It will THROW if the expansion is partial
    public boolean inferCompose(ValueSet valueSet) {
        if (valueSet.hasExpansion() && (!valueSet.hasCompose() || force)) {
            int total = valueSet.getExpansion().hasTotal() ? valueSet.getExpansion().getTotal() : -1;
            int count = 0;

            // Index the expansion contains by code system and version
            HashMap<String, Map<String, ValueSet.ValueSetExpansionContainsComponent>> codesBySystem = new HashMap<String, Map<String, ValueSet.ValueSetExpansionContainsComponent>>();
            for (ValueSet.ValueSetExpansionContainsComponent contains : valueSet.getExpansion().getContains()) {
                count++;
                if (contains.hasCode() && contains.hasSystem()) {
                    String scr = getSystemCanonicalReference(contains.getSystem(), !skipVersion ? contains.getVersion() : null);
                    Map<String, ValueSet.ValueSetExpansionContainsComponent> concepts = codesBySystem.get(scr);
                    if (concepts == null) {
                        concepts = new LinkedHashMap<String, ValueSet.ValueSetExpansionContainsComponent>();
                        codesBySystem.put(scr, concepts);
                    }
                    if (!concepts.containsKey(contains.getCode())) {
                        concepts.put(contains.getCode(), contains);
                    }
                }
            }

            // Validate that we're dealing with a complete expansion
            if (count < total) {
                throw new IllegalArgumentException("Compose cannot be inferred from a partial expansion");
            }

            // Add a compose include for each system and version
            ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
            valueSet.setCompose(compose);
            for (Map.Entry<String, Map<String, ValueSet.ValueSetExpansionContainsComponent>> conceptsEntry : codesBySystem.entrySet()) {
                ValueSet.ConceptSetComponent csc = compose.addInclude();
                String systemUri = getSystemUri(conceptsEntry.getKey());
                String version = getVersion(conceptsEntry.getKey());
                csc.setSystem(systemUri);
                if (version != null) {
                    csc.setVersion(version);
                }

                for (ValueSet.ValueSetExpansionContainsComponent c : conceptsEntry.getValue().values()) {
                    csc.addConcept().setCode(c.getCode()).setDisplay(c.getDisplay());
                }
            }

            // Mark CPG Profiles and knowledge capabilities
            if (declareCPGProfiles) {
                if (!valueSet.getMeta().hasProfile("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-computablevalueset")) {
                    valueSet.getMeta().addProfile("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-computablevalueset");
                }
                ensureKnowledgeCapability(valueSet,"computable");
                ensureKnowledgeRepresentationLevel(valueSet,"structured");
            }

            // Add a warning that the compose element was inferred from the expansion
            boolean hasCaution = false;
            for (Extension e : valueSet.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/valueset-warning")) {
                if (e.getValue() instanceof MarkdownType && ((MarkdownType)e.getValue()).hasValue() && ((MarkdownType)e.getValue()).getValue().equals(USAGE_WARNING)) {
                    hasCaution = true;
                    break;
                }
            }
            if (!hasCaution) {
                valueSet.addExtension("http://hl7.org/fhir/StructureDefinition/valueset-warning", new MarkdownType().setValue(USAGE_WARNING));
            }

            return true;
        }

        return false;
    }

    public boolean hasKnowledgeCapability(DomainResource resource, String capability) {
        for (Extension e : resource.getExtension()) {
            if ("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability".equals(e.getUrl())) {
                if (capability.equals(e.getValue().primitiveValue())) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasKnowledgeRepresentationLevel(DomainResource resource, String level) {
        for (Extension e : resource.getExtension()) {
            if ("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeRepresentationLevel".equals(e.getUrl())) {
                if (level.equals(e.getValue().primitiveValue())) {
                    return true;
                }
            }
        }

        return false;
    }

    public void ensureKnowledgeCapability(DomainResource resource, String capability) {
        if (!hasKnowledgeCapability(resource, capability)) {
            resource.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability").setValue(new StringType(capability));
        }
    }

    public void ensureKnowledgeRepresentationLevel(DomainResource resource, String level) {
        if (!hasKnowledgeRepresentationLevel(resource, level)) {
            resource.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeRepresentationLevel").setValue(new StringType(level));
        }
    }

    public boolean hasSimpleCompose(ValueSet valueSet) {
        if (valueSet.hasCompose()) {
            if (valueSet.getCompose().hasExclude()) {
                return false;
            }
            for (ValueSet.ConceptSetComponent csc : valueSet.getCompose().getInclude()) {
                if (csc.hasValueSet()) {
                    // Cannot expand a compose that references a value set
                    return false;
                }

                if (!csc.hasSystem()) {
                    // Cannot expand a compose that does not have a system
                    return false;
                }

                if (csc.hasFilter()) {
                    // Cannot expand a compose that has a filter
                    return false;
                }

                if (!csc.hasConcept()) {
                    // Cannot expand a compose that does not enumerate concepts
                    return false;
                }
            }

            // If all includes are simple, the compose can be expanded
            return true;
        }

        return false;
    }
}
