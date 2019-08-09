package org.opencds.cqf.modelinfo;

import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.hl7.elm_modelinfo.r1.ModelSpecifier;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.opencds.cqf.modelinfo.Configuration.ModelInfoSettings;
import org.hl7.elm_modelinfo.r1.ClassInfo;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

public class ModelInfoBuilder {
    public ModelInfo build(String model, String version, Configuration config, Collection<TypeInfo> typeInfos) {
        Collection<TypeInfo> modelTypeInfos = typeInfos.stream()
            .map(x -> ((ClassInfo)x))
            .collect(Collectors.toList());


        // TODO: Factor this out in a model specific way
        typeInfos.stream().map(x -> (ClassInfo)x)
            .filter(x -> x != null && x.getBaseType() != null && x.getBaseType().equals("FHIR.Element"))
            .filter(x -> x.getElement().size() == 1)
            .map(x -> new ConversionInfo()
            .withFromType(x.getName())
            .withToType(x.getElement().get(0).getType())
            .withFunctionName("FHIRHelpers.To" + Helpers.unQualify(x.getElement().get(0).getType())))
        .forEach(x -> config.modelConversionInfos.add(x));

        // TODO: Handle versions...
        ModelInfoSettings mis = config.modelInfoSettings.get(model);

        ModelInfo mi = new ModelInfo().withRequiredModelInfo(new ModelSpecifier().withName("System").withVersion("1.0.0"))
            .withTypeInfo(modelTypeInfos)
            .withConversionInfo(config.modelConversionInfos)
            .withName(mis.name)
            .withVersion(mis.version)
            .withUrl(mis.url)
            .withPatientClassName(mis.patientClassName)
            .withPatientBirthDatePropertyName(mis.patientBirthDatePropertyName)
            .withTargetQualifier(new QName(mis.targetQualifier));

        return mi;
    }
}