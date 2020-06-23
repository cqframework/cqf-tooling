package org.opencds.cqf.quick;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.opencds.cqf.Operation;

import ca.uhn.fhir.context.FhirContext;

public class QuickPageGenerator extends Operation {

    // Assuming R4
    private FhirContext context = FhirContext.forR4();
    private String qicoreDirPath;
    private QuickAtlas atlas;

    @Override
    public void execute(String[] args) {
        // some validation and basic setup
        if (args.length < 2) {
            throw new IllegalArgumentException("The path to the QiCore output directory must be provided!");
        }
        qicoreDirPath = args[1];
        if (args.length > 2) {
            setOutputPath(args[2]);
        }
        else {
            // default
            setOutputPath("src/main/resources/org/opencds/cqf/quick/output");
        }

        // resolving and processing definitions for the operation
        try {
            atlas = new QuickAtlas(qicoreDirPath, context);
            // sixth step is to process the profiles
            processQiCoreProfiles();
            // seventh step is to build the html for the complex FHIR types
            processComplexFhirTypes();
            // eighth step is to build the sidebar
            buildSidebar();
            // ninth step is to build the overview page
            buildOverview();
            // tenth step is to build the index
            // TODO
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("QUICK generation failed due to the following error: " + e.getMessage());
        }
    }

