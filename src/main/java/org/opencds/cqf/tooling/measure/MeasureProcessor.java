package org.opencds.cqf.tooling.measure;

public class MeasureProcessor
{      
    public static final String ResourcePrefix = "measure-";   
    public static String getId(String baseId) {
        return ResourcePrefix + baseId;
    }
}
