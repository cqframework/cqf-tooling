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
        JsonObject context = hook.getAsJsonObject("context");
        if (context != null) {
            this.rollContextDates(context);
        }
        hook.remove("context");
        hook.add("context", context);
        JsonObject prefetch = hook.getAsJsonObject("prefetch");
        if (null != prefetch) {
            this.rollPrefetchItemsDates(prefetch);
        }
        hook.remove("prefetch");
        hook.add("prefetch", prefetch);
        return hook;
    }

    public void rollContextDates(JsonObject context) {
        JsonObject draftOrders = context.getAsJsonObject("draftOrders");
        if (draftOrders != null) {
            IBaseResource resource = resourceParser.parseResource(draftOrders.toString());
            if (null == resource) {
                logger.error("This hook draft orders did not contain a resource");
                return;
            }
            if (resource.fhirType().equalsIgnoreCase("bundle")) {
                ResourceDataDateRoller.rollBundleDates(fhirContext, resource);
            } else {
                ResourceDataDateRoller.rollResourceDates(fhirContext, resource);
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
            String itemName = "item" + i;
            try {
                item = prefetch.getAsJsonObject(itemName);
            } catch (Exception ex) {
                // the following is simply to preserve the original order - the item is present, but null
                prefetch.remove(itemName);
                prefetch.add(itemName, null);
                continue;
            }
            if(null == item){
                continue;
            }
            IBaseResource resource = null;
            String resourceTypeName = "resourceType";
            if(item.getAsJsonPrimitive("resourceType") != null){
                resource = resourceParser.parseResource(item.toString());
            }
            if(resource == null && item.getAsJsonObject("resource").toString()!= null){
                resource = resourceParser.parseResource(item.getAsJsonObject("resource").toString());
                resourceTypeName = "resource";
            }
            if(resource == null){
                logger.info("This hook did not contain prefetch items");
                return;
            }
            if (resource.fhirType().equalsIgnoreCase("bundle")) {
                ResourceDataDateRoller.rollBundleDates(fhirContext, resource);
            } else {
                ResourceDataDateRoller.rollResourceDates(fhirContext, resource);
            }
            JsonObject response = item.getAsJsonObject("response");
            if(response != null){
                item.add("response", response);
            }
            item = addUpdatedJsonObject(resource, item, resourceTypeName);
            prefetch.remove(itemName);
            prefetch.add(itemName, item);
       }
    }

    public JsonObject addUpdatedJsonObject(IBaseResource resource, JsonObject objectToAddTo, String objectName){
        JsonObject objectToAdd;
        JsonElement newItem = JsonParser.parseString(resourceParser.setPrettyPrint(true).encodeResourceToString(resource));
        // handle the new format - no response in items
        if(objectName.equalsIgnoreCase("resourceType")){
            return newItem.getAsJsonObject();
        }
        // handle the old format - a response followed by the resource
        objectToAddTo.remove(objectName);
        objectToAdd = newItem.getAsJsonObject();
        objectToAddTo.add(objectName, objectToAdd);
        return objectToAddTo;
    }
}
