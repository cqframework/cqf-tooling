package org.opencds.cqf.tooling.terminology;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.File;
import java.time.Instant;
import java.util.Date;
/**
 * @author Adam Stevenson
 */
public class EnsureExecutableValueSetOperation extends Operation {
    private String valueSetPath;
    private String encoding = IOUtils.Encoding.JSON.toString();
    private boolean declareCPGProfiles = true;
    private FhirContext fhirContext;

    public FhirContext getFhirContext() {
        if (fhirContext == null) {
            fhirContext = FhirContext.forR4();
        }

        return fhirContext;
    }

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output"); // default

        for (String arg : args) {
            if (arg.equals("-EnsureExecutableValueSet")) continue;
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
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
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
                    if (refreshExpansion(valueSet)) {
                        IOUtils.writeResource(valueSet, file.getAbsolutePath(), IOUtils.Encoding.parse(encoding), getFhirContext());
                    }
                }
            }
        }
    }

    public boolean refreshExpansion(ValueSet valueSet) {
        if (hasSimpleCompose(valueSet)) {
            ValueSet.ValueSetExpansionComponent expansion = new ValueSet.ValueSetExpansionComponent();
            expansion.setTimestamp(Date.from(Instant.now()));
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
