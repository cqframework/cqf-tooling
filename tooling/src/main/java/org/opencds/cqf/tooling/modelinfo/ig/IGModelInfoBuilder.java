package org.opencds.cqf.tooling.modelinfo.ig;

import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.opencds.cqf.tooling.modelinfo.Atlas;
import org.opencds.cqf.tooling.modelinfo.ContextInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.ModelInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Map;

public class IGModelInfoBuilder extends ModelInfoBuilder {
    private static final Logger logger = LoggerFactory.getLogger(IGModelInfoBuilder.class);
    private final ContextInfoBuilder contextInfoBuilder;

    public IGModelInfoBuilder(IGModelInfoSettings settings, Map<String, TypeInfo> typeInfos, Atlas atlas) {
        super(typeInfos.values());
        this.settings = settings;
        this.contextInfoBuilder = new ContextInfoBuilder(settings, atlas, typeInfos);
    }

    @Override
    protected ModelInfo afterBuild(ModelInfo mi) {
        mi.withContextInfo(this.contextInfoBuilder.build().values());
        // Apply fixups
        return mi;
    };
}
