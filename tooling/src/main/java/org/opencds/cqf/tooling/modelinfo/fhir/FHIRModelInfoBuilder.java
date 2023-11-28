package org.opencds.cqf.tooling.modelinfo.fhir;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.opencds.cqf.tooling.modelinfo.Atlas;
import org.opencds.cqf.tooling.modelinfo.ContextInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.ModelInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.SearchInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FHIRModelInfoBuilder extends ModelInfoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(FHIRModelInfoBuilder.class);
    private String fhirHelpersPath;
    private SearchInfoBuilder searchInfoBuilder;
    private ContextInfoBuilder contextInfoBuilder;

    public FHIRModelInfoBuilder(String version, Map<String, TypeInfo> typeInfos, Atlas atlas, String fhirHelpersPath) {
        super(typeInfos.values());
        this.fhirHelpersPath = fhirHelpersPath;
        this.settings = new FHIRModelInfoSettings(version);
        this.searchInfoBuilder = new SearchInfoBuilder(settings, atlas, typeInfos);
        this.contextInfoBuilder = new ContextInfoBuilder(settings, atlas, typeInfos);
    }

    private List<ClassInfo> getFhirElementInfos() {
        return  this.typeInfos.stream().map(x -> (ClassInfo)x)
        .filter(x -> x != null && x.getBaseType() != null && x.getBaseType().equals("FHIR.Element"))
        .filter(x -> x.getElement().size() == 1).collect(Collectors.toList());
    }
    
    @Override
    protected void beforeBuild() {
        List<ClassInfo> fhirElementInfos = this.getFhirElementInfos();
        //ClassInfo sd = fhirElementInfos.stream().map(x -> (ClassInfo)x).filter(x -> x.getName().equals("dateTime")).findFirst().get();
        fhirElementInfos.stream().filter(x -> x.getElement().get(0).getElementType() != null)
        .map(x -> new ConversionInfo()
            .withFromType("FHIR." + x.getName())
            .withToType(x.getElement().get(0).getElementType())
            .withFunctionName("FHIRHelpers.To" + this.unQualify(x.getElement().get(0).getElementType())))
        .forEach(x -> this.settings.conversionInfos.add(x));

        List<String> statements = new ArrayList<>();
        //ClassInfo ci = fhirElementInfos.stream().filter(x -> x.getName().equals("string")).findFirst().get();
        fhirElementInfos.stream().filter(x -> x.getElement().get(0).getElementType() != null)
        .sorted(Comparator.comparing(ClassInfo::getName))
        .forEach(x -> {
            String sourceTypeName = x.getName();
            String targetTypeName = x.getElement().get(0).getElementType();
            String functionName = "To" + this.unQualify(targetTypeName);
            statements.add("define function " + functionName + "(value " + sourceTypeName + "): value.value");
        });

        // TODO: File naming?
        try {
            PrintWriter pw = new PrintWriter(this.fhirHelpersPath);
            pw.println(
                    "/*\n" +
                    "@author: Bryn Rhodes\n" +
                    "@description: This library defines functions to convert between FHIR \n" +
                    " data types and CQL system-defined types, as well as functions to support\n" +
                    " FHIRPath implementation. For more information, the FHIRHelpers wiki page:\n" +
                    " https://github.com/cqframework/clinical_quality_language/wiki/FHIRHelpers\n" +
                    "@allowFluent: true\n" +
                    "*/\n" +
                    String.format("library FHIRHelpers version '%s'\n", this.settings.version) +
                    "\n" +
                    String.format("using FHIR version '%s'\n", this.settings.version) +
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
                    "\n" +
                    "define function ToValueSet(uri: String):\n" +
                    "    if uri is null then\n" +
                    "        null\n" +
                    "    else\n" +
                    "        System.ValueSet {\n" +
                    "            id: uri\n" +
                    "        }\n" +
                    "\n" +
                    "define function reference(reference String):\n" +
                    "    if reference is null then\n" +
                    "        null\n" +
                    "    else\n" +
                    "        Reference { reference: string { value: reference } }\n" +
                    "\n" +
                    "define function ToValue(value Choice<base64Binary,\n" +
                    "        boolean,\n" +
                    "        canonical,\n" +
                    "        code,\n" +
                    "        date,\n" +
                    "        dateTime,\n" +
                    "        decimal,\n" +
                    "        id,\n" +
                    "        instant,\n" +
                    "        integer,\n" +
                    "        markdown,\n" +
                    "        oid,\n" +
                    "        positiveInt,\n" +
                    "        string,\n" +
                    "        time,\n" +
                    "        unsignedInt,\n" +
                    "        uri,\n" +
                    "        url,\n" +
                    "        uuid,\n" +
                    "        Address,\n" +
                    "        Age,\n" +
                    "        Annotation,\n" +
                    "        Attachment,\n" +
                    "        CodeableConcept,\n" +
                    "        Coding,\n" +
                    "        ContactPoint,\n" +
                    "        Count,\n" +
                    "        Distance,\n" +
                    "        Duration,\n" +
                    "        HumanName,\n" +
                    "        Identifier,\n" +
                    "        Money,\n" +
                    "        Period,\n" +
                    "        Quantity,\n" +
                    "        Range,\n" +
                    "        Ratio,\n" +
                    "        Reference,\n" +
                    "        SampledData,\n" +
                    "        Signature,\n" +
                    "        Timing,\n" +
                    "        ContactDetail,\n" +
                    "        Contributor,\n" +
                    "        DataRequirement,\n" +
                    "        Expression,\n" +
                    "        ParameterDefinition,\n" +
                    "        RelatedArtifact,\n" +
                    "        TriggerDefinition,\n" +
                    "        UsageContext,\n" +
                    "        Dosage,\n" +
                    "        Meta>):\n" +
                    "    case\n" +
                    "        when value is base64Binary then (value as base64Binary).value\n" +
                    "        when value is boolean then (value as boolean).value\n" +
                    "        when value is canonical then (value as canonical).value\n" +
                    "        when value is code then (value as code).value\n" +
                    "        when value is date then (value as date).value\n" +
                    "        when value is dateTime then (value as dateTime).value\n" +
                    "        when value is decimal then (value as decimal).value\n" +
                    "        when value is id then (value as id).value\n" +
                    "        when value is instant then (value as instant).value\n" +
                    "        when value is integer then (value as integer).value\n" +
                    "        when value is markdown then (value as markdown).value\n" +
                    "        when value is oid then (value as oid).value\n" +
                    "        when value is positiveInt then (value as positiveInt).value\n" +
                    "        when value is string then (value as string).value\n" +
                    "        when value is time then (value as time).value\n" +
                    "        when value is unsignedInt then (value as unsignedInt).value\n" +
                    "        when value is uri then (value as uri).value\n" +
                    "        when value is url then (value as url).value\n" +
                    "        when value is uuid then (value as uuid).value\n" +
                    "        when value is Age then ToQuantity(value as Age)\n" +
                    "        when value is CodeableConcept then ToConcept(value as CodeableConcept)\n" +
                    "        when value is Coding then ToCode(value as Coding)\n" +
                    "        when value is Count then ToQuantity(value as Count)\n" +
                    "        when value is Distance then ToQuantity(value as Distance)\n" +
                    "        when value is Duration then ToQuantity(value as Duration)\n" +
                    "        when value is Quantity then ToQuantity(value as Quantity)\n" +
                    "        when value is Range then ToInterval(value as Range)\n" +
                    "        when value is Period then ToInterval(value as Period)\n" +
                    "        when value is Ratio then ToRatio(value as Ratio)\n" +
                    "        else value as Choice<Address,\n" +
                    "            Annotation,\n" +
                    "            Attachment,\n" +
                    "            ContactPoint,\n" +
                    "            HumanName,\n" +
                    "            Identifier,\n" +
                    "            Money,\n" +
                    "            Reference,\n" +
                    "            SampledData,\n" +
                    "            Signature,\n" +
                    "            Timing,\n" +
                    "            ContactDetail,\n" +
                    "            Contributor,\n" +
                    "            DataRequirement,\n" +
                    "            Expression,\n" +
                    "            ParameterDefinition,\n" +
                    "            RelatedArtifact,\n" +
                    "            TriggerDefinition,\n" +
                    "            UsageContext,\n" +
                    "            Dosage,\n" +
                    "            Meta>\n" +
                    "    end\n" +
                    "\n" +
                    "define function resolve(reference String) returns Resource: external\n" +
                    "define function resolve(reference Reference) returns Resource: external\n" +
                    "define function reference(resource Resource) returns Reference: external\n" +
                    "define function extension(element Element, url String) returns List<Element>: external\n" +
                    "define function extension(resource Resource, url String) returns List<Element>: external\n" +
                    "define function hasValue(element Element) returns Boolean: external\n" +
                    "define function getValue(element Element) returns Any: external\n" +
                    "define function ofType(identifier String) returns List<Any>: external\n" +
                    "define function is(identifier String) returns Boolean: external\n" +
                    "define function as(identifier String) returns Any: external\n" +
                    "define function elementDefinition(element Element) returns ElementDefinition: external\n" +
                    "define function slice(element Element, url String, name String) returns List<Element>: external\n" +
                    "define function checkModifiers(resource Resource) returns Resource: external\n" +
                    "define function checkModifiers(resource Resource, modifier String) returns Resource: external\n" +
                    "define function checkModifiers(element Element) returns Element: external\n" +
                    "define function checkModifiers(element Element, modifier String) returns Element: external\n" +
                    "define function conformsTo(resource Resource, structure String) returns Boolean: external\n" +
                    "define function memberOf(code code, valueSet String) returns Boolean: external\n" +
                    "define function memberOf(coding Coding, valueSet String) returns Boolean: external\n" +
                    "define function memberOf(concept CodeableConcept, valueSet String) returns Boolean: external\n" +
                    "define function subsumes(coding Coding, subsumedCoding Coding) returns Boolean: external\n" +
                    "define function subsumes(concept CodeableConcept, subsumedConcept CodeableConcept) returns Boolean: external\n" +
                    "define function subsumedBy(coding Coding, subsumingCoding Coding) returns Boolean: external\n" +
                    "define function subsumedBy(concept CodeableConcept, subsumingConcept CodeableConcept) returns Boolean: external\n" +
                    "define function htmlChecks(element Element) returns Boolean: external\n");
            statements.stream().forEach(x -> pw.println(x));
            pw.close();
        }
        catch (Exception e) {
            logger.error("Unable to write FileHelpers");
        }
    }

    @Override
    protected ModelInfo afterBuild(ModelInfo mi) {
        // Build search infos (attaches SearchInfo to the existing TypeInfos)
        this.searchInfoBuilder.build();
        mi.withContextInfo(this.contextInfoBuilder.build().values());
        // Apply fixups
        return mi;
    };
}