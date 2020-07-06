package org.opencds.cqf.modelinfo;

import org.hl7.elm_modelinfo.r1.ClassInfoElement;

import java.util.ArrayList;
import java.util.List;

public class SliceList {
    public SliceList() {
        this.slices = new ArrayList<ClassInfoElement>();
    }

    private SliceInfo sliceInfo;
    public SliceInfo getSliceInfo() {
        return sliceInfo;
    }
    public void setSliceInfo(SliceInfo sliceInfo) {
        this.sliceInfo = sliceInfo;
    }

    private List<ClassInfoElement> slices;
    public List<ClassInfoElement> getSlices() {
        return this.slices;
    }
}

