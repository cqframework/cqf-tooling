package org.opencds.cqf.modelinfo.quick;


import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.dstu3.model.StructureDefinition.TypeDerivationRule;
import org.opencds.cqf.modelinfo.ClassInfoBuilder;
import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.ClassInfoElement;
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
        Set<ClassInfoElement> ClassInfoElements = new HashSet<ClassInfoElement>();
        typeInfoValuesAsClassInfos.forEach(x -> ClassInfoElements.addAll(x.getElement()));

        //Remove all Infos that are in the CQL Type Mappings
        List<ClassInfoElement> invalidElementTypes = (ClassInfoElements.stream()
        .filter(x -> this.settings.cqlTypeMappings.containsKey(x.getType()) && x.getType().startsWith("QUICK") 
            || this.settings.cqlTypeMappings.containsKey(x.getElementType())&& (x.getElementType().startsWith("QUICK")))
        .collect(Collectors.toList()));
        invalidElementTypes.forEach(x -> x.setType("System." + this.unQualify(x.getType())));
        invalidElementTypes.forEach(x -> x.setElementType("System." + this.unQualify(x.getType())));
        invalidElementTypes.forEach(
            x -> typeInfoValuesAsClassInfos.forEach(
                y -> y.getElement().stream()
                .filter(z -> z.getType() != null && this.unQualify(z.getType()).matches(this.unQualify(x.getType())))
                .forEach(z -> z.setType(x.getType()))
                )
            );
            invalidElementTypes.forEach(
                x -> typeInfoValuesAsClassInfos.forEach(
                    y -> y.getElement().stream()
                    .filter(z -> z.getElementType() != null && this.unQualify(z.getElementType()).matches(this.unQualify(x.getElementType())))
                    .forEach(z -> z.setElementType(x.getElementType()))
                    )
                );

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
                                if(element.getType() != null && baseClassInfo.getLabel() != null && element.getType().startsWith("QUICK.") 
                                && baseClassInfo.getLabel().startsWith("http://hl7.org/fhir") && this.settings.cqlTypeMappings.get(element.getName()) == null)
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