package org.opencds.cqf.tooling.dateroller;

import com.google.gson.JsonObject;

public class HookDataDateRoller {

    public static void rollContextDates(JsonObject context) {
        JsonObject draftOrders = context.getAsJsonObject("draftOrders");
        if (draftOrders != null) {
            String resourceType = draftOrders.get("resourceType").getAsString();
            if(resourceType != null &&resourceType.equalsIgnoreCase("bundle")){
//                    org.hl7.fhir.r4.model.Bundle dratOrderBundle =  BundleUtils.bundleR4Artifacts();
            }
//            JsonArray extension = draftOrders.getAsJsonArray("extension");
//            if (extension != null) {
//                extension.forEach(extensionMember -> {
//                    String url = extensionMember.getAsJsonObject().get("url").getAsString();
//                    if (url != null && url.equalsIgnoreCase("http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller")) {
//                        LocalDate lastUpdated = getLastUpdated(extensionMember.getAsJsonArray());
//                    }
//                });
//            }
        }
    }

    public static void rollPrefetchItemsDates(JsonObject prefetch){
//        prefetch
    }

    public static void rollJSONHookDates(JsonObject hook) {
        JsonObject context = hook.getAsJsonObject("context");
        if (context != null) {
            HookDataDateRoller.rollContextDates(context);
        }
        JsonObject prefetch = hook.getAsJsonObject("prefetch");
        if(null != prefetch){
            HookDataDateRoller.rollPrefetchItemsDates(prefetch);
        }
    }
}
