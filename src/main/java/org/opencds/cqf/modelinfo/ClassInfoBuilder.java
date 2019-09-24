package org.opencds.cqf.modelinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hl7.elm_modelinfo.r1.ChoiceTypeSpecifier;
import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.ClassInfoElement;
import org.hl7.elm_modelinfo.r1.ListTypeSpecifier;
import org.hl7.elm_modelinfo.r1.NamedTypeSpecifier;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.hl7.elm_modelinfo.r1.TypeSpecifier;
import org.hl7.fhir.dstu3.model.Element;
import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.dstu3.model.Enumerations.BindingStrength;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionKind;

public abstract class ClassInfoBuilder {
    protected Map<String, StructureDefinition> structureDefinitions;
    protected Map<String, TypeInfo> typeInfos = new HashMap<String, TypeInfo>();
    protected ClassInfoSettings settings;

    public ClassInfoBuilder(ClassInfoSettings settings, Map<String, StructureDefinition> structureDefinitions) {
        this.structureDefinitions = structureDefinitions;
        this.settings = settings;
    }

    protected abstract void innerBuild();

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
            return getTypeName(modelName, getTail(url));
        }

        return null;
    }

    private String getTypeName(String modelName, String typeName) {
        return modelName != null ? modelName + "." + typeName : typeName;
    }

    private String getTypeName(NamedTypeSpecifier typeSpecifier) {
        return this.getTypeName(typeSpecifier.getModelName(), typeSpecifier.getName());
    }

    private TypeInfo resolveType(String name) {
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
        if (classInfoElement.getType() != null) {
            return this.resolveType(classInfoElement.getType());
        } else {
            return this.resolveType(classInfoElement.getTypeSpecifier());
        }
    }

    protected String getQualifier(String name) {
        if (name == null)
        {
            return null;
        }
        int index = name.indexOf(".");
        if (index > 0) {
            return name.substring(0, index);
        }

        return null;
    }

    protected String unQualify(String name) {
        if(name == null)
        {
            return null;
        }
        int index = name.indexOf(".");
        if (index > 0) {
            return name.substring(index + 1);
        }

        return null;
    }

    private TypeSpecifier buildTypeSpecifier(String modelName, String typeName) {
        NamedTypeSpecifier ts = new NamedTypeSpecifier();
        ts.setModelName(modelName);
        ts.setName(typeName);

        return ts;
    }

    private TypeSpecifier buildTypeSpecifier(String typeName) {
        return this.buildTypeSpecifier(this.getQualifier(typeName), this.unQualify(typeName));
    }

    // Builds a TypeSpecifier from the given list of TypeRefComponents
    private TypeSpecifier buildTypeSpecifier(String modelName, TypeRefComponent typeRef) {
        try {
            if (typeRef != null && typeRef.getProfile() != null) {
                return this.buildTypeSpecifier(this.resolveTypeName(typeRef.getProfile()));
            } else {

                if (this.settings.useCQLPrimitives && typeRef != null) {
                    String typeName = this.settings.cqlTypeMappings.get(modelName + "." + typeRef.getCode());
                    return this.buildTypeSpecifier(typeName);
                } else {
                    return this.buildTypeSpecifier(modelName,
                            typeRef != null && typeRef.hasCode() ? typeRef.getCode() : null);
                }
            }
        } catch (Exception e) {
            System.out.println("Error building type specifier for " + modelName + "."
                    + (typeRef != null ? typeRef.getCode() : "<No Type>") + ": " + e.getMessage());
            return null;
        }
    }

    private TypeSpecifier buildTypeSpecifier(String modelName, List<TypeRefComponent> typeReferencRefComponents)
            throws Exception {
        List<TypeSpecifier> specifiers = typeReferencRefComponents.stream()
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

    // Gets the type specifier for the given class info element
    private TypeSpecifier getTypeSpecifier(ClassInfoElement classInfoElement) {
        if (classInfoElement.getType() != null) {
            return this.buildTypeSpecifier(this.getQualifier(classInfoElement.getType()),
                    this.unQualify(classInfoElement.getType()));
        } else if (classInfoElement.getTypeSpecifier() != null) {
            if (classInfoElement.getTypeSpecifier() instanceof ListTypeSpecifier) {
                ListTypeSpecifier lts = (ListTypeSpecifier) classInfoElement.getTypeSpecifier();
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

    // Returns the given string with the first letter capitalized
    private String capitalize(String name) {
        if (name.length() >= 1) {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }

        return name;
    }

    // Returns the given path with the first letter of every path capitalized
    private String captitalizePath(String path) {
        return String.join(".",
                Arrays.asList(path.split(".")).stream().map(x -> this.capitalize(x)).collect(Collectors.toList()));
    }

    // Returns the name of the component type used to represent anonymous nested
    // structures
    private String getComponentTypeName(String path) {
        return this.captitalizePath(path) + "Component";
    }

    // Strips the given root from the given path.
    // Throws an error if the path does not start with the root.
    private String stripRoot(String path, String root) throws Exception {
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

    // Strips the [x] suffix of an element name which indicates a choice in FHIR
    private String stripChoice(String name) {
        int index = name.indexOf("[x]");
        if (index != -1) {
            return name.substring(0, index);
        }

        return name;
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

    // Returns true if the ElementDefinition describes a constraint only
    // For now, since constraints cannot be represented in ModelInfo, no element
    // will be created for these
    // For now, we are assuming that if the element specifies a type, it should be
    // declared
    // This may be a type constraint, which can be expressed in ModelInfo, so that's
    // okay, but if all
    // the element is changing is the binding or cardinality, no element will be
    // produced.
    private Boolean isConstraintElement(ElementDefinition ed) {
        if (ed.getType() != null && ed.getType().size() > 0) {
            return true;
        }

        return false;
    }

    // Returns true if the ElementDefinition describes a Backbone Element
    private Boolean isBackboneElement(ElementDefinition ed) {
        return ed.getType() != null && ed.getType().size() == 1
                && ed.getType().get(0).getCode().equals("BackboneElement");
    }

    // Returns true if the ElementDefinition describes an Extension
    private Boolean isExtension(ElementDefinition ed) {
        return ed.getType() != null && ed.getType().size() == 1 && ed.getType().get(0).getCode().equals("Extension");
    }

    // Returns the type code for the element if there is only one type ref
    private String typeCode(ElementDefinition ed) {
        if (ed.getType() != null && ed.getType().size() == 1) {
            return ed.getType().get(0).getCode();
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

    // Returns the element with the given path
    private ClassInfoElement forPath(List<ClassInfoElement> elements, String path) {
        ClassInfoElement result = null;
        String[] segments = path.split(".");
        for (String p : segments) {
            result = element(elements, p);
            if (result != null) {
                TypeInfo elementType = resolveType(result);
                elements = ((ClassInfo) elementType).getElement();
            }
        }

        return result;
    }

    private TypeSpecifier resolveContentReference(String modelName, String path) throws Exception {
        String root = this.getQualifier(this.stripRoot(path, "#"));
        String elementPath = this.unQualify(this.stripRoot(path, "#"));

        TypeInfo rootType = this.resolveType(modelName + "." + root);
        ClassInfoElement element = this.forPath(((ClassInfo) rootType).getElement(), elementPath);
        return this.getTypeSpecifier(element);
    }

    private Boolean isContentReferenceTypeSpecifier(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier) typeSpecifier;
            return nts.getName().startsWith("#");
        } else if (typeSpecifier instanceof ListTypeSpecifier) {
            ListTypeSpecifier lts = (ListTypeSpecifier) typeSpecifier;
            if (lts.getElementType().startsWith("#")) {
                return true;
            } else if (lts.getElementTypeSpecifier() != null) {
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
        } else if (typeSpecifier instanceof ListTypeSpecifier) {
            ListTypeSpecifier lts = (ListTypeSpecifier) typeSpecifier;
            if (lts.getElementType().startsWith("#")) {
                return lts.getElementType();
            } else if (lts.getElementTypeSpecifier() != null) {
                return this.getContentReference(lts.getElementTypeSpecifier());
            }
        }

        return null;
    }

    private Boolean hasContentReferenceTypeSpecifier(ClassInfoElement element) {

        return element.getType().startsWith("#") || this.isContentReferenceTypeSpecifier(element.getTypeSpecifier());
    }

    private ClassInfoElement fixupContentRefererenceSpecifier(String modelName, ClassInfoElement element) {
        ClassInfoElement result = null;
        try {
            if (this.hasContentReferenceTypeSpecifier(element)) {
                result = element;
                if (element.getType().startsWith("#")) {
                    element.setType(this.getTypeName(
                            (NamedTypeSpecifier) this.resolveContentReference(modelName, element.getType())));
                } else if (element.getTypeSpecifier() instanceof ListTypeSpecifier) {
                    ListTypeSpecifier lts = new ListTypeSpecifier();
                    lts.setElementTypeSpecifier(this.resolveContentReference(modelName,
                            this.getContentReference(element.getTypeSpecifier())));

                    element.setTypeSpecifier(lts);
                } else {
                    element.setTypeSpecifier(this.resolveContentReference(modelName,
                            this.getContentReference(element.getTypeSpecifier())));
                }
            }
        } catch (Exception e) {
            System.out.println("Error fixing up contentreferencetypespecifier: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    // Builds a ClassInfoElement for the given ElementDefinition
    // This method assumes the given element is not a structure
    private ClassInfoElement buildClassInfoElement(String root, ElementDefinition ed, ElementDefinition structureEd,
            TypeSpecifier typeSpecifier) throws Exception {
        if (ed.getContentReference() != null) {
            NamedTypeSpecifier nts = new NamedTypeSpecifier();
            nts.setName(ed.getContentReference());
            typeSpecifier = nts;
        }

        // TODO: These code paths look identical to me...
        if (structureEd == null) {
            if (ed.getMax() != null && (ed.getMax().equals("*") || (this.asInteger(ed.getMax()) > 1))) {
                ListTypeSpecifier lts = new ListTypeSpecifier();
                if (typeSpecifier instanceof NamedTypeSpecifier) {
                    lts.setElementType(this.getTypeName((NamedTypeSpecifier) typeSpecifier));
                } else {
                    lts.setElementTypeSpecifier(typeSpecifier);
                }

                typeSpecifier = lts;
            }

            String name = ed.getSliceName() != null ? ed.getSliceName()
                    : this.stripChoice(this.stripRoot(ed.getPath(), root));

            ClassInfoElement cie = new ClassInfoElement();
            cie.setName(name);
            if (typeSpecifier instanceof NamedTypeSpecifier) {
                cie.setType(this.getTypeName((NamedTypeSpecifier) typeSpecifier));
            } else {
                cie.setTypeSpecifier(typeSpecifier);
            }

            return cie;
        } else {
            if (ed.getMax() != null && (ed.getMax().equals("*") || (this.asInteger(ed.getMax()) > 1))) {
                ListTypeSpecifier lts = new ListTypeSpecifier();
                if (typeSpecifier instanceof NamedTypeSpecifier) {
                    lts.setElementType(this.getTypeName((NamedTypeSpecifier) typeSpecifier));
                } else {
                    lts.setElementTypeSpecifier(typeSpecifier);
                }

                typeSpecifier = lts;
            }

            String name = ed.getSliceName() != null ? ed.getSliceName()
                    : this.stripChoice(this.stripRoot(ed.getPath(), root));

            ClassInfoElement cie = new ClassInfoElement();
            cie.setName(name);
            if (typeSpecifier instanceof NamedTypeSpecifier) {
                cie.setType(this.getTypeName((NamedTypeSpecifier) typeSpecifier));
            } else {
                cie.setTypeSpecifier(typeSpecifier);
            }

            return cie;
        }
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

    // Builds the type specifier for the given element
    private TypeSpecifier buildElementTypeSpecifier(String modelName, String root, ElementDefinition ed) {
        String typeCode = this.typeCode(ed);
        if (!this.settings.useCQLPrimitives && typeCode != null && typeCode.equals("code") && ed.hasBinding()
                && ed.getBinding().getStrength() == BindingStrength.REQUIRED) {
            String typeName = ((StringType) (this
                    .extension(ed.getBinding(), "http://hl7.org/fhir/StructureDefinition/elementdefinition-bindingName")
                    .getValue())).getValue();

            if (!this.typeInfos.containsKey(this.getTypeName(modelName, typeName))) {

                List<ClassInfoElement> elements = new ArrayList<>();
                ClassInfoElement cie = new ClassInfoElement();
                cie.setName("value");
                cie.setType("System.String");

                elements.add(cie);

                ClassInfo info = new ClassInfo().withName(typeName).withLabel(null).withBaseType(modelName + ".Element")
                        .withRetrievable(false).withElement(elements).withPrimaryCodePath(null);
                        
                this.typeInfos.put(this.getTypeName(modelName, typeName), info);

            }

            NamedTypeSpecifier nts = new NamedTypeSpecifier();
            nts.setModelName(modelName);
            nts.setName(typeName);

            return nts;
        } else {
            TypeSpecifier ts = this.buildTypeSpecifier(modelName, ed.hasType() ? ed.getType().get(0) : null);
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

    // Visits the given element definition and returns a ClassInfoElement. If the
    // element is a BackboneElement
    // the visit will create an appropriate ClassInfo and record it in the TypeInfos
    // table
    // On return, index will be updated to the index of the next element to be
    // processed
    // This visit should not be used on the root element of a structure definition
    private ClassInfoElement visitElementDefinition(String modelName, String root, List<ElementDefinition> eds,
            String structureRoot, List<ElementDefinition> structureEds, AtomicReference<Integer> index)
            throws Exception {
        ElementDefinition ed = eds.get(index.get());
        String path = ed.getPath();

        TypeSpecifier typeSpecifier = this.buildElementTypeSpecifier(modelName, root, ed);

        String typeCode = this.typeCode(ed);
        StructureDefinition typeDefinition = structureDefinitions.get(typeCode);

        List<ElementDefinition> typeEds;
        if (typeCode != null && typeCode.equals("ComplexType") && !typeDefinition.getId().equals("BackboneElement")) {
            typeEds = typeDefinition.getSnapshot().getElement();
        } else {
            typeEds = structureEds;
        }

        String typeRoot;
        if (typeCode != null && typeCode.equals("ComplexType") && !typeDefinition.getId().equals("BackboneElement")) {
            typeRoot = typeDefinition.getId();
        } else {
            typeRoot = structureRoot;
        }

        index.set(index.get() + 1);
        List<ClassInfoElement> elements = new ArrayList<>();
        while (index.get() < eds.size()) {
            ElementDefinition e = eds.get(index.get());
            if (e.getPath().startsWith(path) && !e.getPath().equals(path)) {
                ClassInfoElement cie = this.visitElementDefinition(modelName, root, eds, typeRoot, structureEds, index);
                if (cie != null) {
                    elements.add(cie);
                }

            } else {
                break;
            }
        }

        if (elements.size() > 0) {
            if (typeDefinition != null && typeDefinition.getId().equals("BackboneElement")) {
                String typeName = this.getComponentTypeName(path);

                ClassInfo componentClassInfo = new ClassInfo().withName(typeName).withLabel(null)
                        .withBaseType(modelName + ".BackboneElement").withRetrievable(false).withElement(elements)
                        .withPrimaryCodePath(null);

                this.typeInfos.put(this.getTypeName(modelName, typeName), componentClassInfo);

                typeSpecifier = this.buildTypeSpecifier(modelName, typeName);

            } else if (typeDefinition != null && typeDefinition.getId().equals("Extension")) {
                // If this is an extension, the elements will be constraints on the existing
                // elements of an extension (i.e. url and value)
                // Use the type of the value element
            } else {
                // element has children that are being ignored.
            }
        }

        ElementDefinition typeEd = this.elementForPath(typeEds, ed.getPath());

        return this.buildClassInfoElement(root, ed, typeEd, typeSpecifier);
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
                if (e.getName().toLowerCase().equals("code") && this.isCodeable(e.getType())) {
                    return e.getName();
                }
            }

        }

        return null;
    }

    // Given a StructureDefinition, creates a ClassInfo to represent it
    // This approach uses the base type to guide the walk, which requires navigating
    // the derived profiles
    private ClassInfo buildClassInfo(String modelName, StructureDefinition sd) throws Exception {
        if (modelName == null) {
            modelName = this.resolveModelName(sd.getUrl());
        }

        String typeName = sd.getId();
        AtomicReference<Integer> index = new AtomicReference<Integer>(1);
        List<ClassInfoElement> elements = new ArrayList<>();
        List<ElementDefinition> eds = sd.getSnapshot().getElement();
        String path = sd.getType(); // Type is used to navigate the elements, regardless of the baseDefinition

        StructureDefinition structure = null;
        if (!typeName.equals(path)) {
            structure = structureDefinitions.get(path);
        }

        List<ElementDefinition> structureEds = null;
        if (structure != null) {
            structureEds = structure.getSnapshot().getElement();
        }

        // int indexer = 0;
        // int edsArraySize = eds.size();
        // while (indexer < edsArraySize)
        // {
        //     if(eds.get(indexer).hasBase())
        //     {
        //         if(getQualifier(eds.get(indexer).getBase().getPath()) != null)
        //         {
        //             if(getQualifier(eds.get(indexer).getBase().getPath()).matches("Element"))
        //             {
        //                 eds.remove(eds.get(indexer));
        //                 --edsArraySize;
        //             }
        //             else ++indexer;
        //         }
        //         else ++indexer;
        //     }
            
        //     else ++indexer;
        // }

        // indexer = 0;
        // int structureEdsArraySize = structureEds.size();
        // while (indexer < structureEdsArraySize)
        // {
        //     if(structureEds.get(indexer).hasBase())
        //     {
        //         if(getQualifier(eds.get(indexer).getBase().getPath()) != null)
        //         {
        //             if(getQualifier(structureEds.get(indexer).getBase().getPath()).matches("Element"))
        //             {
        //                 structureEds.remove(structureEds.get(indexer));
        //                 --structureEdsArraySize;
        //             }
        //             else ++indexer;
        //         }
        //         else ++indexer;
        //     }
        //     else ++indexer;
        // }

        while (index.get() < eds.size()) {
            ElementDefinition e = eds.get(index.get());
            if (e.getPath().startsWith(path) && !e.getPath().equals(path)) {
                ClassInfoElement cie = this.visitElementDefinition(modelName, path, eds, structure == null? null: structure.getId(),
                        structureEds, index);
                if (cie != null) {
                    elements.add(cie);
                }

            } else {
                break;
            }
        }

        System.out.println("Building ClassInfo for " + typeName);

        ClassInfo info = new ClassInfo().withName(modelName + "." +path).withLabel(typeName)
                .withBaseType(this.resolveTypeName(sd.getBaseDefinition()))
                .withRetrievable(sd.getKind() == StructureDefinitionKind.RESOURCE).withElement(elements)
                .withPrimaryCodePath(this.primaryCodePath(elements, typeName));

        //pull this out
        this.typeInfos.merge(this.getTypeName(modelName, path), info, (v1, v2) -> {v2.setBaseType(v1.getBaseType()); return v2;});

        return info;
    }

    protected void buildFor(String model, Predicate<StructureDefinition> predicate) {
        for (StructureDefinition sd : structureDefinitions.values()) {
            if (predicate.test(sd)) {
                try {
                    this.buildClassInfo(model, sd);
                } catch (Exception e) {
                    System.out.println("Error building ClassInfo for: " + sd.getId() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public Map<String, TypeInfo> getTypeInfos() {
        return this.typeInfos;
    }

}