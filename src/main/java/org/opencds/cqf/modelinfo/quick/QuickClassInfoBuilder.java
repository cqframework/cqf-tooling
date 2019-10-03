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

        for(ClassInfo dependantClassInfo : typeInfoValuesAsClassInfos)
        {
            for(TypeInfo independantTypeInfo : typeInfos.values())
            {
                ClassInfo independantClassInfo = (ClassInfo)independantTypeInfo;
                if(independantClassInfo != null)
                {
                    
                    for (ClassInfoElement element: independantClassInfo.getElement())
                    {
                        //update CQL Primitives from FHIR to Quick Versions i.e. System
                        ListTypeSpecifier elementTypeSpecifier = (ListTypeSpecifier) element.getTypeSpecifier();
                        if(this.settings.cqlTypeMappings.containsKey(element.getType()) && element.getType().startsWith("QUICK"))
                        {
                            for(ClassInfoElement filteredElement: dependantClassInfo.getElement())
                            {
                                if(filteredElement.getType() != null && element.getType() != null &&
                                this.unQualify(filteredElement.getType()).matches(this.unQualify(element.getType())))
                                {
                                    filteredElement.setType("System." + this.unQualify(element.getType()));
                                }
                            }
                        }
                        //update CQL Primitives from FHIR to Quick Versions i.e. System
                        else if(elementTypeSpecifier != null && elementTypeSpecifier.getElementType() != null
                        && this.settings.cqlTypeMappings.containsKey(elementTypeSpecifier.getElementType()) && (elementTypeSpecifier.getElementType().startsWith("QUICK")))
                        {
                            for(ClassInfoElement filteredElement: dependantClassInfo.getElement())
                            {
                                ListTypeSpecifier filteredElementTypeSpecifier = (ListTypeSpecifier) filteredElement.getTypeSpecifier();
                                if(filteredElementTypeSpecifier != null && filteredElementTypeSpecifier.getElementType() != null
                                && this.unQualify(filteredElementTypeSpecifier.getElementType()).matches(this.unQualify(elementTypeSpecifier.getElementType())))
                                {
                                    filteredElementTypeSpecifier.setElementType("System." + this.unQualify(elementTypeSpecifier.getElementType()));
                                }
                            }
                        }
                        //Get typespecifiers that are not CQL Primitives from FHIR replace QUICK Versions
                        else if(element.getType() != null && independantClassInfo.getLabel() != null && element.getType().startsWith("QUICK.") 
                        && independantClassInfo.getLabel().startsWith("http://hl7.org/fhir"))
                        {
                            for(ClassInfoElement filteredElement: dependantClassInfo.getElement())
                            {
                                if(filteredElement.getName().matches(element.getName()) && independantClassInfo.getName().matches(dependantClassInfo.getName()))
                                {
                                    filteredElement.setType(element.getType());
                                }
                            }
                        }
                    }
                    //Get BaseType from FHIR replace QUICK
                    if (dependantClassInfo.getName().matches(independantClassInfo.getName()) 
                        && dependantClassInfo.getBaseType() != null && independantClassInfo.getBaseType() != null
                        && !dependantClassInfo.getBaseType().matches(independantClassInfo.getBaseType())
                        && dependantClassInfo.getLabel() != null && independantClassInfo.getLabel() != null
                        && independantClassInfo.getLabel().startsWith("http://hl7.org/fhir"))
                    {
                        dependantClassInfo.setBaseType(independantClassInfo.getBaseType());
                    }
                    //Remove all labels beginning with "http://hl7.org/fhir/StructureDefinition/
                    if (this.settings.primaryCodePath.get(this.unQualify(dependantClassInfo.getName())) == null
                    && dependantClassInfo.getLabel() != null && dependantClassInfo.getLabel().startsWith("http://hl7.org/fhir"))
                    {
                        dependantClassInfo.setLabel(null);
                    }
                }   
            }
            typeInfos.put(dependantClassInfo.getName().substring(0, dependantClassInfo.getName().lastIndexOf(".")) + "." +  dependantClassInfo.getLabel(), dependantClassInfo);
        }

        //Remove Duplicates
        List<ClassInfo> invalidClassInfos = (typeInfoValuesAsClassInfos.stream()
        .filter(x -> this.settings.cqlTypeMappings.containsKey(x.getName()) 
        || (x.getLabel() != null && x.getLabel().startsWith("http://hl7.org/fhir"))).collect(Collectors.toList()));
        invalidClassInfos.forEach(x -> typeInfos.remove( "QUICK." + x.getLabel()));
        typeInfoValuesAsClassInfos.getClass();
    }
}