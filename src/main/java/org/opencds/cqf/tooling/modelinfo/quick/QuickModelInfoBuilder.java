package org.opencds.cqf.tooling.modelinfo.quick;

import java.util.Collection;

import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.opencds.cqf.tooling.modelinfo.ModelInfoBuilder;

public class QuickModelInfoBuilder extends ModelInfoBuilder {

    public QuickModelInfoBuilder(String version, Collection<TypeInfo> typeInfos) {
        super(typeInfos);
        this.settings = new QuickModelInfoSettings(version);
    }
}