package org.opencds.cqf.tooling.operation.ig;

import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CqlVersionManager {
    private String cqlBinaryPath;
    private final Pattern VERSION_PATTERN = Pattern.compile("library\\s+(\")*(.*)(\")*\\s+version\\s+'(.*)'");
    private final Pattern INCLUDE_PATTERN =  Pattern.compile("include\\s+(\")*(.*)(\")*\\s+version\\s+'(.*)'");

    public CqlVersionManager(String cqlBinaryPath) {
        this.cqlBinaryPath = cqlBinaryPath;
    }

    public void updateVersion(String libraryName, String version) {

    }

    public void updateAllVersion(String version) {
        List<String> updatedLibraryNames = new ArrayList<>();
        for (var cqlFile: IOUtils.getFilePaths(cqlBinaryPath, false)) {
            String cqlContent = IOUtils.getCqlString(cqlFile);
            Matcher matcher = VERSION_PATTERN.matcher(cqlContent);
            if (matcher.find() && matcher.groupCount() >= 4) {
                String libraryLine = matcher.group();
                boolean quotedIdentifier = libraryLine.contains("\"");
                String identifier = matcher.group(2).replace("\"", "");
                String oldVersion = matcher.group(4);
                if (!oldVersion.equals(version)) {
                    updatedLibraryNames.add(identifier);
                    String newLibraryLine = String.format("library %s version '%s'", quotedIdentifier ? "\"" + identifier + "\"" : identifier, version);
                    cqlContent = cqlContent.replace(libraryLine, newLibraryLine);
                    // write file
                }
            } else {
                // log warning
            }
        }
        for (var cqlFile: IOUtils.getFilePaths(cqlBinaryPath, false)) {
            String cqlContent = IOUtils.getCqlString(cqlFile);
            Matcher matcher = INCLUDE_PATTERN.matcher(cqlContent);
            while (matcher.find() && matcher.groupCount() >= 4) {
                String includeLine = matcher.group();
                boolean quotedIdentifier = includeLine.contains("\"");
                String identifier = matcher.group(2).replace("\"", "");
                if (updatedLibraryNames.contains(identifier)) {
                    String newIncludeLine = String.format("include %s version '%s'", quotedIdentifier ? "\"" + identifier + "\"" : identifier, version);
                    cqlContent = cqlContent.replace(includeLine, newIncludeLine);
                    // write file
                }
            }
        }
    }
}
