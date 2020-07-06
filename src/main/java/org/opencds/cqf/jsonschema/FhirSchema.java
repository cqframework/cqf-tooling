package org.opencds.cqf.jsonschema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FhirSchema {

    private String name;
    private List<FhirSchemaElement> elements;

    public FhirSchema(String name, JsonObject elements) {
        this.name = name;
        this.elements = new ArrayList<>();

        if (elements == null) {
            throw new IllegalArgumentException("Cannot process null schema");
        }

        boolean isBackboneElement = false;
        for (Map.Entry<String, JsonElement> entry : elements.entrySet()) {
            if (entry.getKey().equals(name)) {
                continue;
            }
            this.elements.add(new FhirSchemaElement(entry.getKey().replace(name + ".", ""), entry.getValue().getAsJsonObject()));
        }
    }

    public String getName() {
        return name;
    }

    public List<FhirSchemaElement> getElements() {
        return elements;
    }
}
