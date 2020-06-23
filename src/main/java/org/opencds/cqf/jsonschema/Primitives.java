package org.opencds.cqf.jsonschema;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Primitives {

    // This class provides a JSONSchema scaffolding map for FHIR primitive types (http://hl7.org/fhir/STU3/datatypes.html)
    // Map<[FHIR type name] -> [JSONSchema]>

    private static final int INT_MIN = -2147483648;
    private static final int INT_MAX = 2147483647;
    private static final long UNSIGNED_INT_MAX = 429496729l;
    private static final double DECIMAL_MIN = -9999999999999999999999999999.99999999;
    private static final double DECIMAL_MAX = 9999999999999999999999999999.99999999;

    private static JsonObject stu3BooleanSchema() {
        JsonObject ret = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add("null");
        arr.add("bool");
        ret.add("type", arr);
        return ret;
    }

    private static JsonObject stu3NumberSchema() {
        JsonObject ret = new JsonObject();
        ret.add("type", new JsonPrimitive("number"));
        ret.add("default", new JsonPrimitive(""));
        return ret;
    }

    private static JsonObject stu3IntegerSchema() {
        JsonObject ret = stu3NumberSchema();
        ret.add("minimum", new JsonPrimitive(INT_MIN));
        ret.add("maximum", new JsonPrimitive(INT_MAX));
        return ret;
    }

    private static JsonObject stu3PositiveIntegerSchema() {
        JsonObject ret = stu3NumberSchema();
        ret.add("minimum", new JsonPrimitive(1));
        ret.add("maximum", new JsonPrimitive(UNSIGNED_INT_MAX));
        return ret;
    }

    private static JsonObject stu3UnsignedIntegerSchema() {
        JsonObject ret = stu3NumberSchema();
        ret.add("minimum", new JsonPrimitive(0));
        ret.add("maximum", new JsonPrimitive(UNSIGNED_INT_MAX));
        return ret;
    }

    private static JsonObject stu3DecimalSchema() {
        JsonObject ret = stu3NumberSchema();
        ret.add("minimum", new JsonPrimitive(DECIMAL_MIN));
        ret.add("maximum", new JsonPrimitive(DECIMAL_MAX));
        return ret;
    }

    private static JsonObject stu3StringSchema() {
        JsonObject ret = new JsonObject();
        ret.add("type", new JsonPrimitive("string"));
        return ret;
    }

    private static JsonObject stu3UriSchema() {
        JsonObject ret = stu3StringSchema();
        ret.add("format", new JsonPrimitive("url"));
        return ret;
    }

    private static JsonObject stu3IdSchema() {
        JsonObject ret = stu3StringSchema();
        ret.add("pattern", new JsonPrimitive("^[A-Za-z0-9\\-\\.]{1,64}$"));
        return ret;
    }

    private static JsonObject stu3OidSchema() {
        JsonObject ret = stu3StringSchema();
        ret.add("pattern", new JsonPrimitive("^urn:oid:[0-2](\\.[1-9]\\d*)+$"));
        return ret;
    }

    private static JsonObject stu3CodeSchema() {
        JsonObject ret = stu3StringSchema();
        ret.add("pattern", new JsonPrimitive("^[^\\s]+([\\s]?[^\\s]+)*$"));
        return ret;
    }

    private static JsonObject stu3DateSchema() {
        JsonObject ret = stu3StringSchema();
        ret.add("pattern", new JsonPrimitive("^\\d{4}(-\\d{2}(-\\d{2})?)?$"));
        return ret;
    }

    private static JsonObject stu3DateTimeSchema() {
        JsonObject ret = stu3StringSchema();
        ret.add("pattern", new JsonPrimitive("^\\d{4}(-\\d{2}(-\\d{2}(T\\d{2}(:\\d{2}(:\\d{2}(\\.\\d{3}(Z|([+-]\\d{2}:\\d{2})?)?)?)?)?)?)?)?$"));
        return ret;
    }

    private static JsonObject stu3InstantSchema() {
        JsonObject ret = stu3StringSchema();
        ret.add("pattern", new JsonPrimitive("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?(Z|([+-]\\d{2}:\\d{2}))$"));
        return ret;
    }

    private static JsonObject stu3TimeSchema() {
        JsonObject ret = stu3StringSchema();
        ret.add("pattern", new JsonPrimitive("^\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?$"));
        return ret;
    }

    private static JsonObject stu3TextAreaSchema() {
        JsonObject ret = stu3StringSchema();
        ret.add("format", new JsonPrimitive("textarea"));
        return ret;
    }

    public static Map<String, JsonObject> stu3() {
        Map<String, JsonObject> ret = new HashMap<>();
        ret.put("boolean", stu3BooleanSchema());
        ret.put("string", stu3StringSchema());
        ret.put("integer", stu3IntegerSchema());
        ret.put("positiveInt", stu3PositiveIntegerSchema());
        ret.put("unsignedInt", stu3UnsignedIntegerSchema());
        ret.put("decimal", stu3DecimalSchema());
        ret.put("uri", stu3UriSchema());
        ret.put("id", stu3IdSchema());
        ret.put("oid", stu3OidSchema());
        ret.put("code", stu3CodeSchema());
        ret.put("date", stu3DateSchema());
        ret.put("dateTime", stu3DateTimeSchema());
        ret.put("instant", stu3InstantSchema());
        ret.put("time", stu3TimeSchema());
        ret.put("markdown", stu3TextAreaSchema());
        ret.put("base64Binary", stu3TextAreaSchema());
        ret.put("xhtml", stu3TextAreaSchema());

        return ret;
    }
}
