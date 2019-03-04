//package org.opencds.cqf.jsonschema;
//
//import com.google.gson.*;
//import org.opencds.cqf.Operation;
//
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class SchemaGenerator extends Operation {
//
//    private JsonObject stu3Profiles;
//    private JsonObject stu3ValueSets;
//    private JsonObject dstu2Profiles;
//    private JsonObject dstu2ValueSets;
//
//    private Map<String, JsonObject> stu3Schemas;
//
//    public SchemaGenerator() {
//        JsonParser parser = new JsonParser();
//
//        JsonObject stu3 = parser.parse(new InputStreamReader(this.getClass().getResourceAsStream("profiles/stu3.json"))).getAsJsonObject();
//        this.stu3Profiles = stu3.getAsJsonObject("profiles");
//        this.stu3ValueSets = stu3.getAsJsonObject("valuesets");
//        this.stu3Schemas = Primitives.stu3();
//
//        JsonObject dstu2 = parser.parse(new InputStreamReader(this.getClass().getResourceAsStream("profiles/dstu2.json"))).getAsJsonObject();
//        this.dstu2Profiles = dstu2.getAsJsonObject("profiles");
//        this.dstu2ValueSets = dstu2.getAsJsonObject("valuesets");
//    }
//
//    @Override
//    public void execute(String[] args) {
//        processStu3Profiles();
//    }
//
//    private JsonObject copySchema(JsonObject toCopy) {
//        Gson gson = new Gson();
//        return gson.fromJson(gson.toJson(toCopy), JsonObject.class).getAsJsonObject();
//    }
//
//    private JsonObject copySchema(String schemaName) {
//        return copySchema(stu3Profiles.get(schemaName).getAsJsonObject());
//    }
//
//    private JsonObject copySchema(String schemaName, String title, String description) {
//        JsonObject copy = copySchema(stu3Profiles.get(schemaName).getAsJsonObject());
//        copy.add("title", new JsonPrimitive(title));
//        copy.add("description", new JsonPrimitive(description));
//        return copy;
//    }
//
//    private JsonObject createTabArraySchema(String title, String description, JsonObject items) {
//        JsonObject arrSchema = new JsonObject();
//        arrSchema.add("type", new JsonPrimitive("array"));
//        arrSchema.add("title", new JsonPrimitive(title));
//        if (description != null) {
//            arrSchema.add("description", new JsonPrimitive(description));
//        }
//        arrSchema.add("format", new JsonPrimitive("tabs"));
//        arrSchema.add("items", items);
//        return arrSchema;
//    }
//
////    private JsonObject createOneOfSchema(String description, List<String> types) {
////        JsonObject oneOfSchema = new JsonObject();
////        oneOfSchema.add("description", new JsonPrimitive(description));
////        JsonArray oneOfArr = new JsonArray();
////        for (String type : types) {
////            if (stu3Schemas.containsKey(type)) {
////                oneOfArr.add(copySchema(type));
////            }
////            else {
////                oneOfArr.add(resolveProfile(type));
////            }
////        }
////
////        return oneOfSchema;
////    }
//
//    private JsonObject createResourceType(String defaultValue) {
//        JsonObject resourceType = new JsonObject();
//        resourceType.add("type", new JsonPrimitive("string"));
//        resourceType.add("readOnly", new JsonPrimitive(true));
//        resourceType.add("default", new JsonPrimitive(defaultValue));
//        return resourceType;
//    }
//
//    private boolean isResource(String name, JsonObject profile) {
//        return profile.get(name).getAsJsonObject().get("type").getAsJsonArray().get(0).getAsJsonObject().get("code").getAsString().equals("DomainResource")
//    }
//
////    private JsonObject resolveProfile(String name) {
////        if (!stu3Profiles.has(name)) {
////            throw new IllegalArgumentException("Invalid profile: " + name);
////        }
////
////        JsonObject profile = stu3Profiles.get(name).getAsJsonObject();
////        FhirSchema fhirSchema = new FhirSchema(name, profile);
////        FhirJsonSchema jsonSchema = new FhirJsonSchema(name);
////        if (isResource(name, profile)) {
////            jsonSchema.addProperty("resourceType", createResourceType(name));
////        }
////
////        for (FhirSchemaElement element : fhirSchema.getElements()) {
////            boolean isArray = element.getMaxCard().equals("*");
////            // case 1: single type
////            if (element.getTypes().size() == 1) {
////                if (stu3Schemas.containsKey(element.getName())) {
////                    if (isArray) {
////                        jsonSchema.addProperty(
////                                element.getName(),
////                                createTabArraySchema(element.getName(), null, stu3Schemas.get(element.getName()))
////                        );
////                    }
////                    else {
////                        jsonSchema.addProperty(
////                                element.getName(),
////                                copySchema(element.getName(), element.getName(), element.getDescription())
////                        );
////                    }
////                }
////                else {
////
////                }
////            }
////            // case 2: multiple types
////            else if (element.getTypes().size() > 1) {
////                // case 2a: oneOf
////            }
////        }
////    }
//
//    private FhirJsonSchema processProfile(FhirSchema schema) {
//        FhirJsonSchema jsonSchema = new FhirJsonSchema(schema.getName());
//
//        for (FhirSchemaElement element : schema.getElements()) {
//            if (element.getTypes().size() > 1) {
//                stu3Schemas.put(element.getName(), createOneOfSchema(element.getDescription(), element.getTypes()));
//            }
//            if (element.getTypes().size() == 1
//                    && !element.getName().contains(".")
//                    && stu3Schemas.containsKey(element.getTypes().get(0)))
//            {
//                if (element.getMinCard() > 0) {
//                    jsonSchema.addRequired(element.getName());
//                    jsonSchema.addDefaultProperty(element.getName());
//                }
//                else if (!element.getTypes().get(0).equals("boolean")) {
//                    jsonSchema.addDefaultProperty(element.getName());
//                }
//
//                JsonObject typeSchema = copySchema(stu3Schemas.get(element.getTypes().get(0)));
//                typeSchema.add("title", new JsonPrimitive(element.getName()));
//                typeSchema.add("description", new JsonPrimitive(element.getDescription()));
//
//                if (element.getMaxCard().equals("*")) {
//                    jsonSchema.addProperty(element.getName(), createTabArraySchema(element.getName(), null, typeSchema));
//                }
//                else {
//                    jsonSchema.addProperty(element.getName(), typeSchema);
//                }
//            }
//        }
//
////        for (Map.Entry<String, JsonElement> profileElement: profile.entrySet()) {
////            if (profileElement.getKey().equals(profileName)) {
////                continue;
////            }
////
////            String title = profileElement.getKey().replace(profileName + ".", "");
////            JsonArray types = profileElement.getValue().getAsJsonObject().get("type").getAsJsonArray();
////
////            if (types.size() > 1) {
////                // handle oneOfMany case
////            }
////            else {
////
////            }
////
////        }
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(gson.toJson(jsonSchema.getSchema()));
//        JsonObject obj = jsonSchema.getSchema();
//        return jsonSchema;
//    }
//
//    private void processStu3Profiles() {
//        List<FhirJsonSchema> stu3JsonSchemas = new ArrayList<>();
//        for (Map.Entry<String, JsonElement> entry : stu3Profiles.entrySet()) {
//            if (stu3Schemas.containsKey(entry.getKey())) {
//                continue;
//            }
//            FhirSchema schema = new FhirSchema(entry.getKey(), entry.getValue().getAsJsonObject());
//            stu3JsonSchemas.add(processProfile(schema));
//        }
//    }
//}