    /**
     *
     * @throws IOException
     */
    private void processQiCoreProfiles() throws IOException, FHIRException {
        for (Map.Entry<String, StructureDefinition> entrySet : atlas.getQicoreProfiles().entrySet()) {
            System.out.println("Processing the " + entrySet.getKey() + " profile...");

            // Initialize HTML page
            HtmlBuilder html = new HtmlBuilder(entrySet.getKey(), atlas);

            // store relative URL in QuickAtlas maps
            atlas.getLinkMap().put(entrySet.getKey(), html.getFileName());
            atlas.getProfileMap().put(entrySet.getKey(), html.getFileName());

            try {

                // Each QiCore profile must have a differential and a snapshot with elements
                if (entrySet.getValue().hasDifferential() && entrySet.getValue().getDifferential().hasElement()
                        && entrySet.getValue().hasSnapshot() && entrySet.getValue().getSnapshot().hasElement()) {
                    html.buildHeader(entrySet.getKey());
                    Map<String, ElementDefinition> snapshotMap = resolveSnapshotElements(entrySet.getValue().getSnapshot());
                    Map<String, String> backboneElements = new HashMap<>();
                    Map<String, HtmlBuilder> backboneHtml = new HashMap<>();

                    // Get the profile definition - check the differential first
                    if (entrySet.getValue().getDifferential().getElementFirstRep().hasDefinition()) {
                        html.buildParagraph(entrySet.getValue().getDifferential().getElementFirstRep().getDefinition());
                    } else {
                        html.buildParagraph(entrySet.getValue().getSnapshot().getElementFirstRep().getDefinition());
                    }

                    // Begin building the table of profile elements
                    html.buildLegend().buildTableStart();

                    // Walk-through each element in the differential
                    for (ElementDefinition element : entrySet.getValue().getDifferential().getElement()) {
                        // get the differential ElementDefinition
                        ElementDefinition snapshotElement = snapshotMap.get(element.getPath());
                        // If the differential is null, check to see if the path is for a choice type
                        if (snapshotElement == null) {
                            String path = element.getPath();

                            if (path.endsWith("[x]")) {
                                path = path.replace("[x]", "");
                            }
                            for (Map.Entry<String, ElementDefinition> set : snapshotMap.entrySet()) {
                                if (set.getValue().getPath().startsWith(path)
                                        && atlas.getFhirTypes().containsKey((set.getValue().getPath().replace(path, "")))) {
                                    snapshotElement = set.getValue();
                                }
                            }

                            // If the differential is still null, we will assume the differential element does not exist
                            if (snapshotElement == null) {
                                throw new IllegalArgumentException("Could not resolve snapshot element for path: " + element.getPath());
                            }
                        }

                        // Resolve the required table elements - always preferring use of the differential element
                        boolean mustSupport = element.hasMustSupport() ? element.getMustSupport() : snapshotElement.getMustSupport();
                        // TODO - for some reason all the modifier values for qicore profile differential elements default to false
                        boolean isModifier = snapshotElement.getIsModifier();
                        // Default to false ... will check later
                        boolean qicoreExtension = false;
                        String field = element.hasSliceName() ? element.getSliceName() : element.getPath().replace(entrySet.getKey() + ".", "");
                        if (field.equals(entrySet.getKey()) || field.equals("id") || field.equals("extension")) {
                            continue;
                        }
                        String min = element.hasMin() ? Integer.toString(element.getMin()) : Integer.toString(snapshotElement.getMin());
                        String max = element.hasMax() ? element.getMax() : snapshotElement.getMax();
                        String card = min + ".." + max;

                        String description = element.hasDefinition() ? element.getDefinition() : snapshotElement.getDefinition();
                        String binding = "";
                        if (element.hasBinding() && element.getBinding().hasValueSet()) {
                            binding += HtmlBuilder.buildBinding(
                                    element.getBinding().getValueSet(),
                                    element.getBinding().hasDescription()
                                            ? element.getBinding().getDescription()
                                            : element.getBinding().getValueSet(),
                                    element.getBinding().getStrength().toCode()
                            );
                        } else if (snapshotElement.hasBinding() && snapshotElement.getBinding().hasValueSet()) {
                            binding += HtmlBuilder.buildBinding(
                                    snapshotElement.getBinding().getValueSet(),
                                    snapshotElement.getBinding().hasDescription()
                                            ? snapshotElement.getBinding().getDescription()
                                            : snapshotElement.getBinding().getValueSet(),
                                    snapshotElement.getBinding().getStrength().toCode()
                            );
                        }
                        description = StringEscapeUtils.escapeHtml(description) + binding;

                        String type;
                        if (element.hasType()) {
                            type = resolveType(element);
                        } else if (snapshotElement.hasType()) {
                            type = resolveType(snapshotElement);
                        } else {
                            String[] pathSplit = field.split("\\.");
                            String path = pathSplit[pathSplit.length - 1];
                            if (backboneElements.containsKey(path)) {
                                type = HtmlBuilder.buildLink(atlas.getLinkMap().get(backboneElements.get(path)), path);
                            } else {
                                throw new IllegalArgumentException("Could not resolve type declaration for field " + field + " for the profile " + entrySet.getKey());
                            }
                        }
                        // Backbone elements need their own page
                        if (type.contains("BackboneElement")) {
                            HtmlBuilder backboneHtmlPage = new HtmlBuilder(element.getPath(), atlas)
                                    .buildHeader(element.getPath())
                                    .buildParagraph(description)
                                    .buildLegend()
                                    .buildTableStart();
                            atlas.getLinkMap().put(element.getPath(), backboneHtmlPage.getFileName());
                            backboneElements.put(field, element.getPath());
                            type = type.replace("''", atlas.getLinkMap().get(element.getPath())).replace("BackboneElement", field);
                            backboneHtml.put(field, backboneHtmlPage);
                        }

                        // Check for max cardinality of many - represent as a List
                        if (max.equals("*")) {
                            type = "List&lt;" + type + "&gt;";
                        }

                        // Empty href are removed (this is for compound extensions)
                        if (type.contains("href=''")) {
                            type = type.replace("<a href=''>", "").replace("</a>", "");
                        }
                        // Check for QiCore defined extension
                        if (type.contains("StructureDefinition-qicore")) {
                            qicoreExtension = true;
                        }
                        if (field.contains(".")) {
                            String base = field.substring(0, field.lastIndexOf("."));
                            if (backboneHtml.containsKey(base)) {
                                backboneHtml.get(base)
                                        .buildRow(mustSupport, isModifier, qicoreExtension, field.replace(base + ".", ""), card, type, description);
                            }
                        } else {
                            html.buildRow(mustSupport, isModifier, qicoreExtension, field, card, type, description);
                        }

                        System.out.println(
                                String.format("Field: %s, Card: %s, Type: %s, Description: %s", field, card, type, description)
                        );
                    }
                    html.buildTableEnd();

                    for (Map.Entry<String, HtmlBuilder> backboneEntry : backboneHtml.entrySet()) {
                        writeHtmlFile(backboneEntry.getValue().getFileName(), backboneEntry.getValue().buildTableEnd().build());
                    }
                }
                writeHtmlFile(html.getFileName(), html.build());
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("QUICK generation failed in processQiCoreProfiles due to the following error: " + e.getMessage());
            }
        }
    }

