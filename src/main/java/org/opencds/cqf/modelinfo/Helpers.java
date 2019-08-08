package org.opencds.cqf.modelinfo;

public class Helpers {
    public static String unQualify(String name) {
        int index = name.indexOf(".");
        if (index > 0) {
            return name.substring(index + 1);
        }

        return null;
    }
}