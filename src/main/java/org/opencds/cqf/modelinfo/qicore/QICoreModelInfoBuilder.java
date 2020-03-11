package org.opencds.cqf.modelinfo.qicore;

import java.util.Collection;
import java.util.Map;

import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.opencds.cqf.modelinfo.Atlas;
import org.opencds.cqf.modelinfo.ContextInfoBuilder;
import org.opencds.cqf.modelinfo.ModelInfoBuilder;

public class QICoreModelInfoBuilder extends ModelInfoBuilder {
    private ContextInfoBuilder contextInfoBuilder;

    public QICoreModelInfoBuilder(String version, Map<String, TypeInfo> typeInfos, Atlas atlas) {
        super(typeInfos.values());
        this.settings = new QICoreModelInfoSettings(version);
        this.contextInfoBuilder = new ContextInfoBuilder(settings, atlas, typeInfos);
    }

    @Override
    protected ModelInfo afterBuild(ModelInfo mi) {
        mi.withContextInfo(this.contextInfoBuilder.build().values());
        // Apply fixups
        return mi;
    };
}