    /**
     *
     * @throws IOException
     */
    private void processComplexFhirTypes() throws IOException {
        for (Map.Entry<String, String> entry : atlas.getComplexMap().entrySet()) {
            StructureDefinition sd = atlas.getFhirTypes().get(entry.getKey());
            if (sd == null || atlas.getPrimitiveMap().containsKey(sd.getType()) || sd.getType().equals("Quantity")) {
                continue;
            }
            HtmlBuilder html = new HtmlBuilder(entry.getKey(), atlas)
                    .buildHeader(entry.getKey())
                    .buildParagraph(sd.getDifferential().getElementFirstRep().getDefinition())
                    .buildLegend()
                    .buildTableStart();
            for (ElementDefinition element : sd.getSnapshot().getElement()) {
                if (element.getPath().equals(sd.getType())) {
                    continue;
                }
                boolean mustSupport = element.getMustSupport();
                boolean isModifier = element.getIsModifier();
                String field = element.getPath().replace(sd.getType() + ".", "");
                if (field.equals("id") || field.equals("extension")) {
                    continue;
                }
                String card = Integer.toString(element.getMin()) + ".." + element.getMax();
                String description = element.getDefinition();
                description = StringEscapeUtils.escapeHtml(description);
                String type = resolveType(element);
                if (type.contains("href=''")) {
                    type = type.replace("<a href=''>", "").replace("</a>", "");
                }
                html.buildRow(mustSupport, isModifier, false, field, card, type, description);
            }
            writeHtmlFile(html.getFileName(), html.buildTableEnd().build());
        }
    }

    /**
     *
     * @throws IOException
     */
    private void buildSidebar() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("all-classes-frame.html");
        Document doc = Jsoup.parse(is, null, "");
        Element body = doc.body();

        body.append("<a href='QUICK-overview.html' target='contentFrame'>Overview</a>");

        StringBuilder profiles = new StringBuilder();
        for (Map.Entry<String, String> profileEntry : atlas.getProfileMap().entrySet()) {
            profiles.append(String.format("<dd><a href='%s' target='contentFrame'>%s</a></dd>", profileEntry.getValue(), profileEntry.getKey()));
        }
        body.append("<dl><dt>Profiles</dt>" + profiles.toString() + "</dl>");

        StringBuilder complexTypes = new StringBuilder();
        for (Map.Entry<String, String> complexEntry : atlas.getComplexMap().entrySet()) {
            if (complexEntry.getKey().equals("Code")
                    || complexEntry.getKey().equals("Concept")
                    || complexEntry.getKey().equals("Quantity"))
            {
                complexTypes.append(String.format("<dd><a href='%s' target='_blank'>%s</a></dd>", complexEntry.getValue(), complexEntry.getKey()));

            }
            else {
                complexTypes.append(String.format("<dd><a href='%s' target='contentFrame'>%s</a></dd>", complexEntry.getValue(), complexEntry.getKey()));
            }
        }
        body.append("<dl><dt>Complex Types</dt>" + complexTypes.toString() + "</dl>");

        StringBuilder primitiveTypes = new StringBuilder();
        for (Map.Entry<String, String> primitiveEntry : atlas.getPrimitiveMap().entrySet()) {
            primitiveTypes.append(String.format("<dd><a href='%s' target='_blank'>%s</a></dd>", primitiveEntry.getValue(), primitiveEntry.getKey()));
        }
        body.append("<dl><dt>Primitive Types</dt>" + primitiveTypes + "</dl>");

