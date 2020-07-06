package org.opencds.cqf.modelinfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hl7.elm_modelinfo.r1.*;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r4.model.Enumerations.BindingStrength;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionKind;
import org.w3._1999.xhtml.P;

public abstract class ClassInfoBuilder {
    protected Map<String, StructureDefinition> structureDefinitions;
    protected Map<String, TypeInfo> typeInfos = new HashMap<String, TypeInfo>();
    protected Map<String, String> typeTargets = new HashMap<String, String>();
    protected ClassInfoSettings settings;

    public ClassInfoBuilder(ClassInfoSettings settings, Map<String, StructureDefinition> structureDefinitions) {
        this.structureDefinitions = structureDefinitions;
        this.settings = settings;
    }

    protected abstract void innerBuild();
    protected void afterBuild() {
        //Clean up Content Reference Specifiers
        Collection<TypeInfo> typeInfoValues = this.getTypeInfos().values();
        for (TypeInfo ti : this.getTypeInfos().values()) {
            if (ti instanceof ClassInfo) {
                ClassInfo ci = (ClassInfo)ti;
                for (ClassInfoElement cie : ci.getElement()) {
                    if (hasContentReferenceTypeSpecifier(cie)) {
                        fixupContentReferenceSpecifier(this.settings.modelName, cie);
                    }
                }
            }
        }
    }

    public Map<String, TypeInfo> build() {
        this.innerBuild();
        return this.getTypeInfos();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private String getHead(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return null;
        } else if (index > 0) {
            return url.substring(0, index);
        } else {
            return "";
        }
    }

