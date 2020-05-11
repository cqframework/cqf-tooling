package org.opencds.cqf.modelinfo;

import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.Type;

import java.util.ArrayList;
import java.util.List;

import static org.opencds.cqf.modelinfo.ClassInfoBuilder.stripPath;

/*
Tracks slices at the root of the slice list
Each slicename resets the slice map
Slice elements contribute to the slice map for discriminator paths
*/
public class SliceInfo {
    public SliceInfo(ElementDefinition sliceRoot, SliceInfo parentSliceInfo) {
        this.sliceRoot = sliceRoot;
        this.parentSliceInfo = parentSliceInfo;
    }

    private SliceInfo parentSliceInfo;

    private ElementDefinition sliceRoot;
    public ElementDefinition getSliceRoot() {
        return this.sliceRoot;
    }

    private String sliceName;
    public String getSliceName() {
        return this.sliceName;
    }
    public void setSliceName(String sliceName) {
        this.sliceName = sliceName;
        this.sliceMap = new ArrayList<String>();
    }

    private List<String> sliceMap = new ArrayList<String>();
    public String getSliceMap() throws Exception {
        if (hasSliceMap() && !isTypeSlicing()) {
            return "%" + String.format("parent.%s[%s]", stripPath(sliceRoot.getId()), String.join(",", this.sliceMap));
        }

        return null;
    }

    public boolean hasSliceMap() {
        return sliceMap.size() > 0;
    }

    public void addSliceMap(String map) {
        this.sliceMap.add(map);
    }

    private String discriminator;
    public String getDiscriminator() {
        if (discriminator == null) {
            discriminator = buildDiscriminator();
        }
        return discriminator;
    }

    private String buildDiscriminator() {
        StringBuilder builder = new StringBuilder();
        if (sliceRoot.getSlicing() != null && sliceRoot.getSlicing().hasDiscriminator()) {
            for (ElementDefinition.ElementDefinitionSlicingDiscriminatorComponent c : sliceRoot.getSlicing().getDiscriminator()) {
                if (builder.length() > 0) {
                    builder.append(',');
                }
                builder.append('%');
                builder.append(c.getType().toCode());
                if (c.hasPath()) {
                    builder.append('#');
                    builder.append(c.getPath());
                }
            }
        }
        return builder.toString();
    }

    public boolean isTypeSlicing() {
        return getDiscriminator().equals("%" + ElementDefinition.DiscriminatorType.TYPE.toCode() + "#$this");
    }

    public boolean isExtensionSlicing() {
        return sliceRoot.getPath().endsWith(".extension");
    }

    private String getValueSlicingPath(String elementName) {
        if (sliceRoot.hasSlicing() && sliceRoot.getSlicing().hasDiscriminator()) {
            for (ElementDefinition.ElementDefinitionSlicingDiscriminatorComponent c : sliceRoot.getSlicing().getDiscriminator()) {
                if (c.getType().equals(ElementDefinition.DiscriminatorType.VALUE)) {
                    if ((sliceRoot.getPath() + "." + c.getPath()).equals(elementName)) {
                        return c.getPath();
                    }
                }
            }
        }

        return null;
    }

    public void resolveSlicePath(ElementDefinition ed) {
        String valueSlicingPath = getValueSlicingPath(ed.getPath());
        if (valueSlicingPath != null && sliceName != null) {
            if (ed.getFixed() == null) {
                throw new IllegalArgumentException("getFixed() is null");
            }
            Type type = ed.getFixed();
            if (!type.isPrimitive()) {
                throw new IllegalArgumentException(String.format("Value slicing on type %s is not supported for slicing of %s",
                        type.fhirType(), this.sliceRoot.getId()));
            }
            sliceMap.add(String.format("%s='%s'", valueSlicingPath, type.primitiveValue()));
        }

        // Because of the possibility of slicing under slicing, child slicings must report slice paths up the chain
        if (parentSliceInfo != null) {
            parentSliceInfo.resolveSlicePath(ed);
        }
    }
}

