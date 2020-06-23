package org.opencds.cqf.jsonschema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FhirSchemaElement {

    private String name;
    private String path;
    private int index;
    private int minCard;
    private String maxCard;
    private String description;
    private List<String> types;

    public FhirSchemaElement(String elementName, JsonObject element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot process null schema element");
        }

        this.name = elementName;
        for (Map.Entry<String, JsonElement> entry : element.entrySet()) {
            if (entry.getKey().equals("path")) {
                this.path = entry.getValue().getAsString();
            }
            else if (entry.getKey().equals("index")) {
                this.index = entry.getValue().getAsInt();
            }
            else if (entry.getKey().equals("min")) {
                this.minCard = entry.getValue().getAsInt();
            }
            else if (entry.getKey().equals("max")) {
                this.maxCard = entry.getValue().getAsString();
            }
            else if (entry.getKey().equals("short")) {
                this.description = entry.getValue().getAsString();
            }
            else if (entry.getKey().equals("type")) {
                types = new ArrayList<>();
                for (JsonElement type : entry.getValue().getAsJsonArray()) {
                    types.add(type.getAsJsonObject().get("code").getAsString());
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getIndex() {
        return index;
    }

    public int getMinCard() {
        return minCard;
    }

    public String getMaxCard() {
        return maxCard;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTypes() {
        return types;
    }

    @Override
    public String toString() {
        return String.format(
                "Name: %s ... Path: %s ... Index: %d ... Min: %d ... Max: %s ... Description: %s ... Types: %s",
                name, path, index, minCard, maxCard, description, types.toString()
        );
    }
}
