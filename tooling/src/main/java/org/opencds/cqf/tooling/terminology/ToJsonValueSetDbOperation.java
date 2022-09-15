package org.opencds.cqf.tooling.terminology;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class ToJsonValueSetDbOperation extends Operation {
    private String valueSetPath;

    @SuppressWarnings("unused")
    private String encoding = IOUtils.Encoding.JSON.toString();
    private FhirContext fhirContext;

    public FhirContext getFhirContext() {
        if (fhirContext == null) {
            fhirContext = FhirContext.forR4Cached();
        }

        return fhirContext;
    }

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output"); // default

        for (String arg : args) {
            if (arg.equals("-ToJsonValueSetDb")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath": case "op": setOutputPath(value); break; // -outputpath (-op)
                case "valuesetpath": case "path": case "vsp": valueSetPath = value; break; // -valuesetpath (-vsp, -path)
                case "encoding": case "e": encoding = value.toLowerCase(); break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (valueSetPath == null) {
            throw new IllegalArgumentException("The path to the value set directory is required");
        }

        JsonObject valueSetDb = new JsonObject();

        for (File file : new File(valueSetPath).listFiles()) {
            if (file.getName().endsWith(".json") || file.getName().endsWith(".xml")) {
                try {
                    IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), getFhirContext());
                    if (resource instanceof org.hl7.fhir.r4.model.ValueSet) {
                        org.hl7.fhir.r4.model.ValueSet valueSet = (ValueSet)resource;
                        addValueSetToDb(valueSetDb, valueSet);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    // Ignore errors that occur, some files in output directories are json or xml but not FHIR resources...
                }
            }
        }

        try {
            FileWriter fw = new FileWriter(getOutputPath() + "/valueset-db.json");
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(valueSetDb, fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing ValueSet to file: " + e.getMessage());
        }
    }

    private void addValueSetToDb(JsonObject valueSetDb, ValueSet valueSet) {
        valueSetDb.add(valueSet.getUrl(), toValueSetVersionExpansion(valueSet));
    }

    private JsonObject toValueSetVersionExpansion(ValueSet valueSet) {
        JsonObject expansion = new JsonObject();
        expansion.add(valueSet.getVersion(), toValueSetExpansion(valueSet));
        return expansion;
    }

    private JsonArray toValueSetExpansion(ValueSet valueSet) {
        JsonArray result = new JsonArray();
        if (valueSet.hasExpansion()) {
            for (ValueSet.ValueSetExpansionContainsComponent cc : valueSet.getExpansion().getContains()) {
                result.add(toCodeEntry(cc));
            }
        }
        return result;
    }

    private JsonObject toCodeEntry(ValueSet.ValueSetExpansionContainsComponent cc) {
        JsonObject result = new JsonObject();
        if (cc.hasCode()) {
            result.addProperty("code", cc.getCode());
        }
        if (cc.hasSystem()) {
            result.addProperty("system", cc.getSystem());
        }
        if (cc.hasVersion()) {
            result.addProperty("version", cc.getVersion());
        }
        return result;
    }
}
