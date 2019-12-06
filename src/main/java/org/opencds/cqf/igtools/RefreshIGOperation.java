package org.opencds.cqf.igtools;

import org.opencds.cqf.Operation;
import org.opencds.cqf.igtools.IGProcessor;

public class RefreshIGOperation extends Operation {

    private String igPath;
    private Boolean includeELM = false;
    private Boolean includeDependencies = false;
    private Boolean includeTerminology = false;
    private Boolean includeTestCasts = false;

    public RefreshIGOperation() { 
   
    } 

    @Override
    public void execute(String[] args) {
        parseArgs(args);
        IGProcessor.refreshIG(igPath, includeELM,  includeDependencies, includeTerminology, includeTestCasts);
    }

    private void parseArgs(String[] args) {
        for (String arg : args) {       
            if (arg.equals("-RefreshIg")) continue;

            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];
    
            switch (flag.replace("-", "").toLowerCase()) {
                case "path": case "p": igPath = value; break;
                case "includeELM": case "ie": includeELM = Boolean.parseBoolean(value); break;
                case "includeDependencies": case "id": includeDependencies = Boolean.parseBoolean(value); break;
                case "includeTerminology": case "it": includeTerminology = Boolean.parseBoolean(value); break;
                case "includeTestCases": case "itc": includeTestCasts = Boolean.parseBoolean(value); break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
    
        }
        if (igPath == null) {
            throw new IllegalArgumentException("The path to the IG is required");
        }
    }
}

