package org.opencds.cqf.modelinfo;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.ModelSpecifier;
import org.hl7.elm_modelinfo.r1.TypeInfo;

public class ModelInfoBuilder {

    protected Collection<TypeInfo> typeInfos;
    protected ModelInfoSettings settings;

    protected ModelInfoBuilder(Collection<TypeInfo> typeInfos) {
        this.typeInfos = typeInfos;
    }

    public ModelInfo build()
    {
        this.beforeBuild();
        ModelInfo mi = this.innerBuild();
        return this.afterBuild(mi);
    }

    protected ModelInfo innerBuild() {
        Collection<TypeInfo> modelTypeInfos = this.typeInfos.stream()
        .map(x -> ((ClassInfo)x))
        .sorted(Comparator.comparing(ClassInfo::getName))
        .collect(Collectors.toList());

        ModelInfo mi = new ModelInfo().withRequiredModelInfo(new ModelSpecifier().withName("System").withVersion("1.0.0"))
            .withTypeInfo(modelTypeInfos)
            .withConversionInfo(this.settings.conversionInfos)
            .withName(this.settings.name)
            .withVersion(this.settings.version)
            .withUrl(this.settings.url)
            .withPatientClassName(this.settings.patientClassName)
            .withPatientBirthDatePropertyName(this.settings.patientBirthDatePropertyName)
            .withTargetQualifier(new QName(this.settings.targetQualifier));

        return mi;
    }


    protected String unQualify(String name) {
        int index = name.indexOf(".");
        if (index > 0) {
            return name.substring(index + 1);
        }

        return null;
    }

    // Apply any pre-build fixups to TypeInfos here
    protected void beforeBuild() {};

    // Apply any post-build fixups to ModelInfo here
    protected ModelInfo afterBuild(ModelInfo mi) {
        return mi;
    };
}