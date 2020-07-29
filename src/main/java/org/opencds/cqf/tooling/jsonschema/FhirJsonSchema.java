package org.opencds.cqf.tooling.jsonschema;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class FhirJsonSchema {

    private JsonObject schema;
    private JsonObject options;
    private JsonObject definitions;
    private JsonObject properties;
    private JsonArray required;
    private JsonArray defaultProperties;

    public FhirJsonSchema(String title) {
        this.schema = new JsonObject();
        this.schema.add("type", new JsonPrimitive("object"));
        this.schema.add("title", new JsonPrimitive(title));

        this.options = new JsonObject();
        this.definitions = new JsonObject();
        this.properties = new JsonObject();
        this.required = new JsonArray();
        this.defaultProperties = new JsonArray();
    }

    public JsonObject getSchema() {
        if (options.entrySet().size() > 0) {
            schema.add("options", options);
        }
        if (definitions.entrySet().size() > 0) {
            schema.add("definitions", definitions);
        }
        if (required.size() > 0) {
            schema.add("required", required);
        }
        if (defaultProperties.size() > 0) {
            schema.add("defaultProperties", defaultProperties);
        }
        schema.add("properties", properties);

        return schema;
    }

    public void addProperty(String name, JsonObject property) {
        this.properties.add(name, property);
    }

    public void addRequired(String name) {
        this.required.add(name);
    }

    public void addDefaultProperty(String name) {
        this.defaultProperties.add(name);
    }
}
