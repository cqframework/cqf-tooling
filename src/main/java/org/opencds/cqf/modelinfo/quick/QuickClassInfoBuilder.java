package org.opencds.cqf.modelinfo.quick;


import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.dstu3.model.StructureDefinition.TypeDerivationRule;
import org.opencds.cqf.modelinfo.ClassInfoBuilder;
import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.ClassInfoElement;
import org.hl7.elm_modelinfo.r1.ListTypeSpecifier;
import org.hl7.elm_modelinfo.r1.TypeInfo;


public class QuickClassInfoBuilder extends ClassInfoBuilder {

    public QuickClassInfoBuilder(Map<String, StructureDefinition> structureDefinitions) {
        super(new QuickClassInfoSettings(), structureDefinitions);
    }

    @Override
    protected void innerBuild() {
        this.settings.useCQLPrimitives = true;
        System.out.println("Building ComplexTypes");
        this.buildFor("QUICK",
            (x -> x.getKind() == StructureDefinitionKind.COMPLEXTYPE && (x.getBaseDefinition() == null
                    || !x.getBaseDefinition().equals("http://hl7.org/fhir/StructureDefinition/Extension"))
                    && !this.settings.cqlTypeMappings.containsKey(this.unQualify(x.getName())))
            );

        this.settings.useCQLPrimitives = false;
        System.out.println("Building Base FHIR Resources");
        this.buildFor("QUICK", 
            (x -> x.getKind() == StructureDefinitionKind.RESOURCE
            && (!x.hasDerivation() || x.getDerivation() == TypeDerivationRule.SPECIALIZATION)
                && x.getUrl().startsWith("http://hl7.org/fhir") && !x.getUrl().startsWith("http://hl7.org/fhir/us/qicore")
                && !x.getUrl().startsWith("http://hl7.org/fhir/us/core"))
        );
        
        this.settings.useCQLPrimitives = true;
        System.out.println("Building Quick Extension Resources");
        this.buildFor("QUICK", 
            (x -> x.getKind() == StructureDefinitionKind.RESOURCE 
                && (!x.hasDerivation() || x.getDerivation() == TypeDerivationRule.CONSTRAINT)
                && (x.getUrl().startsWith("http://hl7.org/fhir/us/qicore") || x.getUrl().startsWith("http://hl7.org/fhir/us/core")))
        );

        List<ClassInfo> typeInfoValuesAsClassInfos = new ArrayList<ClassInfo>();  
        typeInfos.values().forEach(x -> typeInfoValuesAsClassInfos.add((ClassInfo)x));

        for(ClassInfo v1 : typeInfoValuesAsClassInfos)
        {
            ClassInfo filteredClassInfo = (ClassInfo)v1;
            for(TypeInfo v2 : typeInfos.values())
            {
                ClassInfo baseClassInfo = (ClassInfo)v2;
                if(baseClassInfo != null)
                {
                    //Get typespecifiers that are not CQL Primitives from FHIR replace QUICK Versions
                    for (ClassInfoElement element: baseClassInfo.getElement())
                    {
                        ListTypeSpecifier elementTypeSpecifier = (ListTypeSpecifier) element.getTypeSpecifier();
                        if(this.settings.cqlTypeMappings.containsKey(element.getType()) && element.getType().startsWith("QUICK"))
                        {
                            for(ClassInfoElement filteredElement: filteredClassInfo.getElement())
                            {
                                if(filteredElement.getType() != null && element.getType() != null &&
                                this.unQualify(filteredElement.getType()).matches(this.unQualify(element.getType())))
                                {
                                    filteredElement.setType("System." + this.unQualify(element.getType()));
                                }
                            }
                        }
                        else if(elementTypeSpecifier != null && elementTypeSpecifier.getElementType() != null
                        && this.settings.cqlTypeMappings.containsKey(elementTypeSpecifier.getElementType()) && (elementTypeSpecifier.getElementType().startsWith("QUICK")))
                        {
                            for(ClassInfoElement filteredElement: filteredClassInfo.getElement())
                            {
                                ListTypeSpecifier filteredElementTypeSpecifier = (ListTypeSpecifier) filteredElement.getTypeSpecifier();
                                if(filteredElementTypeSpecifier != null && filteredElementTypeSpecifier.getElementType() != null
                                && this.unQualify(filteredElementTypeSpecifier.getElementType()).matches(this.unQualify(elementTypeSpecifier.getElementType())))
                                {
                                    filteredElementTypeSpecifier.setElementType("System." + this.unQualify(elementTypeSpecifier.getElementType()));
                                }
                            }
                        }
                        else if(element.getType() != null && baseClassInfo.getLabel() != null && element.getType().startsWith("QUICK.") 
                        && baseClassInfo.getLabel().startsWith("http://hl7.org/fhir"))
                        {
                            for(ClassInfoElement filteredElement: filteredClassInfo.getElement())
                            {
                                if(filteredElement.getName().matches(element.getName()) && baseClassInfo.getName().matches(filteredClassInfo.getName()))
                                {
                                    filteredElement.setType(element.getType());
                                }
                            }
                        }
                    }
                    //Get BaseType from FHIR replace QUICK
                    if (filteredClassInfo.getName().matches(baseClassInfo.getName()) 
                        && filteredClassInfo.getBaseType() != null && baseClassInfo.getBaseType() != null
                        && !filteredClassInfo.getBaseType().matches(baseClassInfo.getBaseType())
                        && filteredClassInfo.getLabel() != null && baseClassInfo.getLabel() != null
                        && baseClassInfo.getLabel().startsWith("http://hl7.org/fhir"))
                    {
                        filteredClassInfo.setBaseType(baseClassInfo.getBaseType());
                    }
                    //Remove all labels beginning with "http://hl7.org/fhir/StructureDefinition/
                    if (this.settings.primaryCodePath.get(this.unQualify(filteredClassInfo.getName())) == null
                    && filteredClassInfo.getLabel() != null && filteredClassInfo.getLabel().startsWith("http://hl7.org/fhir"))
                    {
                        filteredClassInfo.setLabel(null);
                    }
                }   
            }
            typeInfos.put(filteredClassInfo.getName().substring(0, filteredClassInfo.getName().lastIndexOf(".")) + "." +  filteredClassInfo.getLabel(), filteredClassInfo);
        }

        //Remove Duplicates
        List<ClassInfo> invalidClassInfos = (typeInfoValuesAsClassInfos.stream()
        .filter(x -> this.settings.cqlTypeMappings.containsKey(x.getName()) 
        || (x.getLabel() != null && x.getLabel().startsWith("http://hl7.org/fhir"))).collect(Collectors.toList()));
        invalidClassInfos.forEach(x -> typeInfos.remove( "QUICK." + x.getLabel()));
        typeInfoValuesAsClassInfos.getClass();
    }
}