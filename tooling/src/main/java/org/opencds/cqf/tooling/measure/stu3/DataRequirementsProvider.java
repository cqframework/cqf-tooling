package org.opencds.cqf.tooling.measure.stu3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.fhir.dstu3.model.DataRequirement;
import org.hl7.fhir.dstu3.model.DataRequirement.DataRequirementCodeFilterComponent;
import org.hl7.fhir.dstu3.model.Library;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.opencds.cqf.tooling.library.LibraryResourceProvider;
//import org.opencds.cqf.tooling.cql.execution.LibraryLoader;
//import org.opencds.cqf.tooling.dstu3.helpers.DataElementType;
//import org.opencds.cqf.tooling.dstu3.helpers.LibraryHelper;
//import org.opencds.cqf.tooling.dstu3.providers.CqfMeasure.TerminologyRef;
//import org.opencds.cqf.tooling.dstu3.providers.CqfMeasure.TerminologyRef.TerminologyRefType;


public class DataRequirementsProvider {
//
//    // For creating the CQF measure we need to:
//    // 1. Find the Primary Library Resource
//    // 2. Load the Primary Library as ELM. This will recursively load the dependent libraries as ELM by Name
//    // 3. Load the Library Depedencies as Resources
//    // 4. Update the Data Requirements on the Resources accordingly
//    // Since the Library Loader only exposes the loaded libraries as ELM, we actually have to load them twice.
//    // Once via the loader, Once manually
//    public CqfMeasure createCqfMeasure(Measure measure, LibraryResourceProvider libraryResourceProvider) {
//        Map<VersionedIdentifier, Pair<Library, Library>> libraryMap = this.createLibraryMap(measure, libraryResourceProvider);
//        return this.createCqfMeasure(measure, libraryMap);
//    }
//
//    private Map<VersionedIdentifier, Pair<Library, Library>>  createLibraryMap(Measure measure, LibraryResourceProvider libraryResourceProvider) {
//        LibraryLoader libraryLoader = LibraryHelper.createLibraryLoader(libraryResourceProvider);
//        List<Library> libraries = LibraryHelper.loadLibraries(measure, libraryLoader, libraryResourceProvider);
//        Map<VersionedIdentifier, Pair<Library, Library>> libraryMap = new HashMap<>();
//
//        for (Library library : libraries) {
//            VersionedIdentifier vi = library.getIdentifier();
//            Library libraryResource = libraryResourceProvider.resolveLibraryByName(vi.getId(), vi.getVersion());
//            libraryMap.put(vi, Pair.of(library, libraryResource));
//        }
//
//        return libraryMap;
//    }
//
//    private CqfMeasure createCqfMeasure(Measure measure, Map<VersionedIdentifier, Pair<Library, Library>> libraryMap)
//    {
//        //Ensure All Data Requirements for all referenced libraries
//        Library moduleDefinition = this.getDataRequirements(measure,
//            libraryMap.values().stream().map(x -> x.getRight()).filter(x -> x != null).collect(Collectors.toList()));
//
//        CqfMeasure cqfMeasure = new CqfMeasure(measure);
//        moduleDefinition.getRelatedArtifact().forEach(x -> cqfMeasure.addRelatedArtifact(x));
//        cqfMeasure.setDataRequirement(moduleDefinition.getDataRequirement());
//        cqfMeasure.setParameter(moduleDefinition.getParameter());
//
//        ArrayList<RelatedArtifact> citations = new ArrayList<>();
//        for (RelatedArtifact citation : cqfMeasure.getRelatedArtifact()) {
//            if (citation.hasType() && citation.getType().toCode() == "citation" && citation.hasCitation()) {
//                citations.add(citation);
//            }
//        }
//
//        ArrayList<MeasureGroupComponent> populationStatements = new ArrayList<>();
//        for (MeasureGroupComponent group : measure.getGroup()) {
//            populationStatements.add(group.copy());
//        }
//        List<MeasureGroupPopulationComponent> definitionStatements = new ArrayList<>();
//        List<MeasureGroupPopulationComponent> functionStatements = new ArrayList<>();
//        List<MeasureGroupPopulationComponent> supplementalDataElements = new ArrayList<>();
//        List<CqfMeasure.TerminologyRef> terminology = new ArrayList<>();
//        List<CqfMeasure.TerminologyRef > codes = new ArrayList<>();
//        List<CqfMeasure.TerminologyRef > codeSystems = new ArrayList<>();
//        List<CqfMeasure.TerminologyRef > valueSets = new ArrayList<>();
//        List<StringType> dataCriteria = new ArrayList<>();
//
//        String primaryLibraryId = measure.getLibraryFirstRep().getReferenceElement().getIdPart();
//        Library primaryLibrary = libraryMap.values().stream()
//            .filter(x -> x.getRight() != null)
//            .filter(x -> x.getRight().getIdElement() != null && x.getRight().getIdElement().getIdPart().equals(primaryLibraryId))
//            .findFirst().get().getLeft();
//
//        for (Entry<VersionedIdentifier, Pair<Library, Library>> libraryEntry : libraryMap.entrySet()) {
//            Library library = libraryEntry.getValue().getLeft();
//            Library libraryResource = libraryEntry.getValue().getRight();
//            Boolean isPrimaryLibrary = libraryResource != null && libraryResource.getId().equals(primaryLibraryId);
//            String libraryNamespace = "";
//            if (primaryLibrary.getIncludes() != null) {
//                for (IncludeDef include : primaryLibrary.getIncludes().getDef()) {
//                    if (library.getIdentifier().getId().equalsIgnoreCase(include.getPath())) {
//                        libraryNamespace = include.getLocalIdentifier() + ".";
//                    }
//                }
//            }
//
//
//            if (library.getCodeSystems() != null && library.getCodeSystems().getDef() != null) {
//                for (CodeSystemDef codeSystem : library.getCodeSystems().getDef()) {
//                    String codeId = codeSystem.getId().replace("urn:oid:", "");
//                    String name = codeSystem.getName();
//                    String version = codeSystem.getVersion();
//
//                    CqfMeasure.TerminologyRef term = new CqfMeasure.VersionedTerminologyRef(TerminologyRefType.CODESYSTEM, name, codeId, version);
//                    Boolean exists = false;
//                    for (CqfMeasure.TerminologyRef  t : codeSystems) {
//                        if (t.getDefinition().equalsIgnoreCase(term.getDefinition())) {
//                            exists = true;
//                        }
//                    }
//                    if (!exists) {
//                        codeSystems.add(term);
//                    }
//                }
//            }
//
//            if (library.getCodes() != null && library.getCodes().getDef() != null) {
//                for (CodeDef code : library.getCodes().getDef()) {
//                    String codeId = code.getId();
//                    String name = code.getName();
//                    String codeSystemName = code.getCodeSystem().getName();
//                    String displayName = code.getDisplay();
//                    String codeSystemId = null;
//
//                    for (TerminologyRef rf : codeSystems)
//                    {
//                        if (rf.getName().equals(codeSystemName)) {
//                            codeSystemId = rf.getId();
//                            break;
//                        }
//                    }
//
//                    CqfMeasure.TerminologyRef term = new CqfMeasure.CodeTerminologyRef(name, codeId, codeSystemName, codeSystemId, displayName);
//                    Boolean exists = false;
//                    for (CqfMeasure.TerminologyRef  t : codes) {
//                        if (t.getDefinition().equalsIgnoreCase(term.getDefinition())) {
//                            exists = true;
//                        }
//                    }
//                    if (!exists) {
//                        codes.add(term);
//                    }
//                }
//            }
//
//            if (library.getValueSets() != null && library.getValueSets().getDef() != null) {
//                for (ValueSetDef valueSet : library.getValueSets().getDef()) {
//                    String valueSetId = valueSet.getId().replace("urn:oid:", "");
//                    String name = valueSet.getName();
//
//                    CqfMeasure.TerminologyRef term = new CqfMeasure.VersionedTerminologyRef(TerminologyRefType.VALUESET, name, valueSetId);
//                    Boolean exists = false;
//                    for (CqfMeasure.TerminologyRef  t : valueSets) {
//                        if (t.getDefinition().equalsIgnoreCase(term.getDefinition())) {
//                            exists = true;
//                        }
//                    }
//                    if (!exists) {
//                        valueSets.add(term);
//                    }
//
//                    for (DataRequirement data : cqfMeasure.getDataRequirement()) {
//                        String type = data.getType();
//                        try {
//                            DataElementType dataType = DataElementType.valueOf(type.toUpperCase());
//                            type = dataType.toString();
//                        } catch (Exception e) {
//                            //Do Nothing.  Leave type as is.
//                        }
//
//                        for (DataRequirementCodeFilterComponent filter : data.getCodeFilter()) {
//                            if (filter.hasValueSetStringType() && filter.getValueSetStringType().getValueAsString().equalsIgnoreCase(valueSet.getId())) {
//                                StringType dataElement = new StringType();
//                                dataElement.setValueAsString("\"" + type + ": " + name + "\" using \"" + name + " (" + valueSetId  + ")");
//                                exists = false;
//                                for (StringType string : dataCriteria) {
//                                    if (string.getValueAsString().equalsIgnoreCase(dataElement.getValueAsString())) {
//                                        exists = true;
//                                    }
//                                }
//                                if (!exists) {
//                                    dataCriteria.add(dataElement);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Don't try to parse statements for libraries that don't have resources (such as an embedded FHIRHelpers)
//            if (libraryResource == null) {
//                continue;
//            }
//
//            String cql = "";
//            for (Attachment attachment : libraryResource.getContent()) {
//                cqfMeasure.addContent(attachment);
//                if (attachment.getContentType().equalsIgnoreCase("text/cql")) {
//                    cql = new String(attachment.getData());
//                }
//            }
//
//            String[] cqlLines = cql.replaceAll("[\r]", "").split("[\n]");
//
//            if (library.getStatements() != null) {
//                for (ExpressionDef statement : library.getStatements().getDef()) {
//                    String[] location = statement.getLocator().split("-");
//                    String statementText = "";
//                    String signature = "";
//                    int start = Integer.parseInt(location[0].split(":")[0]);
//                    int end = Integer.parseInt(location[1].split(":")[0]);
//                    for (int i = start - 1; i < end; i++) {
//                        if (cqlLines[i].contains("define function \"" + statement.getName() + "\"(")) {
//                            signature = cqlLines[i].substring(cqlLines[i].indexOf("("), cqlLines[i].indexOf(")") + 1);
//                        }
//                        if (!cqlLines[i].contains("define \"" + statement.getName() + "\":") && !cqlLines[i].contains("define function \"" + statement.getName() + "\"(")) {
//                            statementText = statementText.concat((statementText.length() > 0 ? "\r\n" : "") + cqlLines[i]);
//                        }
//                    }
//                    if (statementText.startsWith("context")) {
//                        continue;
//                    }
//                    MeasureGroupPopulationComponent def = new MeasureGroupPopulationComponent();
//                    def.setName(libraryNamespace + statement.getName() + signature);
//                    def.setCriteria(statementText);
//                    if (statement.getClass() == FunctionDef.class) {
//                            functionStatements.add(def);
//                    }
//                    else {
//                            definitionStatements.add(def);
//                    }
//
//                    for (MeasureGroupComponent group : populationStatements) {
//                        for (MeasureGroupPopulationComponent population : group.getPopulation()) {
//                            if (population.getCriteria() != null && population.getCriteria().equalsIgnoreCase(statement.getName())) {
//                                String code = population.getCode().getCodingFirstRep().getCode();
//                                String display = HQMFProvider.measurePopulationValueSetMap.get(code).displayName;
//                                population.setName(display);
//                                population.setCriteria(statementText);
//                            }
//                        }
//
//                        for (MeasureGroupStratifierComponent mgsc : group.getStratifier()) {
//                            if (mgsc.getCriteria() != null && mgsc.getCriteria().equalsIgnoreCase(statement.getName())) {
//                                mgsc.setCriteria(statementText);
//                            }
//                        }
//                    }
//
//                    for (MeasureSupplementalDataComponent dataComponent : cqfMeasure.getSupplementalData()) {
//                        if (dataComponent.getCriteria()!= null && dataComponent.getCriteria().equalsIgnoreCase(def.getName())) {
//                            supplementalDataElements.add(def);
//                        }
//                    }
//                }
//            }
//        }
//
//        Comparator<StringType> stringTypeComparator = new Comparator<StringType>() {
//            @Override
//            public int compare(StringType item, StringType t1) {
//                String s1 = item.asStringValue();
//                String s2 = t1.asStringValue();
//                return s1.compareToIgnoreCase(s2);
//            }
//        };
//
//        Comparator<TerminologyRef> terminologyRefComparator = new Comparator<TerminologyRef>() {
//            @Override
//            public int compare(TerminologyRef item, TerminologyRef t1) {
//                String s1 = item.getDefinition();
//                String s2 = t1.getDefinition();
//                return s1.compareToIgnoreCase(s2);
//            }
//        };
//        Comparator<MeasureGroupPopulationComponent> populationComparator = new Comparator<MeasureGroupPopulationComponent>() {
//            @Override
//            public int compare(MeasureGroupPopulationComponent item, MeasureGroupPopulationComponent t1) {
//                String s1 = item.getName();
//                String s2 = t1.getName();
//                return s1.compareToIgnoreCase(s2);
//            }
//        };
//
//        Collections.sort(definitionStatements, populationComparator);
//        Collections.sort(functionStatements, populationComparator);
//        Collections.sort(supplementalDataElements, populationComparator);
//        Collections.sort(codeSystems, terminologyRefComparator);
//        Collections.sort(codes, terminologyRefComparator);
//        Collections.sort(valueSets, terminologyRefComparator);
//        Collections.sort(dataCriteria, stringTypeComparator);
//
//        terminology.addAll(codeSystems);
//        terminology.addAll(codes);
//        terminology.addAll(valueSets);
//
//        cqfMeasure.setPopulationStatements(populationStatements);
//        cqfMeasure.setDefinitionStatements(definitionStatements);
//        cqfMeasure.setFunctionStatements(functionStatements);
//        cqfMeasure.setSupplementalDataElements(supplementalDataElements);
//        cqfMeasure.setTerminology(terminology);
//        cqfMeasure.setDataCriteria(dataCriteria);
//        cqfMeasure.setLibraries(libraryMap.values().stream().map(x -> x.getRight()).filter(x -> x != null).collect(Collectors.toList()));
//        cqfMeasure.setCitations(citations);
//
//
//        Map<String, List<Triple<Integer, String, String>>> criteriaMap = new HashMap<>();
//        // Index all usages of criteria
//        for (int i = 0; i < cqfMeasure.getGroup().size(); i++) {
//            MeasureGroupComponent mgc = cqfMeasure.getGroup().get(i);
//            for (int j = 0; j < mgc.getPopulation().size(); j++) {
//                MeasureGroupPopulationComponent mgpc = mgc.getPopulation().get(j);
//                String criteria = mgpc.getCriteria();
//                if (criteria != null && !criteria.isEmpty()) {
//                    if (!criteriaMap.containsKey(criteria)) {
//                        criteriaMap.put(criteria, new ArrayList<Triple<Integer, String, String>>());
//                    }
//
//                    criteriaMap.get(criteria).add(Triple.of(i, mgpc.getCode().getCodingFirstRep().getCode(), mgpc.getDescription()));
//                }
//            }
//        }
//
//        // Find shared usages
//        for (Entry<String, List<Triple<Integer, String, String>>> entry : criteriaMap.entrySet()) {
//            String criteria = entry.getKey();
//            if (cqfMeasure.getGroup().size() == 1 || entry.getValue().stream().map(x -> x.getLeft()).distinct().count() > 1) {
//                String code = entry.getValue().get(0).getMiddle();
//                String display = HQMFProvider.measurePopulationValueSetMap.get(code).displayName;
//                cqfMeasure.addSharedPopulationCritiera(criteria, display, entry.getValue().get(0).getRight());
//            }
//        }
//
//        // If there's only one group every critieria was shared. Kill the group.
//        if (cqfMeasure.getGroup().size() == 1) {
//            cqfMeasure.getGroup().clear();
//        }
//        // Otherwise, remove the shared components.
//        else {
//            for (int i = 0; i < cqfMeasure.getGroup().size(); i++) {
//                MeasureGroupComponent mgc = cqfMeasure.getGroup().get(i);
//                List<MeasureGroupPopulationComponent> newMgpc = new ArrayList<MeasureGroupPopulationComponent>();
//                for (int j = 0; j < mgc.getPopulation().size(); j++) {
//                    MeasureGroupPopulationComponent mgpc = mgc.getPopulation().get(j);
//                    if (mgpc.hasCriteria() && !mgpc.getCriteria().isEmpty() && !cqfMeasure.getSharedPopulationCritieria().containsKey(mgpc.getCriteria())) {
//                        String code = mgpc.getCode().getCodingFirstRep().getCode();
//                        String display = HQMFProvider.measurePopulationValueSetMap.get(code).displayName;
//                        mgpc.setName(display);
//                        newMgpc.add(mgpc);
//                    }
//                }
//
//                mgc.setPopulation(newMgpc);
//            }
//        }
//
//        CqfMeasure processedMeasure = processMarkDown(cqfMeasure);
//
//        return processedMeasure;
//    }
//
//    private CqfMeasure processMarkDown(CqfMeasure measure) {
//
//        MutableDataSet options = new MutableDataSet();
//
//        options.setFrom(ParserEmulationProfile.GITHUB_DOC);
//        options.set(Parser.EXTENSIONS, Arrays.asList(
//                AutolinkExtension.create(),
//                //AnchorLinkExtension.create(),
//                //EmojiExtension.create(),
//                StrikethroughExtension.create(),
//                TablesExtension.create(),
//                TaskListExtension.create()
//        ));
//
//        // uncomment and define location of emoji images from https://github.com/arvida/emoji-cheat-sheet.com
//        // options.set(EmojiExtension.ROOT_IMAGE_PATH, "");
//
//        // Uncomment if GFM anchor links are desired in headings
//        // options.set(AnchorLinkExtension.ANCHORLINKS_SET_ID, false);
//        // options.set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "anchor");
//        // options.set(AnchorLinkExtension.ANCHORLINKS_SET_NAME, true);
//        // options.set(AnchorLinkExtension.ANCHORLINKS_TEXT_PREFIX, "<span class=\"octicon octicon-link\"></span>");
//
//        // References compatibility
//        options.set(Parser.REFERENCES_KEEP, KeepType.LAST);
//
//        // Set GFM table parsing options
//        options.set(TablesExtension.COLUMN_SPANS, false)
//                .set(TablesExtension.MIN_HEADER_ROWS, 1)
//                .set(TablesExtension.MAX_HEADER_ROWS, 1)
//                .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
//                .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
//                .set(TablesExtension.WITH_CAPTION, false)
//                .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true);
//
//        // Setup List Options for GitHub profile which is kramdown for documents
//        options.setFrom(ParserEmulationProfile.GITHUB_DOC);
//
//        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
//
//
//        Parser parser = Parser.builder(options).build();
//        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
//
//        measure.setDescription(markdownToHtml(parser, renderer, measure.getDescription()));
//        measure.setPurpose(markdownToHtml(parser, renderer, measure.getPurpose()));
//        // measure.setCopyright(markdownToHtml(parser, renderer, measure.getCopyright()));
//        measure.setRationale(markdownToHtml(parser, renderer, measure.getRationale()));
//        measure.setClinicalRecommendationStatement(markdownToHtml(parser, renderer, measure.getClinicalRecommendationStatement()));
//        measure.setGuidance(markdownToHtml(parser, renderer, measure.getGuidance()));
//
//        measure.setDefinition(measure.getDefinition().stream()
//            .map(x -> markdownToHtml(parser, renderer, x.getValueAsString()))
//            .map(x -> new MarkdownType(x))
//            .collect(Collectors.toList()));
//
//        return measure;
//    }
//
//    private String markdownToHtml(Parser parser, HtmlRenderer renderer, String markdown) {
//        if (Strings.isNullOrEmpty(markdown)) {
//            return null;
//        }
//
//        Node document = parser.parse(markdown);
//        return renderer.render(document);
//    }
//
//    public Library getDataRequirements(Measure measure, LibraryResourceProvider libraryResourceProvider){
//        Map<VersionedIdentifier, Pair<Library, Library>> libraryMap = this.createLibraryMap(measure, libraryResourceProvider);
//        return this.getDataRequirements(measure, libraryMap.values().stream().map(x -> x.getRight()).filter(x -> x != null).collect(Collectors.toList()));
//    }
//
//    private Library getDataRequirements(Measure measure, Collection<Library> libraries){
//        List<DataRequirement> reqs = new ArrayList<>();
//        List<RelatedArtifact> dependencies = new ArrayList<>();
//        List<ParameterDefinition> parameters = new ArrayList<>();
//
//        for (Library library : libraries) {
//            for (RelatedArtifact dependency : library.getRelatedArtifact()) {
//                if (dependency.getType().toCode().equals("depends-on")) {
//                    dependencies.add(dependency);
//                }
//            }
//
//            reqs.addAll(library.getDataRequirement());
//            parameters.addAll(library.getParameter());
//        }
//
//        List<Coding> typeCoding = new ArrayList<>();
//        typeCoding.add(new Coding().setCode("module-definition"));
//        Library library =
//                new Library().setType(new CodeableConcept().setCoding(typeCoding));
//
//        if (!dependencies.isEmpty()) {
//            library.setRelatedArtifact(dependencies);
//        }
//
//        if (!reqs.isEmpty()) {
//            library.setDataRequirement(reqs);
//        }
//
//        if (!parameters.isEmpty()) {
//            library.setParameter(parameters);
//        }
//
//        return library;
//    }
//
//
//    public CqlTranslator getTranslator(Library library, LibraryManager libraryManager, ModelManager modelManager) {
//        Attachment cql = null;
//        for (Attachment a : library.getContent()) {
//            if (a.getContentType().equals("text/cql")) {
//                cql = a;
//                break;
//            }
//        }
//
//        if (cql == null) {
//            return null;
//        }
//
//        CqlTranslator translator = LibraryHelper.getTranslator(
//                new ByteArrayInputStream(Base64.getDecoder().decode(cql.getDataElement().getValueAsString())),
//                libraryManager, modelManager);
//
//        return translator;
//    }
//
//    public void formatCql(Library library) {
//        for (Attachment att : library.getContent()) {
//            if (att.getContentType().equals("text/cql")) {
//                try {
//                    FormatResult fr = CqlFormatterVisitor.getFormattedOutput(new ByteArrayInputStream(
//                            Base64.getDecoder().decode(att.getDataElement().getValueAsString())));
//
//                    // Only update the content if it's valid CQL.
//                    if (fr.getErrors().size() == 0) {
//                        Base64BinaryType bt = new Base64BinaryType(
//                                new String(Base64.getEncoder().encode(fr.getOutput().getBytes())));
//                        att.setDataElement(bt);
//                    }
//                } catch (IOException e) {
//                    // Intentionally empty for now
//                }
//            }
//        }
//    }
//
//    public void ensureElm(Library library, CqlTranslator translator) {
//
//        library.getContent().removeIf(a -> a.getContentType().equals("application/elm+xml"));
//        String xml = translator.toXml();
//        Attachment elm = new Attachment();
//        elm.setContentType("application/elm+xml");
//        elm.setData(xml.getBytes());
//        library.getContent().add(elm);
//    }

