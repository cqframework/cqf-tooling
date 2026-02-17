package org.opencds.cqf.tooling.operations.bundle;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.testng.annotations.Test;

public class BundleOperationsIT {

    @Test
    void TestBundleOperations() {
        FhirContext context = FhirContext.forR4Cached();
        String bundleId = "test-collection-bundle";
        BundleTypeEnum type = BundleTypeEnum.COLLECTION;
        List<IBaseResource> resourcesToBundle =
                Arrays.asList(new Patient().setId("test-1"), new Patient().setId("test-2"));

        IBaseBundle bundle = BundleResources.bundleResources(context, bundleId, type, resourcesToBundle);
        assertTrue(bundle instanceof Bundle);
        assertEquals(((Bundle) bundle).getId(), bundleId);
        assertEquals(((Bundle) bundle).getType().toCode(), type.getCode());
        assertEquals(((Bundle) bundle).getEntry().size(), 2);
        assertTrue(((Bundle) bundle).getEntryFirstRep().hasResource());
        assertTrue(((Bundle) bundle).getEntryFirstRep().getResource() instanceof Patient);

        List<IBaseResource> resourcesFromBundle = BundleToResources.bundleToResources(context, bundle);
        assertEquals(resourcesFromBundle.size(), 2);
        assertTrue(resourcesFromBundle.get(0) instanceof Patient);

        bundleId = "test-transaction-bundle";
        bundle = BundleResources.bundleResources(context, bundleId, null, resourcesToBundle);
        assertTrue(bundle instanceof Bundle);
        assertEquals(bundleId, ((Bundle) bundle).getId());
        assertEquals(((Bundle) bundle).getType().toCode(), "transaction");
        assertEquals(((Bundle) bundle).getEntry().size(), 2);
        assertTrue(((Bundle) bundle).getEntryFirstRep().hasRequest());
        assertTrue(((Bundle) bundle).getEntryFirstRep().getRequest().hasMethod());
        assertEquals(((Bundle) bundle).getEntryFirstRep().getRequest().getMethod(), Bundle.HTTPVerb.PUT);
        assertTrue(((Bundle) bundle).getEntryFirstRep().hasResource());
        assertTrue(((Bundle) bundle).getEntryFirstRep().getResource() instanceof Patient);

        bundle = BundleResources.bundleResources(context, null, null, Collections.emptyList());
        assertTrue(bundle instanceof Bundle);
        assertTrue(((Bundle) bundle).hasId());
        assertEquals(((Bundle) bundle).getType().toCode(), "transaction");
        assertFalse(((Bundle) bundle).hasEntry());

        resourcesFromBundle = BundleToResources.bundleToResources(context, bundle);
        assertTrue(resourcesFromBundle.isEmpty());
    }
}