        writeHtmlFile("all-classes-frame.html", doc.outerHtml());
    }

    /**
     *
     * @throws IOException
     */
    private void buildOverview() throws IOException {
        HtmlBuilder overview = new HtmlBuilder("overview", atlas)
                .buildOverviewHeader("QUICK Data Model")
                .buildParagraph("The QUICK data model provides a logical view of clinical data from the persepctive of representing quality measurement and decision support knowledge.")
                .appendHtml("<h2>Relationship to FHIR and QI-Core</h2>\n" +
                        "          <p>The QUICK data model uses the QI-Core profiles to provide a physical representation for the data. QUICK provides a logical model that enables knowledge authors to ignore certain details of the FHIR Physical representation, including:</p>\n" +
                        "          <ul>\n" +
                        "            <li>The representation of primitives in FHIR using a \"value\" element of a complex type, rather than a true primitive</li>\n" +
                        "            <li>The representation of extensions in FHIR as first class elements in QUICK</li>\n" +
                        "            <li>Direct reference of resources, rather than needing to traverse a \"reference\"</li>\n" +
                        "          </ul>\n" +
                        "          <p>To address the first issue, the QUICK model maps the FHIR base types to CQL primitives, rather than using the FHIR types directly:</p>")
                .buildOverviewTableStart()
                .buildOverviewRow("base64Binary", "String", atlas.getCqlStringUrl())
                .buildOverviewRow("boolean", "Boolean", atlas.getCqlBooleanUrl())
                .buildOverviewRow("code", "String", atlas.getCqlStringUrl())
                .buildOverviewRow("CodeableConcept", "Concept", atlas.getCqlConceptUrl())
                .buildOverviewRow("Coding", "Code", atlas.getCqlCodeUrl())
                .buildOverviewRow("date", "DateTime", atlas.getCqlDateTimeAndTimeUrl())
                .buildOverviewRow("dateTime", "DateTime", atlas.getCqlDateTimeAndTimeUrl())
                .buildOverviewRow("decimal", "Decimal", atlas.getCqlDecimalUrl())
                .buildOverviewRow("id", "String", atlas.getCqlStringUrl())
                .buildOverviewRow("instant", "DateTime", atlas.getCqlDateTimeAndTimeUrl())
                .buildOverviewRow("integer", "Integer", atlas.getCqlIntegerUrl())
                .buildOverviewRow("markdown", "String", atlas.getCqlStringUrl())
                .buildOverviewRow("oid", "String", atlas.getCqlStringUrl())
                .buildOverviewRowWithInterval("Period", "DateTime", atlas.getCqlDateTimeAndTimeUrl())
                .buildOverviewRow("positiveInt", "Integer", atlas.getCqlIntegerUrl())
                .buildOverviewRowWithInterval("Range", "Quantity", atlas.getCqlQuantityUrl())
                .buildOverviewRow("string", "String", atlas.getCqlStringUrl())
                .buildOverviewRow("time", "Time", atlas.getCqlDateTimeAndTimeUrl())
                .buildOverviewRow("uri", "String", atlas.getCqlStringUrl())
                .buildOverviewTableEnd()
                .buildParagraph("To address the second issue, the QUICK model represents FHIR extensions as first-class attributes of the class. To address the third issue, the QUICK model represents FHIR references as direct appearances of the referenced class or classes. NOTE: The third issue is still being worked out, so current QUICK documentation still uses the Reference type to model references.");
        writeHtmlFile(overview.getFileName(), overview.build());
    }

    /**
     *
     * @param snapshotComponent
     * @return
     */
    private Map<String, ElementDefinition> resolveSnapshotElements(StructureDefinition.StructureDefinitionSnapshotComponent snapshotComponent)
    {
        Map<String, ElementDefinition> snapshotElements = new HashMap<>();
        for (ElementDefinition snapshotElement : snapshotComponent.getElement()) {
            snapshotElements.put(snapshotElement.getPath(), snapshotElement);
        }
        return snapshotElements;
    }

    /**
     *
     * @param element
     * @return
     */
    private String resolveType(ElementDefinition element) {
        List<String> types = new ArrayList<>();
        try {
            for (ElementDefinition.TypeRefComponent typeRef : element.getType()) {
                String elementCQLType = mapToCqlType(typeRef.getCode());
                if (elementCQLType.startsWith("Interval")) {

                    //types.add(HtmlBuilder.build(href, elementCQLType));

                    types.add(elementCQLType);
                    continue;
                }

                String href;
                if ((elementCQLType.equals("Reference") && typeRef.hasTargetProfile()) || (elementCQLType.equals("Extension") && typeRef.hasProfile())) {
                    List<CanonicalType> profileList = new ArrayList<>();

                    if (elementCQLType.equals("Reference")) {
                        profileList = typeRef.getTargetProfile();
                    } else if (elementCQLType.equals("Extension")) {
                        profileList = typeRef.getProfile();
                    }

                    String canonicalTypeString;
                    for (CanonicalType canonicalType : profileList) {
                        canonicalTypeString = canonicalType.asStringValue();
                        href = canonicalTypeString;
                        String profileCQLType = elementCQLType;
                        if (elementCQLType.equals("Reference")) {
                            profileCQLType = atlas.getQicoreUrlToType().get(canonicalTypeString);
                            if (profileCQLType == null) {
                                String[] urlSplit = canonicalTypeString.split("/");
                                profileCQLType = urlSplit[urlSplit.length - 1];
                            }
                        } else if (elementCQLType.equals("Extension")) {
                            if (href != null && href.contains("us/core/StructureDefinition")) {
                                href = href.replace("StructureDefinition/", "StructureDefinition-") + ".html";
                            }
                        }

                        if (href != null && href.contains("qicore")) {
                            if (atlas.getQicoreExtensions().containsKey(href)) {
                                href = href.replace("http://hl7.org/fhir/us/qicore/", "").replace("StructureDefinition/", "../StructureDefinition-") + ".html";
                                types.add(HtmlBuilder.buildNewTabLink(href, profileCQLType));
                                continue;
                            }
                            else {
                                href = "QUICK-" + profileCQLType + ".html";
                            }
                        }

                        types.add(HtmlBuilder.buildLink(href, profileCQLType));
                    }
                }
                else {
                    href = atlas.getLinkMap().get(elementCQLType);
                    if (href != null && href.contains("QUICK-Quantity")) {
                        href = "http://cql.hl7.org/02-authorsguide.html#quantities";
                    }
                    if (atlas.getFhirTypes().containsKey(elementCQLType)) {
                        atlas.getComplexMap().put(elementCQLType, href);
                    }

                    if (href != null && href.contains("qicore")) {
                        if (atlas.getQicoreExtensions().containsKey(href)) {
                            href = href.replace("http://hl7.org/fhir/us/qicore/", "").replace("StructureDefinition/", "../StructureDefinition-") + ".html";
                            types.add(HtmlBuilder.buildNewTabLink(href, elementCQLType));
                            continue;
                        }
                        else {
                            href = "QUICK-" + elementCQLType + ".html";
                        }
                    }

                    types.add(HtmlBuilder.buildLink(href, elementCQLType));
                }
            }

            return types.stream().collect(Collectors.joining(" | "));
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("QUICK generation failed in resolveType due to the following error: " + e.getMessage());
        }
    }

    /**
     *
     * @param type
     * @return
     */
    private String mapToCqlType(String type) {
        switch (type) {
            // primitives
            case "boolean": return "Boolean";
            case "integer": return "Integer";
            case "string": return "String";
            case "decimal": return "Decimal";
            case "uri": return "String";
            case "base64Binary": return "String";
            case "instant": return "DateTime";
            case "date": return "DateTime";
            case "dateTime": return "DateTime";
            case "time": return "Time";
            case "code": return "String";
            case "oid": return "String";
            case "id": return "String";
            case "markdown": return "String";
            case "unsignedInt": return "Integer";
            case "positiveInt": return "Integer";
            // complex
            case "Coding": return "Code";
            case "CodeableConcept": return "Concept";
            case "Period": return String.format("<a href='%s' target='_blank'>Interval</a>&lt;<a href='%s' target='_blank'>DateTime</a>&gt;", atlas.getCqlIntervalUrl(), atlas.getCqlDateTimeAndTimeUrl());
            case "Range": return String.format("<a href='%s' target='_blank'>Interval</a>&lt;<a href='#s' target='_blank'>Quantity</a>&gt;", atlas.getCqlIntervalUrl(), atlas.getCqlQuantityUrl());
            default: return type;
        }
    }

    /**
     *
     * @param fileName
     * @param html
     * @throws IOException
     */
    private void writeHtmlFile(String fileName, String html) throws IOException {
        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + fileName)) {
            writer.write(html.getBytes());
            writer.flush();
        }
    }
}
