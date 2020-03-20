package org.opencds.cqf.modelinfo;

import org.hl7.elm_modelinfo.r1.*;
import org.hl7.fhir.r4.model.CompartmentDefinition;
import org.hl7.fhir.r4.model.SearchParameter;
import org.hl7.fhir.r4.model.StringType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextInfoBuilder {
    protected Atlas atlas;
    protected Map<String, ContextInfo> contextInfos;
    protected Map<String, TypeInfo> typeInfos;
    protected ModelInfoSettings settings;

    public ContextInfoBuilder(ModelInfoSettings settings, Atlas atlas, Map<String, TypeInfo> typeInfos) {
        this.settings = settings;
        this.atlas = atlas;
        this.typeInfos = typeInfos;
    }

    public Map<String, ContextInfo> build() {
        this.contextInfos = new HashMap<String, ContextInfo>();
        this.innerBuild();
        return this.contextInfos;
    }

    protected ContextInfo buildContextInfo(CompartmentDefinition cd) {
        ContextInfo ci = new ContextInfo();
        ci.setName(cd.getCode().toCode());

        NamedTypeSpecifier nts = new NamedTypeSpecifier();
        nts.setNamespace(this.settings.name);
        if (ci.getName().equals("Patient")) {
            nts.setName(this.settings.patientClassName);
            ci.setBirthDateElement(this.settings.patientBirthDatePropertyName);
        }
        else {
            nts.setName(ci.getName());
        }
        ci.setContextType(nts);
        ci.setKeyElement("id"); // KeyElement for all FHIR Resources is id

        // Do not add compartments for types that cannot be resolved
        if (!typeInfos.containsKey(this.settings.name + "." + ci.getContextType().getName())) {
            return null;
        }

        for (CompartmentDefinition.CompartmentDefinitionResourceComponent r : cd.getResource()) {
            String relatedResourceTypeName = this.settings.name + "." + r.getCode();
            if (typeInfos.containsKey(relatedResourceTypeName)) {
                TypeInfo relatedResourceTypeInfo = typeInfos.get(relatedResourceTypeName);
                if (relatedResourceTypeInfo instanceof ClassInfo) {
                    ClassInfo relatedResourceClassInfo = (ClassInfo)relatedResourceTypeInfo;
                    for (StringType p : r.getParam()) {
                        SearchParameter sp = atlas.resolveSearchParameter(r.getCode(), p.getValue());
                        if (sp != null) {
                            RelationshipInfo relationshipInfo = new RelationshipInfo();
                            relationshipInfo.setContext(ci.getName());
                            List<String> terms = Arrays.asList(sp.getExpression().split("\\."));
                            if (terms.size() >= 1) {
                                relationshipInfo.setRelatedKeyElement(terms.get(terms.size() - 1));
                                relatedResourceClassInfo.getContextRelationship().add(relationshipInfo);
                            }
                        }
                    }
                }
            }
        }

        return ci;
    }

    protected void innerBuild() {
        for (CompartmentDefinition cd : atlas.getCompartmentDefinitions().values()) {
            ContextInfo ci = buildContextInfo(cd);
            if (ci != null) {
                this.contextInfos.put(ci.getName(), ci);
            }
        }
    }
}
