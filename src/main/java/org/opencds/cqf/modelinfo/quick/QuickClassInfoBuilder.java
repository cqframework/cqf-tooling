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

        for(ClassInfo updatedClassInfo : typeInfoValuesAsClassInfos)
        {
            for(TypeInfo originalTypeInfo : typeInfos.values())
            {
                ClassInfo originalClassInfo = (ClassInfo)originalTypeInfo;
                if(originalClassInfo != null)
                {
                    
                    for (ClassInfoElement originalElement: originalClassInfo.getElement())
                    {
                        //update CQL Primitives from FHIR to Quick Versions i.e. System
                        ListTypeSpecifier originalElementTypeSpecifier = (ListTypeSpecifier) originalElement.getTypeSpecifier();
                        if(this.settings.cqlTypeMappings.containsKey(originalElement.getType()) && originalElement.getType().startsWith("QUICK"))
                        {
                            for(ClassInfoElement updatedElement: updatedClassInfo.getElement())
                            {
                                if(updatedElement.getType() != null && originalElement.getType() != null &&
                                this.unQualify(updatedElement.getType()).matches(this.unQualify(originalElement.getType())))
                                {
                                    updatedElement.setType("System." + this.unQualify(originalElement.getType()));
                                }
                            }
                        }
                        //update CQL Primitives from FHIR to Quick Versions i.e. System
                        else if(originalElementTypeSpecifier != null && originalElementTypeSpecifier.getElementType() != null
                        && this.settings.cqlTypeMappings.containsKey(originalElementTypeSpecifier.getElementType()) 
                        && (originalElementTypeSpecifier.getElementType().startsWith("QUICK")))
                        {
                            for(ClassInfoElement updatedElement: updatedClassInfo.getElement())
                            {
                                ListTypeSpecifier updatedElementTypeSpecifier = (ListTypeSpecifier) updatedElement.getTypeSpecifier();
                                if(updatedElementTypeSpecifier != null && updatedElementTypeSpecifier.getElementType() != null
                                && this.unQualify(updatedElementTypeSpecifier.getElementType()).matches(this.unQualify(originalElementTypeSpecifier.getElementType())))
                                {
                                    updatedElementTypeSpecifier.setElementType("System." + this.unQualify(originalElementTypeSpecifier.getElementType()));
                                }
                            }
                        }
                        //Get typespecifiers that are not CQL Primitives from FHIR replace QUICK Versions
                        else if(originalElement.getType() != null && originalClassInfo.getLabel() != null 
                        && originalElement.getType().startsWith("QUICK.") 
                        && originalClassInfo.getLabel().startsWith("http://hl7.org/fhir"))
                        {
                            for(ClassInfoElement updatedElement: updatedClassInfo.getElement())
                            {
                                if(updatedElement.getName().matches(originalElement.getName()) && originalClassInfo.getName().matches(updatedClassInfo.getName()))
                                {
                                    updatedElement.setType(originalElement.getType());
                                }
                            }
                        }
                        else if(originalElementTypeSpecifier != null && originalClassInfo.getLabel() != null 
                        && originalElementTypeSpecifier.getElementType() != null
                        && (originalElementTypeSpecifier.getElementType().startsWith("QUICK")) 
                        && originalClassInfo.getLabel().startsWith("http://hl7.org/fhir"))
                        {
                            for(ClassInfoElement updatedElement: updatedClassInfo.getElement())
                            {
                                ListTypeSpecifier updatedElementTypeSpecifier = (ListTypeSpecifier) updatedElement.getTypeSpecifier();
                                if(updatedElementTypeSpecifier != null && updatedElement.getName().matches(originalElement.getName())
                                && originalClassInfo.getName().matches(updatedClassInfo.getName()))
                                {
                                    updatedElementTypeSpecifier.setElementType(originalElementTypeSpecifier.getElementType());
                                }
                            }
                        }
                    }
                    //Get BaseType from FHIR replace QUICK
                    if (updatedClassInfo.getName().matches(originalClassInfo.getName()) 
                        && updatedClassInfo.getBaseType() != null && originalClassInfo.getBaseType() != null
                        && !updatedClassInfo.getBaseType().matches(originalClassInfo.getBaseType())
                        && updatedClassInfo.getLabel() != null && originalClassInfo.getLabel() != null
                        && originalClassInfo.getLabel().startsWith("http://hl7.org/fhir"))
                    {
                        updatedClassInfo.setBaseType(originalClassInfo.getBaseType());
                    }
                    //Remove all labels beginning with "http://hl7.org/fhir/StructureDefinition/
                    if (this.settings.primaryCodePath.get(this.unQualify(updatedClassInfo.getName())) == null
                    && updatedClassInfo.getLabel() != null && updatedClassInfo.getLabel().startsWith("http://hl7.org/fhir"))
                    {
                        updatedClassInfo.setLabel(null);
                    }
                }   
            }
            typeInfos.put(updatedClassInfo.getName().substring(0, updatedClassInfo.getName().lastIndexOf(".")) + "." +  updatedClassInfo.getLabel(), updatedClassInfo);
        }

        //Remove Duplicates
        List<ClassInfo> invalidClassInfos = (typeInfoValuesAsClassInfos.stream()
        .filter(x -> this.settings.cqlTypeMappings.containsKey(x.getName()) 
        || (x.getLabel() != null && x.getLabel().startsWith("http://hl7.org/fhir"))).collect(Collectors.toList()));
        invalidClassInfos.forEach(x -> typeInfos.remove( "QUICK." + x.getLabel()));
        typeInfoValuesAsClassInfos.getClass();
    }
}