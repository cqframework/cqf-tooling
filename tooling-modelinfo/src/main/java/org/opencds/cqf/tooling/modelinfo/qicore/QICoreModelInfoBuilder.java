package org.opencds.cqf.tooling.modelinfo.qicore;

import java.io.PrintWriter;
import java.util.Map;

import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.opencds.cqf.tooling.modelinfo.Atlas;
import org.opencds.cqf.tooling.modelinfo.ContextInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.ModelInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QICoreModelInfoBuilder extends ModelInfoBuilder {
    private static final Logger logger = LoggerFactory.getLogger(QICoreModelInfoBuilder.class);
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
                    "define function ToInterval(period FHIR.Period):\n"+
                    "    if period is null then\n"+
                    "        null\n"+
                    "    else\n"+
                    "        if period.\"start\" is null then\n"+
                    "            Interval(period.\"start\".value, period.\"end\".value]\n"+
                    "        else\n"+
                    "            Interval[period.\"start\".value, period.\"end\".value]\n"+
                    "\n"+
                    "define function ToCalendarUnit(unit System.String):\n" +
                    "    case unit\n"+
                    "        when \'ms\' then \'millisecond\'\n"+
                    "        when \'s\' then \'second\'\n"+
                    "        when \'min\' then \'minute\'\n"+
                    "        when \'h\' then \'hour\'\n"+
                    "        when \'d\' then \'day\'\n"+
                    "        when \'wk\' then \'week\'\n"+
                    "        when \'mo\' then \'month\'\n"+
                    "        when \'a\' then \'year\'\n"+
                    "        else unit\n"+
                    "    end\n"+
                    "\n"+
                    "define function ToQuantity(quantity FHIR.Quantity):\n"+
                    "    case\n"+
                    "        when quantity is null then null\n"+
                    "        when quantity.value is null then null\n"+
                    "        when quantity.comparator is not null then\n"+
                    "            Message(null, true, \'FHIRHelpers.ToQuantity.ComparatorQuantityNotSupported\', \'Error\', \'FHIR Quantity value has a comparator and cannot be converted to a System.Quantity value.\')\n"+
                    "        when quantity.system is null or quantity.system.value = \'http://unitsofmeasure.org\'\n"+
                    "              or quantity.system.value = \'http://hl7.org/fhirpath/CodeSystem/calendar-units\' then\n"+
                    "            System.Quantity { value: quantity.value.value, unit: ToCalendarUnit(Coalesce(quantity.code.value, quantity.unit.value, \'1\')) }\n"+
                    "        else\n"+
                    "            Message(null, true, \'FHIRHelpers.ToQuantity.InvalidFHIRQuantity\', \'Error\', \'Invalid FHIR Quantity code: \' & quantity.unit.value & \' (\' & quantity.system.value & \'|\' & quantity.code.value & \')\')\n"+
                    "    end\n"+
                    "\n"+
                    "define function ToQuantityIgnoringComparator(quantity FHIR.Quantity):\n"+
                    "    case\n"+
                    "        when quantity is null then null\n"+
                    "        when quantity.value is null then null\n"+
                    "        when quantity.system is null or quantity.system.value = \'http://unitsofmeasure.org\'\n"+
                    "              or quantity.system.value = \'http://hl7.org/fhirpath/CodeSystem/calendar-units\' then\n"+
                    "            System.Quantity { value: quantity.value.value, unit: ToCalendarUnit(Coalesce(quantity.code.value, quantity.unit.value, \'1\')) }\n"+
                    "        else\n"+
                    "            Message(null, true, \'FHIRHelpers.ToQuantity.InvalidFHIRQuantity\', \'Error\', \'Invalid FHIR Quantity code: \' & quantity.unit.value & \' (\' & quantity.system.value & \'|\' & quantity.code.value & \')\')\n"+
                    "    end\n"+
                    "\n"+
                    "define function ToInterval(quantity FHIR.Quantity):\n"+
                    "    if quantity is null then null else\n"+
                    "        case quantity.comparator.value\n"+
                    "            when \'<\' then\n"+
                    "                Interval[\n"+
                    "                    null,\n"+
                    "                    ToQuantityIgnoringComparator(quantity)\n"+
                    "                )\n"+
                    "            when \'<=\' then\n"+
                    "                Interval[\n"+
                    "                    null,\n"+
                    "                    ToQuantityIgnoringComparator(quantity)\n"+
                    "                ]\n"+
                    "            when \'>=\' then\n"+
                    "                Interval[\n"+
                    "                    ToQuantityIgnoringComparator(quantity),\n"+
                    "                    null\n"+
                    "                ]\n"+
                    "            when \'>\' then\n"+
                    "                Interval(\n"+
                    "                    ToQuantityIgnoringComparator(quantity),\n"+
                    "                    null\n"+
                    "                ]\n"+
                    "            else\n"+
                    "                Interval[ToQuantity(quantity), ToQuantity(quantity)]\n"+
                    "        end\n"+
                    "\n"+
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
            logger.error("Unable to write QICoreHelpers");
        }
    }

    @Override
    protected ModelInfo afterBuild(ModelInfo mi) {
        mi.withContextInfo(this.contextInfoBuilder.build().values());
        // Apply fixups
        return mi;
    };
}