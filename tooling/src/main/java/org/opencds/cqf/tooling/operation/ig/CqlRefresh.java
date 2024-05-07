package org.opencds.cqf.tooling.operation.ig;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.processor.argument.RefreshIGArgumentProcessor;

import java.util.List;

public class CqlRefresh extends Refresh {

    public CqlRefresh(IGInfo igInfo) {
        super(igInfo);
    }

    @Override
    public List<IBaseResource> refresh() {
        return List.of();
    }

    public void refreshCql(IGInfo igInfo, RefreshIGArgumentProcessor params) {

    }

}
