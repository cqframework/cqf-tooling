package org.opencds.cqf.tooling.dateroller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HookDataDateRoller {
    private final FhirContext fhirContext;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IParser resourceParser;
    private static final String CONTEXT = "context";
    private static final String PREFETCH = "prefetch";
    private static final String DRAFT_ORDERS = "draftOrders";

    public HookDataDateRoller(FhirContext fhirContext, IOUtils.Encoding encoding) {
        this.fhirContext = fhirContext;
        resourceParser = DataDateRollerUtils.getParser(encoding, fhirContext);
    }

    public JsonObject rollJSONHookDates(JsonObject hook) {
        if (hook.has(CONTEXT)) {
            JsonObject context = hook.getAsJsonObject(CONTEXT);
            this.rollContextDates(context);
            hook.remove(CONTEXT);
            hook.add(CONTEXT, context);
        }
        if (hook.has(PREFETCH)) {
            JsonObject prefetch = hook.getAsJsonObject(PREFETCH);
            this.rollPrefetchItemsDates(prefetch);
            hook.remove(PREFETCH);
            hook.add(PREFETCH, prefetch);
        } else {
            logger.info("This hook did not contain prefetch items");
        }
        return hook;
    }

    public void rollContextDates(JsonObject context) {
        if (context.has(DRAFT_ORDERS)) {
            JsonObject draftOrders = context.getAsJsonObject(DRAFT_ORDERS);
            IBaseResource resource = resourceParser.parseResource(draftOrders.toString());
            if (null == resource) {
                logger.error("This hook draft orders did not contain a resource");
                return;
            }
            if (resource.fhirType().equalsIgnoreCase("bundle")) {
                ResourceDataDateRoller.rollBundleDates(fhirContext, resource);
            } else {
                logger.error("Draft orders should contain a bundle.");
                return;
            }
            context.remove(DRAFT_ORDERS);
            JsonElement newDraftOrders = JsonParser.parseString(resourceParser.setPrettyPrint(true).encodeResourceToString(resource));
            draftOrders = newDraftOrders.getAsJsonObject();
            context.add(DRAFT_ORDERS, draftOrders);
        }
    }

    public void rollPrefetchItemsDates(JsonObject prefetch) {
        JsonObject item;
        for (int i = 1; i <= prefetch.size(); i++) {
            String itemNo = "item" + i;
            if (prefetch.has(itemNo)) {
                if (!prefetch.get(itemNo).isJsonNull()) {
                    item = prefetch.getAsJsonObject(itemNo);
                    IBaseResource resource = item.has("resource")
                            ? resourceParser.parseResource(item.getAsJsonObject("resource").toString())
                            : resourceParser.parseResource(item.toString());
                    if (resource != null) {
                        if (resource.fhirType().equalsIgnoreCase("bundle")) {
                            ResourceDataDateRoller.rollBundleDates(fhirContext, resource);
                        } else {
                            ResourceDataDateRoller.rollResourceDates(fhirContext, resource);
                        }
                        if (item.has("response")) {
                            item.add("response", item.getAsJsonObject("response"));
                        }
                        addUpdatedJsonObject(resource, prefetch, itemNo);
                    }
                }
            }
            else {
                logger.warn("Prefetch is missing {} - setting to null", itemNo);
                prefetch.add(itemNo, null);
            }
       }
    }

    public void addUpdatedJsonObject(IBaseResource resource, JsonObject objectToAddTo, String objectName){
        objectToAddTo.add(objectName, JsonParser.parseString(resourceParser.setPrettyPrint(true).encodeResourceToString(resource)));
    }
}