    private String getTail(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return null;
        } else if (index > 0) {
            return url.substring(index + 1);
        } else {
            return "";
        }
    }

    private String resolveModelName(String url) throws Exception {
        // Strips off the identifier and type name
        String model = getHead(getHead(url));
        if (this.settings.urlToModel.containsKey(model)) {
            return this.settings.urlToModel.get(model);
        }

        throw new Exception("Couldn't resolve model name for url: " + url);
    }

    private String resolveTypeName(String url) throws Exception {
        if (url != null) {
            String modelName = resolveModelName(url);
            String typeName = getTypeNameFromUrl(url);
            return getTypeName(modelName, typeName);
        }

        return null;
    }

    // Resolves the base type name for the given type
    private String resolveBaseTypeName(String typeId) throws Exception {
        if (typeId != null) {
            StructureDefinition sd = structureDefinitions.get(typeId);
            return resolveTypeName(sd.getBaseDefinition());
        }

        return null;
    }

    private String getTypeNameFromUrl(String url) {
        if (url != null) {
            String typeId = getTail(url);
            StructureDefinition sd = structureDefinitions.get(typeId);
            return getTypeName(sd);
        }

        return null;
    }

    private String getTypeName(String modelName, String typeName) {
        return modelName != null ? modelName + "." + typeName : typeName;
    }

    protected String getTypeName(NamedTypeSpecifier typeSpecifier) {
        return this.getTypeName(typeSpecifier.getNamespace(), typeSpecifier.getName());
    }

    protected String mapTypeName(String typeName) {
        if (this.settings.typeNameMappings != null && this.settings.typeNameMappings.containsKey(typeName)) {
            return this.settings.typeNameMappings.get(typeName);
        }

        return typeName;
    }

    protected String getTypeName(StructureDefinition sd) {
        String typeId = getTail(sd.getId());
        String typeName = sd.getName() == null ? capitalizePath(typeId) : sd.getName();
        if (this.settings.modelPrefix != null && typeName.startsWith(this.settings.modelPrefix)) {
            typeName = typeName.substring(this.settings.modelPrefix.length());
        }
        typeName = mapTypeName(typeName);
        return typeName;
    }

    private String getLabel(StructureDefinition sd) {
        return sd.getTitle() != null ? sd.getTitle() : getTypeName(sd);
    }

    protected TypeInfo resolveType(String name) {
        return this.typeInfos.get(name);
    }

    private TypeInfo resolveType(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            return this.resolveType(((NamedTypeSpecifier) typeSpecifier).getName());
        } else {
            ListTypeSpecifier lts = (ListTypeSpecifier) typeSpecifier;
            if (lts.getElementType() != null) {
                return this.resolveType(lts.getElementType());
            } else {
                return this.resolveType(lts.getElementTypeSpecifier());
            }
        }
    }

    private TypeInfo resolveType(ClassInfoElement classInfoElement) {
        if (classInfoElement.getElementType() != null) {
            return this.resolveType(classInfoElement.getElementType());
        } else {
            return this.resolveType(classInfoElement.getElementTypeSpecifier());
        }
    }

    protected String getQualifier(String name) {
        if (name == null) {
            return null;
        }
        int index = name.indexOf(".");
        if (index > 0) {
            return name.substring(0, index);
        }

        return null;
    }

    protected String unQualify(String name) {
        if (name == null) {
            return null;
        }
        if (name.contains(".")) {
            int index = name.indexOf(".");
            if (index > 0) {
                return name.substring(index + 1);
            }

            return null;
        }

        return null;
    }

    // Returns the given string with the first letter capitalized
    private String capitalize(String name) {
        if (name.length() >= 1) {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }

        return name;
    }

    // Returns the given path with the first letter of every path capitalized
    private String capitalizePath(String path) {
        if (path.contains("-")) {
            return String.join("_", Arrays.asList(path.split("\\-")).stream().map(x -> this.capitalize(x))
                    .collect(Collectors.toList()));
        } else {
            return String.join(".", Arrays.asList(path.split("\\.")).stream().map(x -> this.capitalize(x))
                    .collect(Collectors.toList()));
        }
    }

    private String capitalizePath(String path, String modelName) {
        if (path.contains("-")) {
            return String.join("_", Arrays.asList(path.split("\\-")).stream().map(x -> this.capitalize(x))
                    .collect(Collectors.toList()));
        } else if (!path.contains(modelName + ".")) {
            return String.join(".", Arrays.asList(path.split("\\.")).stream().map(x -> this.capitalize(x))
                    .collect(Collectors.toList()));
        } else
            return path;
    }

    // Strips the given root from the given path.
    // Throws an error if the path does not start with the root.
    protected String stripRoot(String path, String root) throws Exception {
        int index = path.indexOf(root);
        if (index == -1) {
            throw new Exception("Path " + path + " does not start with the root " + root + ".");
        }

        String result = path.substring(root.length());

        if (result.startsWith(".")) {
            result = result.substring(1);
        }

        return result;
    }

    static String stripPath(String path) throws Exception {
        int index = path.lastIndexOf(".");
        if (index == -1) {
            throw new Exception("Path " + path + " does not have any continuation represented by " + ".");
        }

        String result = path.substring(index);

        if (result.startsWith(".")) {
            result = result.substring(1);
        }

        return result;
    }

    // Strips the [x] suffix of an element name which indicates a choice in FHIR
    private static String stripChoice(String name) {
        int index = name.indexOf("[x]");
        if (index != -1) {
            return name.substring(0, index);
        }

        return name;
    }

    private String normalizeValueElement(String path) {
        int index = path.indexOf(".value");
        if (index != -1 && path.length() > (index + ".value".length())) {
            return path.substring(0, index);
        } else {
            return path;
        }
    }

    // Translates a path from the source root to the target root
    private String translate(String path, String sourceRoot, String targetRoot) {
        String result = this.normalizeValueElement(path);
        int sourceRootIndex = result.indexOf(sourceRoot);
        if (sourceRootIndex == 0) {
            result = targetRoot + result.substring(sourceRoot.length());
        }

        return result;
    }

    // Returns the value of the given string as an integer if it is integer-valued,
    // nil otherwise.
    private Integer asInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    // Returns true if the ElementDefinition describes a Backbone Element
    private Boolean isBackboneElement(ElementDefinition ed) {
        return ed.getType() != null && ed.getType().size() == 1
                && ed.getType().get(0).getCode().equals("BackboneElement");
    }

    // Returns true if the StructureDefinition is "BackboneElement"
    private Boolean isBackboneElement(StructureDefinition sd) {
        return getTail(sd.getId()).equals("BackboneElement");
    }

    // Returns true if the StructureDefinition is "Element"
    private Boolean isElement(StructureDefinition sd) {
        return getTail(sd.getId()).equals("Element");
    }

    private Boolean isExtension(StructureDefinition sd) {
        return getTail(sd.getId()).equals("Extension") || (sd.getBaseDefinition() != null && getTail(sd.getBaseDefinition()).equals("Extension"));
    }

    // Returns true if the ElementDefinition describes an Extension
    private Boolean isExtension(ElementDefinition ed) {
        return ed.getType() != null && ed.getType().size() == 1 && ed.getType().get(0).hasCode()
                && ed.getType().get(0).getCode() != null && ed.getType().get(0).getCode().equals("Extension");
    }

    // Returns the type code for the element if there is only one type ref
    private String typeCode(ElementDefinition ed) {
        // In FHIR 4.0.1, the typeCode for the Resource.id element is incorrectly specified as a System.String
        if (this.settings.modelName.equals("FHIR") && ed.getPath().equals("Resource.id")) {
            return "FHIR.string";
        }

        if (ed.getType() != null && ed.getType().size() == 1) {
            return ed.getType().get(0).getCode();
        }

        return null;
    }

    private String typeUrl(ElementDefinition ed) {
        String typeCode = typeCode(ed);
        if (typeCode != null) {
            TypeRefComponent typeRef = ed.getType().get(0);
            if (typeRef.hasProfile() && typeRef.getProfile().size() == 1) {
                String typeId = getTail(typeRef.getProfile().get(0).asStringValue());
                StructureDefinition sd = structureDefinitions.get(typeId);
                if (sd != null) {
                    return sd.getUrl();
                }
            }
        }

        return null;
    }

    // Returns true if the type specifier is a NamedTypeSpecifier referencing
    // FHIR.BackboneElement
    private Boolean isBackboneElement(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier) typeSpecifier;
            String typeName = this.getTypeName(nts);
            return typeName != null && typeName.endsWith(".BackboneElement");
        }

        return false;
    }

    // Returns the set of element definitions for the given type id
    private List<ElementDefinition> getElementDefinitions(String typeId) {
        if (!structureDefinitions.containsKey(typeId)) {
            throw new RuntimeException("Could not retrieve element definitions for " + typeId);
        }

        return structureDefinitions.get(typeId).getSnapshot().getElement();
    }

    // Returns the set of element definitions for the given type
    private List<ElementDefinition> getElementDefinitions(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier) typeSpecifier;
            if (nts.getName() != null) {
                return this.getElementDefinitions(nts.getName());
            }
        }

        return null;
    }

    // Returns the element definition for the given path
    private ElementDefinition elementForPath(List<ElementDefinition> elements, String path) {
        if (elements != null) {
            for (ElementDefinition ed : elements) {
                if (ed.getPath().equals(path)) {
                    return ed;
                }
            }
        }

        return null;
    }

    // Returns the given extension if it exists
    private Extension extension(Element element, String url) {
        if (element != null) {
            for (Extension ex : element.getExtension()) {
                if (ex.getUrl() != null && ex.getUrl().equals(url)) {
                    return ex;
                }
            }
        }

        return null;
    }

    // Returns true if the type is a "codeable" type (i.e. String, Code, Concept,
    // string, code, Coding, CodeableConcept)
    private Boolean isCodeable(String typeName) {
        return this.settings.codeableTypes.contains(typeName);
    }

    // Returns the primary code path for the given type, based on the following:
    // If the type has an entry in the PrimaryCodePaths table, the value there is
    // used
    // If the type has an element named "code" with a type of "String", "Code",
    // "Coding", or "CodeableConcept", that element is used
    private String primaryCodePath(List<ClassInfoElement> elements, String typeName) {
        if (this.settings.primaryCodePath.containsKey(typeName)) {
            return this.settings.primaryCodePath.get(typeName);
        } else if (elements != null) {
            for (ClassInfoElement e : elements) {
                if (e.getName().toLowerCase().equals("code") && this.isCodeable(e.getElementType())) {
                    return e.getName();
                }
            }

        }

        return null;
    }

    private String unQualifyId(String id)
    {
        if (id == null) {
            return null;
        }

        if (id.contains("/")) {
            int index = id.indexOf("/");
            if (index > 0) {
                return id.substring(index + 1);
            }
        }

        if (id.contains("-")) {
            int index = id.indexOf("-");
            if (index > 0) {
                return id.substring(index + 1);
            }
        }

        return null;
    }

    // Returns the element with the given name, if it exists
    private ClassInfoElement element(List<ClassInfoElement> elements, String name) {
        if (elements != null) {
            for (ClassInfoElement cie : elements) {
                if (cie.getName().equals(name)) {
                    return cie;
                }
            }
        }

        return null;
    }

    //This is the start of the impactful logic... the above are mostly helper functions

    // Returns the element with the given path
    protected ClassInfoElement forPath(List<ClassInfoElement> elements, String path) {
        ClassInfoElement result = null;
        String[] segments = path.split("\\.");
        for (String p : segments) {
            result = element(elements, p);
            if (result != null) {
                TypeInfo elementType = resolveType(result);
                elements = ((ClassInfo) elementType).getElement();
            }
        }

        return result;
    }

    private void ensureClassInfo(String modelName, String typeName) {
        String qualifiedTypeName = getTypeName(modelName, typeName);
        try {
            if (!this.typeInfos.containsKey(qualifiedTypeName) && !this.settings.primitiveTypeMappings.containsKey(qualifiedTypeName) && !modelName.equals("System")) {
                buildClassInfo(modelName, structureDefinitions.get(typeName));
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException(String.format("Could not resolve type %s.", qualifiedTypeName), e);
        }
    }

    private TypeSpecifier buildTypeSpecifier(String modelName, String typeName) {
        if (modelName != null && typeName != null) {
            ensureClassInfo(modelName, typeName);
            NamedTypeSpecifier ts = new NamedTypeSpecifier();
            ts.setNamespace(modelName);
            ts.setName(typeName);

            return ts;
        }

        return null;
    }

    private TypeSpecifier buildTypeSpecifier(String typeName) {
        if (typeName.startsWith("Interval<") && typeName.endsWith(">")) {
            TypeSpecifier pointTypeSpecifier = buildTypeSpecifier(typeName.substring(typeName.indexOf("<") + 1, typeName.indexOf(">")));
            return new IntervalTypeSpecifier().withPointTypeSpecifier(pointTypeSpecifier);
        }
        return this.buildTypeSpecifier(this.getQualifier(typeName), this.unQualify(typeName));
    }

    private String resolveMappedTypeName(String url) throws Exception {
        if (url != null) {
            String modelName = resolveModelName(url);
            String typeName = getTypeNameFromUrl(url);
            return resolveMappedTypeName(modelName, typeName);
        }

        return null;
    }

    private boolean isSystemTypeName(String typeName) {
        return typeName.startsWith("System.");
    }

    private boolean isClassType(String typeName) {
        if (isSystemTypeName(typeName)) {
            switch (typeName) {
                case "System.Boolean":
                case "System.Integer":
                case "System.Decimal":
                case "System.String":
                case "System.DateTime":
                case "System.Date":
                case "System.Time":
                    return false;
            }

            return true;
        }
        else {
            TypeInfo type = resolveType(typeName);
            return type instanceof ClassInfo;
        }
    }

    private boolean isClassType(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            return isClassType(getTypeName((NamedTypeSpecifier)typeSpecifier));
        }

        return false;
    }

    private boolean isMappedTypeName(String modelName, String typeName) {
        return this.settings.useCQLPrimitives && this.settings.cqlTypeMappings.values().contains(modelName + "." + typeName);
    }

    private boolean isPrimitiveMappedTypeName(String modelName, String typeName) {
        return this.settings.useCQLPrimitives && this.settings.primitiveTypeMappings.values().contains(modelName + "." + typeName);
    }

    private String resolveMappedTypeName(String modelName, String typeName) {
        // In FHIR 4.0.1, System type names consistent with FHIRPath type names are used to specify actual primitive types in the FHIR structure definitions.
        String mappedTypeName = null;
        if (typeName.startsWith("http://hl7.org/fhirpath/")) {
            mappedTypeName = typeName.substring("http://hl7.org/fhirpath/".length());
            return mappedTypeName;
        }

        if (this.settings.useCQLPrimitives) {
            mappedTypeName = this.settings.cqlTypeMappings.get(modelName + "." + typeName);
            if (mappedTypeName != null) {
                return mappedTypeName;
            }
        }

        mappedTypeName = modelName + "." + typeName;
        return mappedTypeName;
    }

    private String resolveMappedTypeName(String modelName, TypeRefComponent typeRef) {
        if (typeRef.hasCode() && typeRef.getCode() != null) {
            return resolveMappedTypeName(modelName, typeRef.getCode());
        }

        Extension typeExtension = typeRef.getCodeElement().getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/structuredefinition-xml-type");
        if (typeExtension != null) {
            return resolveMappedTypeName(modelName, typeExtension.getValue().toString());
        }

        throw new IllegalArgumentException("Could not determine mapping for null type code");
    }

    // Builds a TypeSpecifier from the given list of TypeRefComponents
    private TypeSpecifier buildTypeSpecifier(String modelName, TypeRefComponent typeRef) {
        try {
            if (typeRef != null && typeRef.getProfile() != null && typeRef.getProfile().size() != 0) {
                List<CanonicalType> canonicalTypeRefs = typeRef.getProfile();
                if (canonicalTypeRefs.size() == 1) {
                    return this.buildTypeSpecifier(this.resolveMappedTypeName(canonicalTypeRefs.get(0).asStringValue()));
                } else if (canonicalTypeRefs.size() > 1) {
                    ChoiceTypeSpecifier cts = new ChoiceTypeSpecifier();
                    for (CanonicalType canonicalType : canonicalTypeRefs) {
                        if (canonicalType != null) {
                            cts.withChoice(
                                    this.buildTypeSpecifier(this.resolveMappedTypeName(canonicalType.asStringValue())));
                        }
                    }
                    return cts;
                } else
                    return null;
            } else {
                return this.buildTypeSpecifier(this.resolveMappedTypeName(modelName, typeRef));
            }
        } catch (Exception e) {
            System.out.println("Error building type specifier for " + modelName + "."
                    + (typeRef != null ? typeRef.getCode() : "<No Type>") + ": " + e.getMessage());
            return null;
        }
    }

    private TypeSpecifier buildTypeSpecifier(String modelName, List<TypeRefComponent> typeReferenceRefComponents) {

        if (typeReferenceRefComponents == null) {
            return buildTypeSpecifier(modelName, (TypeRefComponent) null);
        } else {
            List<TypeSpecifier> specifiers = typeReferenceRefComponents.stream()
                    .map(x -> this.buildTypeSpecifier(modelName, x)).filter(distinctByKey(x -> x.toString()))
                    .collect(Collectors.toList());

            if (specifiers.size() == 1) {
                return specifiers.get(0);
            } else if (specifiers.size() > 1) {
                ChoiceTypeSpecifier cts = new ChoiceTypeSpecifier();
                return cts.withChoice(specifiers);
            } else {
                return null;
            }
        }
    }

    // Gets the type specifier for the given class info element
    protected TypeSpecifier getTypeSpecifier(ClassInfoElement classInfoElement) {
        if (classInfoElement.getElementType() != null) {
            return this.buildTypeSpecifier(this.getQualifier(classInfoElement.getElementType()),
                    this.unQualify(classInfoElement.getElementType()));
        } else if (classInfoElement.getElementTypeSpecifier() != null) {
            if (classInfoElement.getElementTypeSpecifier() instanceof ListTypeSpecifier) {
                ListTypeSpecifier lts = (ListTypeSpecifier) classInfoElement.getElementTypeSpecifier();
                if (lts.getElementType() != null) {
                    return this.buildTypeSpecifier(this.getQualifier(lts.getElementType()),
                            this.unQualify(lts.getElementType()));
                } else {
                    return lts.getElementTypeSpecifier();
                }
            }
        }

        return null;
    }

    private boolean typeSpecifiersEqual(TypeSpecifier left, TypeSpecifier right) {
        if (left instanceof NamedTypeSpecifier && right instanceof NamedTypeSpecifier) {
            return this.getTypeName((NamedTypeSpecifier)left).equals(this.getTypeName((NamedTypeSpecifier)right));
        }

        // TODO: More sophisticated type specifier equality determination
        return left.equals(right);
    }

    // Builds the type specifier for the given element
    private TypeSpecifier buildElementTypeSpecifier(String modelName, String root, ElementDefinition ed) {
        if (ed.getContentReference() != null) {
            NamedTypeSpecifier nts = new NamedTypeSpecifier();
            nts.setName(ed.getContentReference());
            return nts;
        }

        try {
            String typeCode = this.typeCode(ed);
            String typeName;
            String typeId;
            if (ed.getId().equals("Extension.url")) {
                // This is special-case code because many underlying class representations reuse the FHIR.uri type
                // to represent url-valued attributes
                typeName = getTypeName(modelName, "uri");
                if (this.settings.useCQLPrimitives && this.settings.primitiveTypeMappings.containsKey(typeName)) {
                    return this.buildTypeSpecifier(this.settings.primitiveTypeMappings.get(typeName));
                }
                else {
                    return this.buildTypeSpecifier(typeName);
                }
            }
            if (typeCode != null && typeCode.equals("Extension") && ed.getId().contains(":")
                    && ed.getType().get(0).hasProfile()) {
                List<CanonicalType> extensionProfile = ed.getType().get(0).getProfile();
                if (extensionProfile.size() == 1) {
                    //set targetPath here
                    typeId = getTail(extensionProfile.get(0).asStringValue());
                    StructureDefinition sd = this.structureDefinitions.get(typeId);
                    typeName = getTypeName(sd);
                    String qualifiedTypeName = this.getTypeName(modelName, typeName);
                    if (!this.typeInfos.containsKey(qualifiedTypeName)) {
                        buildClassInfo(modelName, sd);
                    }

                    NamedTypeSpecifier nts = new NamedTypeSpecifier();
                    nts.setNamespace(modelName);
                    nts.setName(typeName);

                    return nts;
                }
                else if (extensionProfile.size() > 1) {
                    ChoiceTypeSpecifier cts = new ChoiceTypeSpecifier();
                    for (CanonicalType canonicalType : extensionProfile) {
                        if (canonicalType != null) {
                            cts.withChoice(
                                    this.buildTypeSpecifier(this.resolveTypeName(canonicalType.asStringValue())));
                        }
                    }

                    return cts;
                }
                else {
                    return null;
                }
            }
            else if (typeCode != null && typeCode.equals("BackboneElement") && ed.hasSliceName()) {
                // This is a slice, create a derived type out of the slice name
                // ed.getPath() - is the base type for the slice
                // ed.sliceName() - is the name of the type
                String baseTypeName = capitalizePath(ed.getPath());
                String qualifiedBaseTypeName = getTypeName(modelName, baseTypeName);
                typeName = capitalizePath(ed.getPath()) + "." + ed.getSliceName();
                String qualifiedTypeName = getTypeName(modelName, typeName);
                if (!this.typeInfos.containsKey(qualifiedTypeName)) {
                    ClassInfo sliceType = new ClassInfo().withNamespace(modelName).withName(typeName).withBaseType(qualifiedBaseTypeName).withRetrievable(false);
                    this.typeInfos.put(qualifiedTypeName, sliceType);
                }

                NamedTypeSpecifier nts = new NamedTypeSpecifier();
                nts.setNamespace(modelName);
                nts.setName(typeName);

                return nts;
            }
            else if (typeCode != null && typeCode.equals("code") && ed.hasBinding()
                    && ed.getBinding().getStrength() == BindingStrength.REQUIRED) {
                Extension bindingExtension = this.extension(ed.getBinding(), "http://hl7.org/fhir/StructureDefinition/elementdefinition-bindingName");
                if (bindingExtension != null) {
                    typeName = capitalizePath(((StringType) (bindingExtension.getValue())).getValue());
                }

                else {
                    TypeSpecifier ts = this.buildTypeSpecifier(modelName, ed.hasType() ? ed.getType() : null);
                    if (ts instanceof NamedTypeSpecifier && ((NamedTypeSpecifier) ts).getName() == null) {
                        String tn = this.getTypeName(modelName, root);
                        if (this.settings.primitiveTypeMappings.containsKey(tn)) {
                            ts = this.buildTypeSpecifier(this.settings.primitiveTypeMappings.get(this.getTypeName(modelName, root)));
                        } else {
                            ts = null;
                        }
                    }
                    return ts;
                }

                if (!this.typeInfos.containsKey(this.getTypeName(modelName, typeName))) {
                    if (!this.settings.useCQLPrimitives) {
                        List<ClassInfoElement> elements = new ArrayList<>();
                        ClassInfoElement cie = new ClassInfoElement();
                        cie.setName("value");
                        cie.setElementType("System.String");

                        elements.add(cie);

                        ClassInfo info = new ClassInfo().withName(typeName).withNamespace(modelName).withLabel(null).withBaseType(modelName + ".Element")
                                .withRetrievable(false).withElement(elements).withPrimaryCodePath(null);

                        this.typeInfos.put(this.getTypeName(modelName, typeName), info);
                    }
                    else {
                        ClassInfo info = new ClassInfo().withName(typeName).withNamespace(modelName).withBaseType("System.String")
                                .withRetrievable(false);

                        this.typeInfos.put(this.getTypeName(modelName, typeName), info);
                    }
                }

                NamedTypeSpecifier nts = new NamedTypeSpecifier();
                nts.setNamespace(modelName);
                nts.setName(typeName);

                return nts;
            }
            else {
                TypeSpecifier ts = this.buildTypeSpecifier(modelName, ed.hasType() ? ed.getType() : null);
                if (ts instanceof NamedTypeSpecifier && ((NamedTypeSpecifier) ts).getName() == null) {
                    String tn = this.getTypeName(modelName, root);
                    if (this.settings.primitiveTypeMappings.containsKey(tn)) {
                        ts = this.buildTypeSpecifier(this.settings.primitiveTypeMappings.get(this.getTypeName(modelName, root)));
                    } else {
                        ts = null;
                    }
                }
                return ts;
            }
        }
        catch (Exception e) {
            System.out.println("Error building type specifier for " + modelName + "."
                    + ed.getId() + ": " + e.getMessage());
            return null;
        }
    }

    protected String determineTarget(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier namedTypeSpecifier = (NamedTypeSpecifier)typeSpecifier;
            String qualifiedTypeName = this.getTypeName(namedTypeSpecifier);
            if (isPrimitiveMappedTypeName(namedTypeSpecifier.getNamespace(), namedTypeSpecifier.getName())) {
                return "%value.value";
            }
            else if (isMappedTypeName(namedTypeSpecifier.getNamespace(), namedTypeSpecifier.getName())) {
                switch (qualifiedTypeName) {
                    case "System.Quantity": {
                        return this.settings.helpersLibraryName + ".ToQuantity(%value)";
                    }
                    case "System.Ratio": {
                        return this.settings.helpersLibraryName + ".ToRatio(%value)";
                    }
                    case "System.Code": {
                        return this.settings.helpersLibraryName + ".ToCode(%value)";
                    }
                    case "System.Concept": {
                        return this.settings.helpersLibraryName + ".ToConcept(%value)";
                    }
                    case "Interval<System.Quantity>":
                    case "Interval<System.DateTime>": {
                        return this.settings.helpersLibraryName + ".ToInterval(%value)";
                    }
                    default: return null;
                }
            }
            else {
                if (typeTargets.containsKey(qualifiedTypeName)) {
                    return typeTargets.get(qualifiedTypeName);
                }
            }
        }
        else if (typeSpecifier instanceof ChoiceTypeSpecifier) {
            ChoiceTypeSpecifier choiceTypeSpecifier = (ChoiceTypeSpecifier)typeSpecifier;
            StringBuilder target = new StringBuilder();
            for (TypeSpecifier choice : choiceTypeSpecifier.getChoice()) {
                if (target.length() > 0) {
                    target.append(";"); // Separator for target per choice type
                }
                if (choice instanceof NamedTypeSpecifier) {
                    NamedTypeSpecifier namedChoice = (NamedTypeSpecifier)choice;
                    target.append(getTypeName(namedChoice));
                    target.append(":");
                    target.append(determineTarget(namedChoice));
                }
            }
            return target.toString();
        }

        return null;
    }

    // Builds a ClassInfoElement for the given ElementDefinition
    // This method assumes the given element is not a structure
    private ClassInfoElement buildClassInfoElement(String root, ElementDefinition ed, TypeSpecifier typeSpecifier) throws Exception {
        String target = determineTarget(typeSpecifier);

        if (ed.getContentReference() != null) {
            NamedTypeSpecifier nts = new NamedTypeSpecifier();
            nts.setName(ed.getContentReference());
            typeSpecifier = nts;
        }

        System.out.println(String.format("Building ClassInfoElement for element %s", ed.getId()));

        // If the base is different than the path, it indicates the element is a restatement (or constraint) on an element
        // defined in a base class. If the path and id are different, it indicates a slice definition, which should only
        // be returned for the core slice element.
        if (ed.hasBase() && ed.getBase().hasPath() && !ed.getBase().getPath().startsWith(root)
                || !ed.getId().equals(ed.getPath())) {
            if (ed.getSliceName() == null && !(ed.getBase() != null && ed.getPath().endsWith("[x]") && ed.getType() != null && ed.getType().size() == 1)) {
                System.out.println(String.format("Element %s is a restatement (and not a choice constraint) of a base element, no ClassInfoElement created.", ed.getId()));
                return null;
            }
        }

        if (ed.getMax() != null) {
            if ((ed.getMax().equals("*") || (this.asInteger(ed.getMax()) > 1))) {
                ListTypeSpecifier lts = new ListTypeSpecifier();
                if (typeSpecifier instanceof NamedTypeSpecifier) {
                    lts.setElementType(this.getTypeName((NamedTypeSpecifier) typeSpecifier));
                } else {
                    lts.setElementTypeSpecifier(typeSpecifier);
                }

                typeSpecifier = lts;
            }
            else if (this.asInteger(ed.getMax()) == 0) {
                System.out.println(String.format("Element %s has max cardinality 0, no ClassInfoElement created", ed.getId()));
                return null;
            }
        }

        String name = ed.getSliceName() != null
                ? ed.getSliceName()
                : stripChoice(stripPath(ed.getPath()));

        ClassInfoElement cie = new ClassInfoElement();
        cie.setName(name);
        if (target != null) {
            cie.setTarget(target);
        }

        if (typeSpecifier instanceof NamedTypeSpecifier) {
            cie.setElementType(this.getTypeName((NamedTypeSpecifier) typeSpecifier));
        }
        else {
            cie.setElementTypeSpecifier(typeSpecifier);
        }

        System.out.println(String.format("Done building ClassInfoElement %s: %s", cie.getName(), cie.toString()));

        return cie;
    }

    private boolean isNextAContinuationOfElement(String id, String path, ElementDefinition e) {
        // If the id of the element starts with the given id and the next character in the id is a '.' or ':'
        String nextId = e.getId();
        return nextId.startsWith(id) && nextId.length() > id.length() && (nextId.charAt(id.length()) == '.' || nextId.charAt(id.length()) == ':');
    }

    private ClassInfoElement mergeElements(ClassInfoElement cie, ClassInfoElement slice) {
        if (cie == null) {
            return slice;
        }

        boolean isList = cie.getElementTypeSpecifier() instanceof ListTypeSpecifier;
        cie.setTarget(slice.getTarget());
        cie.setElementType(null);
        cie.setElementTypeSpecifier(null);
        if (slice.getElementType() != null) {
            if (isList) {
                cie.setElementTypeSpecifier(new ListTypeSpecifier().withElementType(slice.getElementType()));
            }
            else {
                cie.setElementType(slice.getElementType());
            }
        }
        else if (slice.getElementTypeSpecifier() != null) {
            if (isList) {
                if (!(slice.getElementTypeSpecifier() instanceof ListTypeSpecifier)) {
                    cie.setElementTypeSpecifier(new ListTypeSpecifier().withElementTypeSpecifier(slice.getElementTypeSpecifier()));
                }
                else {
                    cie.setElementTypeSpecifier(slice.getElementTypeSpecifier());
                }
            }
            else {
                cie.setElementTypeSpecifier(slice.getElementTypeSpecifier());
            }
        }

        return cie;
    }

    // Visits the given element definition and returns a ClassInfoElement. If the
    // element is a BackboneElement
    // the visit will create an appropriate ClassInfo and record it in the TypeInfos
    // table
    // On return, index will be updated to the index of the next element to be
    // processed
    // This visit should not be used on the root element of a structure definition
    private ClassInfoElement visitElementDefinition(String modelName, String pathRoot,
            List<ElementDefinition> eds, AtomicReference<Integer> index, SliceInfo sliceInfo, SliceList slices)
            throws Exception {
        ElementDefinition ed = eds.get(index.get());

        String id = ed.getId();
        String path = ed.getPath();
        System.out.println(String.format("Visiting element %s, path %s", id, path));

        if (ed.getSlicing() != null && !ed.getSlicing().isEmpty()) {
            sliceInfo = new SliceInfo(ed, sliceInfo);
            slices.setSliceInfo(sliceInfo);
        }

        if (ed.hasSliceName()) {
            if (sliceInfo == null) {
                System.out.println(String.format("WARNING: Slice %s is not defined as part of a valid slicing", ed.getSliceName()));
            }
            else {
                sliceInfo.setSliceName(ed.getSliceName());
                System.out.println(String.format("Started slice %s of slicing root %s", sliceInfo.getSliceName(), sliceInfo.getSliceRoot().getId()));
            }
        }

        if (sliceInfo != null && !sliceInfo.isTypeSlicing() && !sliceInfo.getSliceRoot().getId().equals(ed.getId())) {
            sliceInfo.resolveSlicePath(ed);
        }

        TypeSpecifier typeSpecifier = this.buildElementTypeSpecifier(modelName, pathRoot, ed);

        String typeCode = this.typeCode(ed);
        StructureDefinition typeDefinition;
        if (this.settings.useCQLPrimitives && !this.settings.primitiveTypeMappings.containsKey(this.settings.modelName + "." + typeCode)) {
            typeDefinition = structureDefinitions.get(typeCode);
        }
        else if (!this.settings.useCQLPrimitives) {
            typeDefinition = structureDefinitions.get(typeCode);
        }
        else {
            typeDefinition = null;
        }
            
        index.set(index.get() + 1);
        List<ClassInfoElement> elements = new ArrayList<>();
        while (index.get() < eds.size()) {
            ElementDefinition e = eds.get(index.get());
            if (isNextAContinuationOfElement(id, path, e)) {
                SliceList elementSlices = new SliceList();
                ClassInfoElement cie = this.visitElementDefinition(modelName, pathRoot, eds, index, sliceInfo, elementSlices);
                if (cie != null && !(cie.getElementType() == null && cie.getElementTypeSpecifier() == null)) {
                    elements.add(cie);
                }

                for (ClassInfoElement slice : elementSlices.getSlices()) {
                    // Slices other than type slices are reported to the containing element (or class)
                    System.out.println(String.format("Adding slice %s to slice list of element %s", slice.getName(), ed.getId()));
                    if (elementSlices.getSliceInfo() != null) {
                        // The slices have been collected at the discriminator, so bubbling them past this point means they need to be qualified
                        // by the path of the current element
                        if (slice.getTarget() != null) {
                            slice.setTarget(slice.getTarget().replace("%parent.", "%parent." + stripPath(ed.getPath()) + "."));
                        }
                    }
                    slices.getSlices().add(slice);
                }
            }
            else {
                break;
            }
        }

        if (elements.size() > 0) {
            if (typeDefinition != null && (isBackboneElement(typeDefinition) || isElement(typeDefinition))) {
                String typeName = this.capitalizePath(path);

                ClassInfo componentClassInfo = new ClassInfo().withNamespace(modelName).withName(typeName).withLabel(null)
                        .withBaseType(modelName + (isBackboneElement(typeDefinition) ? ".BackboneElement" : ".Element"))
                        .withRetrievable(false).withElement(elements).withPrimaryCodePath(null);

                this.typeInfos.put(this.getTypeName(modelName, typeName), componentClassInfo);

                typeSpecifier = this.buildTypeSpecifier(modelName, typeName);
                // Elements have been added to the type of the element
                elements.clear();
            }
        }

        // If this is a slice of a backbone element and there are slices, add the slices to the constructed type for the slice
        if (ed.hasSliceName() && isBackboneElement(ed) && (slices.getSlices().size() > 0)) {
            // In this case, the type was constructed as part of the buildElementTypeSpecifier call earlier
            // If the typeSpecifier is not a constructed type as expected, allow the slices to bubble up one level
            if (typeSpecifier instanceof NamedTypeSpecifier && !isSystemTypeName(getTypeName((NamedTypeSpecifier)typeSpecifier))) {
                TypeInfo elementTypeInfo = resolveType(getTypeName((NamedTypeSpecifier)typeSpecifier));
                if (elementTypeInfo instanceof ClassInfo) {
                    ClassInfo elementClassInfo = (ClassInfo)elementTypeInfo;
                    for (ClassInfoElement slice : slices.getSlices()) {
                        System.out.println(String.format("Adding slice %s to constructed type %s.", slice.getName(), elementClassInfo.getName()));
                        elementClassInfo.getElement().add(slice);
                    }
                    slices.getSlices().clear();
                }
            }
        }

        ClassInfoElement cie = buildClassInfoElement(pathRoot, ed, typeSpecifier);

        // If this element is a slice, set the target map for the element
        if (ed.hasSliceName() && sliceInfo != null) {
            if (elements.size() == 1) {
                System.out.println(String.format("Element %s is the only element for slice %s, collapsing into the slice.", elements.get(0).getName(), ed.getSliceName()));
                cie = mergeElements(cie, elements.remove(0));
            }
            else if (elements.size() > 1) {
                throw new IllegalArgumentException("Multiple slices encountered");
            }

            if (cie != null) {
                if (sliceInfo.isExtensionSlicing() && !sliceInfo.hasSliceMap()) {
                    ed.getType().get(0).getProfile().get(0);
                    sliceInfo.addSliceMap(String.format("url='%s'", typeUrl(ed)));
                }
                String targetMap = sliceInfo.getSliceMap();
                if (targetMap != null && !targetMap.isEmpty()) {
                    if (cie.getTarget() != null && !cie.getTarget().isEmpty()) {
                        targetMap = cie.getTarget().replace("%value", targetMap + (isClassType(typeSpecifier) ? ".value" : ""));
                    }
                    cie.setTarget(targetMap);
                }
            }

            if (!sliceInfo.isTypeSlicing()) {
                System.out.println(String.format("Element %s is a slice of element %s, adding to slice list.", cie.getName(), sliceInfo.getSliceRoot().getId()));
                slices.getSlices().add(cie);
                cie = null;
            }
        }

        // If this is a slice root, process the slices...
        if (sliceInfo != null && sliceInfo.getSliceRoot().getId().equals(ed.getId())) {
            if (sliceInfo.isTypeSlicing()) {
                if (elements.size() >= 1) {
                    if (elements.size() > 1) {
                        throw new IllegalArgumentException("Multiple type slices encountered");
                    }
                    System.out.println(String.format("Element %s is the only element of a type-discriminated slicing of element %s, collapsing into the element.", elements.get(0).getName(), ed.getId()));
                    cie = mergeElements(cie, elements.remove(0));
                }
            }
            else {
                // These are slices, report them to the containing element as sub-elements
                slices.getSlices().addAll(elements);
                elements.clear();
            }
        }

        // If this is a not a slice and not a slicing root, but there are slices (slices.size() > 0)
        if (!ed.hasSliceName() && !(sliceInfo != null && sliceInfo.getSliceRoot().getId().equals(ed.getId())) && (slices.getSlices().size() > 0)) {

            if (!(typeSpecifier instanceof NamedTypeSpecifier)) {
                throw new IllegalArgumentException("Derived type for slicing support only supported for named types");
            }

            if (!(isClassType(getTypeName((NamedTypeSpecifier)typeSpecifier)))) {
                throw new IllegalArgumentException("Derived type for slicing support on primitives not implemented");
            }

            // The element should be represented as a type with elements for the slices
            // Construct an element-specific type that is derived from the type of the current element
            // Add the slices to the elements of the new type
            // However, constructing derived types based on System data types is cumbersome and confusing
            // So if the element is of a system type, allow the slices to bubble up one level
            if (!(isSystemTypeName(getTypeName((NamedTypeSpecifier)typeSpecifier)))) {
                String typeName = capitalizePath(ed.getId().replace(':', '.'));
                String qualifiedTypeName = getTypeName(modelName, typeName);
                ClassInfo elementType = null;
                if (this.typeInfos.containsKey(qualifiedTypeName)) {
                    System.out.println(String.format("WARNING: Adding slices to existing type %s.", qualifiedTypeName));
                    elementType = (ClassInfo)typeInfos.get(qualifiedTypeName);
                }
                else {
                    elementType = new ClassInfo().withNamespace(modelName).withName(typeName).withBaseTypeSpecifier(typeSpecifier).withRetrievable(false);
                    this.typeInfos.put(qualifiedTypeName, elementType);
                }

                for (ClassInfoElement slice : slices.getSlices()) {
                    System.out.println(String.format("Adding slice %s to derived type %s", slice.getName(), qualifiedTypeName));
                    if (elementType.getElement().stream().noneMatch(x -> x.getName().equals(slice.getName()))) {
                        elementType.getElement().add(slice);
                    }
                    else {
                        System.out.println(String.format("WARNING: Duplicate element %s not added to derived type %s", slice.getName(), qualifiedTypeName));
                    }
                }
                //this.typeInfos.put(qualifiedTypeName, elementType);
                slices.getSlices().clear();

                NamedTypeSpecifier nts = new NamedTypeSpecifier();
                nts.setNamespace(modelName);
                nts.setName(typeName);

                typeSpecifier = nts;

                if (cie == null) {
                    cie = new ClassInfoElement().withName(stripPath(stripChoice(ed.getPath()))).withElementType(getTypeName(nts));
                }
                else {
                    cie.setElementType(getTypeName(nts));
                    cie.setElementTypeSpecifier(null);
                }
            }
        }

        for (ClassInfoElement slice : elements) {
            System.out.println(String.format("WARNING: Element %s ignored", slice.getName()));
        }

        return cie;
    }

    // Given a StructureDefinition, creates a ClassInfo to represent it
    // This approach uses the base type to guide the walk, which requires navigating
    // the derived profiles
    private ClassInfo buildClassInfo(String modelName, StructureDefinition sd) throws Exception {
        if (modelName == null) {
            modelName = this.resolveModelName(sd.getUrl());
        }
        String typeName = getTypeName(sd);
        String qualifiedTypeName = getTypeName(modelName, typeName);
        System.out.println("Building ClassInfo for " + typeName);
        ClassInfo info = new ClassInfo().withName(typeName).withNamespace(modelName).withLabel(this.getLabel(sd))
                .withIdentifier(sd.getUrl())
                .withRetrievable(sd.getKind() == StructureDefinitionKind.RESOURCE);

        this.typeInfos.put(qualifiedTypeName, info);

        AtomicReference<Integer> index = new AtomicReference<Integer>(1);
        List<ClassInfoElement> elements = new ArrayList<>();
        String path = sd.getType(); // Type is used to navigate the elements, regardless of the baseDefinition
        String id = path; // Id starts with the Type
        List<ElementDefinition> eds = sd.getSnapshot().getElement();
        SliceList elementSlices = null;

        while (index.get() < eds.size()) {
            ElementDefinition e = eds.get(index.get());
            if (isNextAContinuationOfElement(id, path, e)) {
                elementSlices = new SliceList();
                ClassInfoElement cie = this.visitElementDefinition(modelName, path, eds, index, null, elementSlices);
                if (cie != null && !(cie.getElementType() == null && cie.getElementTypeSpecifier() == null)) {
                    elements.add(cie);
                }

                // Slices of the element, if any
                for (ClassInfoElement slice : elementSlices.getSlices()) {
                    System.out.println(String.format("Adding slice %s to elements for %s", slice.getName(), typeName));
                    elements.add(slice);
                }
            }
            else {
                break;
            }
        }

        String baseDefinition = sd.getBaseDefinition();
        String baseTypeName = sd.getKind() == StructureDefinitionKind.RESOURCE
                ? resolveBaseTypeName(sd.getType()) : resolveTypeName(baseDefinition);

        if (baseTypeName != null && !this.typeInfos.containsKey(baseTypeName)) {
            StructureDefinition baseSd = this.structureDefinitions.get(unQualify(baseTypeName));
            buildClassInfo(modelName, baseSd);
        }

        if (baseTypeName != null && unQualify(baseTypeName).equals("Extension") && elements.size() == 2
                && elements.get(0).getName().equals("url") && elements.get(1).getName().startsWith("value")) {
            if (elements.get(1).getElementType() != null) {
                info.setBaseType(elements.get(1).getElementType());
            }
            else {
                info.setBaseTypeSpecifier(elements.get(1).getElementTypeSpecifier());
            }

            // Set up target type map
            typeTargets.put(qualifiedTypeName, elements.get(1).getTarget());
        }
        else {
            // Set base type, elements, and primary code path
            info.withBaseType(baseTypeName)
                    .withElement(elements)
                    .withPrimaryCodePath(this.primaryCodePath(elements, typeName));
        }

        System.out.println(String.format("Done building ClassInfo for %s", typeName));
        return info;
    }

    private StructureDefinition getBaseDefinitionStructureDef(String model, StructureDefinition sd) {
        String baseSd = (sd.getBaseDefinition() == null) ? null : getTail(sd.getBaseDefinition());
        if (baseSd != null && !baseSd.equals("ElementDefinition") && !baseSd.equals("Element")
                && !baseSd.equals("BackboneElement") && !baseSd.equals("Resource") && !baseSd.equals("DomainResource")
                && !this.settings.primitiveTypeMappings.containsKey(model + "." + baseSd)
                && this.settings.useCQLPrimitives) {
            return getBaseDefinitionStructureDef(model, structureDefinitions.get(baseSd));
        }
        else {
            return sd;
        }
    }

    protected void buildFor(String model, String id) {
        try {
            this.buildClassInfo(model, structureDefinitions.get(id));
        }
        catch (Exception e) {
            System.out.println("Error building ClassInfo for: " + id + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void buildFor(String model, Predicate<StructureDefinition> predicate) {
        for (StructureDefinition sd : structureDefinitions.values()) {
            if (predicate.test(sd)) {
                try {
                    this.buildClassInfo(model, sd);
                }
                catch (Exception e) {
                    System.out.println("Error building ClassInfo for: " + sd.getId() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public Map<String, TypeInfo> getTypeInfos() {
        return this.typeInfos;
    }

    protected TypeSpecifier resolveContentReference(String modelName, String path) throws Exception {
        String strippedPath = this.stripRoot(path, "#");
        if (strippedPath.contains(":")) {
            String[] paths = strippedPath.split(":");
            if (paths.length != 2) {
                throw new IllegalArgumentException("Could not resolve content reference for " + path);
            }

            strippedPath = getQualifier(paths[0]) + "." + paths[1];
        }

        // TODO: This won't work for deep references....
        String root = this.getQualifier(strippedPath);
        String elementPath = this.unQualify(strippedPath);

        TypeInfo rootType = this.resolveType(modelName + "." + root);
        ClassInfoElement element = this.forPath(((ClassInfo) rootType).getElement(), elementPath);
        return this.getTypeSpecifier(element);
    }

    private Boolean isContentReferenceTypeSpecifier(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier) typeSpecifier;
            return nts.getName().startsWith("#");
        }
        else if (typeSpecifier instanceof ListTypeSpecifier) {
            ListTypeSpecifier lts = (ListTypeSpecifier) typeSpecifier;
            if (lts.getElementType().startsWith("#")) {
                return true;
            }
            else if (lts.getElementTypeSpecifier() != null) {
                return this.isContentReferenceTypeSpecifier(lts.getElementTypeSpecifier());
            }
        }

        return false;
    }

    private String getContentReference(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier) typeSpecifier;
            if (nts.getName().startsWith("#")) {
                return nts.getName();
            }
        }
        else if (typeSpecifier instanceof ListTypeSpecifier) {
            ListTypeSpecifier lts = (ListTypeSpecifier) typeSpecifier;
            if (lts.getElementType().startsWith("#")) {
                return lts.getElementType();
            }
            else if (lts.getElementTypeSpecifier() != null) {
                return this.getContentReference(lts.getElementTypeSpecifier());
            }
        }

        return null;
    }

    protected Boolean hasContentReferenceTypeSpecifier(ClassInfoElement element) {
        try {
            return element.getElementType() != null && element.getElementType().startsWith("#")
                    || this.isContentReferenceTypeSpecifier(element.getElementTypeSpecifier());
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    protected ClassInfoElement fixupContentReferenceSpecifier(String modelName, ClassInfoElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element is null");
        }
        ClassInfoElement result = null;
        try {
            if (this.hasContentReferenceTypeSpecifier(element)) {
                result = element;
                if (element.getElementType() != null && element.getElementType().startsWith("#")) {
                    element.setElementType(this.getTypeName(
                            (NamedTypeSpecifier) this.resolveContentReference(modelName, element.getElementType())));
                }
                else if (element.getElementTypeSpecifier() != null
                        && element.getElementTypeSpecifier() instanceof ListTypeSpecifier) {
                    ListTypeSpecifier lts = new ListTypeSpecifier();
                    lts.setElementTypeSpecifier(this.resolveContentReference(modelName,
                            this.getContentReference(element.getElementTypeSpecifier())));

                    element.setElementTypeSpecifier(lts);
                }
                else if (element.getElementTypeSpecifier() != null) {
                    element.setElementTypeSpecifier(this.resolveContentReference(modelName,
                            this.getContentReference(element.getElementTypeSpecifier())));
                }
                else {
                    return element;
                }
            }
        }
        catch (Exception e) {
            System.out.println(String.format("Error fixing up contentreferencetypespecifier %s.%s: %s", modelName, element.getName(), e.getMessage()));
            e.printStackTrace();
        }

        return result;
    }
}