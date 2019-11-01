package org.opencds.cqf.modelinfo.fhir;

import java.util.Collection;
import java.util.Map;

import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.ClassInfoElement;
import org.hl7.elm_modelinfo.r1.ListTypeSpecifier;
import org.hl7.elm_modelinfo.r1.NamedTypeSpecifier;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.hl7.elm_modelinfo.r1.TypeSpecifier;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r4.model.StructureDefinition.TypeDerivationRule;
import org.opencds.cqf.modelinfo.ClassInfoBuilder;

public class FHIRClassInfoBuilder extends ClassInfoBuilder {

    public FHIRClassInfoBuilder(Map<String, StructureDefinition> structureDefinitions) {
        super(new FHIRClassInfoSettings(), structureDefinitions);
    }

    @Override
    protected void innerBuild() {
        System.out.println("Building Primitives");
        this.buildFor("FHIR", (x -> x.getKind() == StructureDefinitionKind.PRIMITIVETYPE));

        System.out.println("Building ComplexTypes");
        this.buildFor("FHIR", (x -> x.getKind() == StructureDefinitionKind.COMPLEXTYPE && (x.getBaseDefinition() == null
                || !x.getBaseDefinition().equals("http://hl7.org/fhir/StructureDefinition/Extension"))));

        System.out.println("Building Resources");
        this.buildFor("FHIR", (x -> x.getKind() == StructureDefinitionKind.RESOURCE
                && (!x.hasDerivation() || x.getDerivation() == TypeDerivationRule.SPECIALIZATION)));
    }

    @Override
    protected void afterBuild() {
        //Clean up Content Reference Specifiers
        Collection<TypeInfo> typeInfoValues = this.getTypeInfos().values();
        typeInfoValues.stream().map(x -> (ClassInfo)x).forEach(
            x -> x.getElement().stream()
            .filter(y -> this.hasContentReferenceTypeSpecifier(y))
            .forEach(y -> this.fixupContentReferenceSpecifier("FHIR", y))
        );
    }

    private TypeSpecifier resolveContentReference(String modelName, String path) throws Exception {
        String root = this.getQualifier(this.stripRoot(path, "#"));
        String elementPath = this.unQualify(this.stripRoot(path, "#"));

        TypeInfo rootType = this.resolveType(modelName + "." + root);
        ClassInfoElement element = this.forPath(((ClassInfo) rootType).getElement(), elementPath);
        return this.getTypeSpecifier(element);
    }

    private Boolean isContentReferenceTypeSpecifier(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier) typeSpecifier;
            return nts.getName().startsWith("#");
        } else if (typeSpecifier instanceof ListTypeSpecifier) {
            ListTypeSpecifier lts = (ListTypeSpecifier) typeSpecifier;
            if (lts.getElementType().startsWith("#")) {
                return true;
            } else if (lts.getElementTypeSpecifier() != null) {
                return this.isContentReferenceTypeSpecifier(lts.getElementTypeSpecifier());
            }
        }

        return false;
    }

    private String getContentReference(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier) typeSpecifier;
            if (nts.getName().startsWith("#")) {
                return nts.getName();
            }
        } else if (typeSpecifier instanceof ListTypeSpecifier) {
            ListTypeSpecifier lts = (ListTypeSpecifier) typeSpecifier;
            if (lts.getElementType().startsWith("#")) {
                return lts.getElementType();
            } else if (lts.getElementTypeSpecifier() != null) {
                return this.getContentReference(lts.getElementTypeSpecifier());
            }
        }

        return null;
    }

    protected Boolean hasContentReferenceTypeSpecifier(ClassInfoElement element) {

        return element.getElementType() != null && element.getElementType().startsWith("#")
                || this.isContentReferenceTypeSpecifier(element.getElementTypeSpecifier());
    }

    protected ClassInfoElement fixupContentReferenceSpecifier(String modelName, ClassInfoElement element) {
        ClassInfoElement result = null;
        try {
            if (this.hasContentReferenceTypeSpecifier(element)) {
                result = element;
                if (element.getElementType() != null && element.getElementType().startsWith("#")) {
                    element.setElementType(this.getTypeName(
                            (NamedTypeSpecifier) this.resolveContentReference(modelName, element.getElementType())));
                } else if (element.getElementTypeSpecifier() != null
                        && element.getElementTypeSpecifier() instanceof ListTypeSpecifier) {
                    ListTypeSpecifier lts = new ListTypeSpecifier();
                    lts.setElementTypeSpecifier(this.resolveContentReference(modelName,
                            this.getContentReference(element.getElementTypeSpecifier())));

                    element.setElementTypeSpecifier(lts);
                } else if (element.getElementTypeSpecifier() != null) {
                    element.setElementTypeSpecifier(this.resolveContentReference(modelName,
                            this.getContentReference(element.getElementTypeSpecifier())));
                } else
                    return element;
            }
        } catch (Exception e) {
            System.out.println("Error fixing up contentreferencetypespecifier: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}