    public void ensureRelatedArtifacts(Library library, CqlTranslator translator, LibraryResourceProvider libraryResourceProvider)
    {
        library.getRelatedArtifact().clear();
        org.hl7.elm.r1.Library elm = translator.toELM();
        if (elm.getIncludes() != null && !elm.getIncludes().getDef().isEmpty()) {
            for (org.hl7.elm.r1.IncludeDef def : elm.getIncludes().getDef()) {
                library.addRelatedArtifact(new RelatedArtifact().setType(RelatedArtifact.RelatedArtifactType.DEPENDSON)
                        .setResource(new Reference().setReference(
                            libraryResourceProvider.resolveLibraryByName(def.getPath(), def.getVersion()).getId())));
            }
        }

        if (elm.getUsings() != null && !elm.getUsings().getDef().isEmpty()) {
            for (org.hl7.elm.r1.UsingDef def : elm.getUsings().getDef()) {
                String uri = def.getUri();
                String version = def.getVersion();
                if (version != null && !version.isEmpty()) {
                    uri = uri + "|" + version;
                }
                library.addRelatedArtifact(
                        new RelatedArtifact().setType(RelatedArtifact.RelatedArtifactType.DEPENDSON).setUrl(uri));
            }
        }
    }

    public void ensureDataRequirements(Library library, CqlTranslator translator) {
        library.getDataRequirement().clear();

        List<DataRequirement> reqs = new ArrayList<>();

        for (org.hl7.elm.r1.Retrieve retrieve : translator.toRetrieves()) {
            DataRequirement dataReq = new DataRequirement();
            dataReq.setType(retrieve.getDataType().getLocalPart());
            if (retrieve.getCodeProperty() != null) {
                DataRequirementCodeFilterComponent codeFilter = new DataRequirementCodeFilterComponent();
                codeFilter.setPath(retrieve.getCodeProperty());
                if (retrieve.getCodes() instanceof ValueSetRef) {
                    Type valueSetName = new StringType(
                            getValueSetId(((ValueSetRef) retrieve.getCodes()).getName(), translator));
                    codeFilter.setValueSet(valueSetName);
                }
                dataReq.setCodeFilter(Collections.singletonList(codeFilter));
            }
            // TODO - Date filters - we want to populate this with a $data-requirements
            // request as there isn't a good way through elm analysis
            reqs.add(dataReq);
        }

        // org.hl7.elm.r1.Library elm = translator.toELM();
        // Codes codes = elm.getCodes();
        // for (CodeDef cd : codes.getDef()) {
        //     cd.
        // }

        library.setDataRequirement(reqs);
    }

    public String getValueSetId(String valueSetName, CqlTranslator translator) {
        org.hl7.elm.r1.Library.ValueSets valueSets = translator.toELM().getValueSets();
        if (valueSets != null) {
            for (org.hl7.elm.r1.ValueSetDef def : valueSets.getDef()) {
                if (def.getName().equals(valueSetName)) {
                    return def.getId();
                }
            }
        }

        return valueSetName;
    }
}