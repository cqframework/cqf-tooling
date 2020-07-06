package org.opencds.cqf.modelinfo.qicore;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.opencds.cqf.modelinfo.Atlas;
import org.opencds.cqf.modelinfo.ContextInfoBuilder;
import org.opencds.cqf.modelinfo.ModelInfoBuilder;

public class QICoreModelInfoBuilder extends ModelInfoBuilder {
    private String helpersPath;
    private ContextInfoBuilder contextInfoBuilder;

    public QICoreModelInfoBuilder(String version, Map<String, TypeInfo> typeInfos, Atlas atlas, String helpersPath) {
        super(typeInfos.values());
        this.settings = new QICoreModelInfoSettings(version);
        this.helpersPath = helpersPath;
        this.contextInfoBuilder = new ContextInfoBuilder(settings, atlas, typeInfos);
    }

    @Override
    protected void beforeBuild() {
        // TODO: File naming?
        try {
            PrintWriter pw = new PrintWriter(this.helpersPath);
            pw.println(String.format("library QICoreHelpers version '%s'\n", this.settings.version) +
                    "\n" +
                    "using FHIR version '4.0.1'\n" +
                    "\n" +
                    "define function ToInterval(period FHIR.Period):\n" +
                    "    if period is null then\n" +
                    "        null\n" +
                    "    else\n" +
                    "        Interval[period.\"start\".value, period.\"end\".value]\n" +
                    "\n" +
                    "define function ToQuantity(quantity FHIR.Quantity):\n" +
                    "    if quantity is null then\n" +
                    "        null\n" +
                    "    else\n" +
                    "        System.Quantity { value: quantity.value.value, unit: quantity.unit.value }\n" +
                    "\n" +
                    "define function ToRatio(ratio FHIR.Ratio):\n" +
                    "    if ratio is null then\n" +
                    "        null\n" +
                    "    else\n" +
                    "        System.Ratio { numerator: ToQuantity(ratio.numerator), denominator: ToQuantity(ratio.denominator) }\n" +
                    "\n" +
                    "define function ToInterval(range FHIR.Range):\n" +
                    "    if range is null then\n" +
                    "        null\n" +
                    "    else\n" +
                    "        Interval[ToQuantity(range.low), ToQuantity(range.high)]\n" +
                    "\n" +
                    "define function ToCode(coding FHIR.Coding):\n" +
                    "    if coding is null then\n" +
                    "        null\n" +
                    "    else\n" +
                    "        System.Code {\n" +
                    "          code: coding.code.value,\n" +
                    "          system: coding.system.value,\n" +
                    "          version: coding.version.value,\n" +
                    "          display: coding.display.value\n" +
                    "        }\n" +
                    "\n" +
                    "define function ToConcept(concept FHIR.CodeableConcept):\n" +
                    "    if concept is null then\n" +
                    "        null\n" +
                    "    else\n" +
                    "        System.Concept {\n" +
                    "            codes: concept.coding C return ToCode(C),\n" +
                    "            display: concept.text.value\n" +
                    "        }\n" +
                    "\n");
            pw.close();
        }
        catch (Exception e) {
            System.out.println("Unable to write QICoreHelpers");
        }
    }

    @Override
    protected ModelInfo afterBuild(ModelInfo mi) {
        mi.withContextInfo(this.contextInfoBuilder.build().values());
        // Apply fixups
        return mi;
    };
}