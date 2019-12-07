package org.opencds.cqf.utilities;

public class ArgUtils 
{    
    //TODO: implement abbreviated arg name
    public static String getValue(String arg, String[]args){
        return getValue(arg, args, false);
    }

    public static String getValue(String arg, String[]args, Boolean required){
        for (String entry : args) {  
            String[] flagAndValue = entry.split("=");
            if (flagAndValue.length == 2) {       
                String flag = flagAndValue[0];
                String value = flagAndValue[1];     
                if (flag.toLowerCase().equals("-" + arg.toLowerCase())) {
                    return value;    
                }
            }
        }
        if (required) {
            throw new IllegalArgumentException("Argument required: " + arg);
        }
        return "";
    }

    public static Boolean isTrue(String arg, String[] args) {
        return exists(arg, args);
    }

    public static Boolean isTrue(String arg, String[] args, Boolean required) {
        return exists(arg, args, required);
    }

    public static Boolean exists(String arg, String[] args) {
        return exists(arg, args, false);
    }

    public static Boolean exists(String arg, String[] args, Boolean required) {
        for (String entry : args) {       
            if (entry.toLowerCase().equals("-" + arg.toLowerCase())) {
                return true;    
            }
        }
        if (required) {
            throw new IllegalArgumentException("Argument required: " + arg);
        }
        return false;
    }

    public static void ensure(String arg, String[] args) {
        exists(arg, args, true);
    }
}
