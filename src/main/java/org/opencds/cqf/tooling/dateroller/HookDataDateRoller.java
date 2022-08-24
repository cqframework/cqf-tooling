package org.opencds.cqf.tooling.dateroller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HookDataDateRoller {
    private FhirContext fhirContext;
    private IOUtils.Encoding fileEncoding;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private IParser resourceParser;

    public HookDataDateRoller(FhirContext FHIRContext, IOUtils.Encoding encoding) {
        fhirContext = FHIRContext;
        fileEncoding = encoding;
        resourceParser = DataDateRollerUtils.getParser(fileEncoding, fhirContext);
    }

    public JsonObject rollJSONHookDates(JsonObject hook) {
        if (!hook.get("context").isJsonNull()) {
            JsonObject context = hook.getAsJsonObject("context");
            this.rollContextDates(context);
            hook.remove("context");
            hook.add("context", context);
        }
        if (!hook.get("prefetch").isJsonNull()) {
            JsonObject prefetch = hook.getAsJsonObject("prefetch");
            this.rollPrefetchItemsDates(prefetch);
            hook.remove("prefetch");
            hook.add("prefetch", prefetch);
        }
        return hook;
    }

    public void rollContextDates(JsonObject context) {
        if (!context.get("draftOrders").isJsonNull()) {
            JsonObject draftOrders = context.getAsJsonObject("draftOrders");
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
            context.remove("draftOrders");
            JsonElement newDraftOrders = JsonParser.parseString(resourceParser.setPrettyPrint(true).encodeResourceToString(resource));
            draftOrders = newDraftOrders.getAsJsonObject();
            context.add("draftOrders", draftOrders);
        }
    }

    public void rollPrefetchItemsDates(JsonObject prefetch) {
        JsonObject item;
        for (int i = 1; i <= prefetch.size(); i++) {
            if (!prefetch.get("item" + i).isJsonNull()) {
                item = prefetch.getAsJsonObject("item" + i);
            }else{
                continue;
            }
            IBaseResource resource = resourceParser.parseResource(item.getAsJsonObject("resource").toString());
            if (null == resource) {
                logger.info("This hook did not contain prefetch items");
                continue;
            }
            if (resource.fhirType().equalsIgnoreCase("bundle")) {
                ResourceDataDateRoller.rollBundleDates(fhirContext, resource);
            } else {
                ResourceDataDateRoller.rollResourceDates(fhirContext, resource);
            }
            JsonObject response = item.getAsJsonObject("response");
            item.add("response", response);
            addUpdatedJsonObject(resource, item, "resource");
//            addUpdatedJsonObject(resource, prefetch, "item" + i);
       }
    }

    public void addUpdatedJsonObject(IBaseResource resource, JsonObject objectToAddTo, String objectName){
        JsonObject objectToAdd;
        objectToAddTo.remove(objectName);
        JsonElement newItem = JsonParser.parseString(resourceParser.setPrettyPrint(true).encodeResourceToString(resource));
        objectToAdd = newItem.getAsJsonObject();
        objectToAddTo.add(objectName, objectToAdd);

    }
}
