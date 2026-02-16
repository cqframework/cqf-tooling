package org.opencds.cqf.tooling.operation.ig;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CqlRefresh extends Refresh {

    private static final Logger logger = LoggerFactory.getLogger(CqlRefresh.class);
    private final Pattern VERSION_PATTERN = Pattern.compile("^(library\\s+(\\S+)\\s+version\\s+)'[0-9]+\\.[0-9]+\\.[0-9]+'");
    private final Pattern INCLUDE_PATTERN = Pattern.compile("^(include\\s+(\\S+)\\s+version\\s+)'([0-9]+\\.[0-9]+\\.[0-9]+)'(\\s+called\\s+(\\S+))?");

    public CqlRefresh(IGInfo igInfo) {
        super(igInfo);
    }

    @Override
    public List<IBaseResource> refresh() {
        return List.of();
    }

    public void refreshCql(IGInfo igInfo, RefreshIGParameters params) {
        Map<String, String> updatedLibraries = refreshCqlFile(igInfo.getCqlBinaryPath(), params.updatedVersion);
        updateCqlReferences(igInfo.getCqlBinaryPath(), updatedLibraries);
    }

    private Map<String, String> refreshCqlFile(String cqlBinaryPath, String updatedVersion) {
        Map<String, String> updatedLibraries = new HashMap<>();
        try (Stream<Path> paths = Files.walk(Paths.get(cqlBinaryPath))) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".cql"))
                    .collect(Collectors.toList());

            for (Path file : files) {
                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++) {
                    Matcher matcher = VERSION_PATTERN.matcher(lines.get(i));
                    if (matcher.matches()) {
                        String libraryName = matcher.group(2);
                        String updatedLine = matcher.replaceFirst("$1'" + updatedVersion + "'");
                        lines.set(i, updatedLine);
                        Files.write(file, lines);
                        updatedLibraries.put(libraryName, updatedVersion);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error updating cql files: {}", e.getMessage());
        }
        return updatedLibraries;
    }

    private void updateCqlReferences(String cqlBinaryPath, Map<String, String> updatedLibraries) {
        try (Stream<Path> paths = Files.walk(Paths.get(cqlBinaryPath))) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".cql"))
                    .collect(Collectors.toList());

            for (Path file : files) {
                List<String> lines = Files.readAllLines(file);
                boolean fileUpdated = false;
                for (int i = 0; i < lines.size(); i++) {
                    Matcher matcher = INCLUDE_PATTERN.matcher(lines.get(i));
                    while (matcher.find()) {
                        String libraryName = matcher.group(2);
                        String newVersion = updatedLibraries.get(libraryName);
                        if (newVersion != null && !matcher.group(3).equals(newVersion)) {
                            String calledPart = matcher.group(4) != null ? matcher.group(4) : "";
                            String newLine = matcher.replaceFirst("$1'" + newVersion + "'" + calledPart);
                            lines.set(i, newLine);
                            fileUpdated = true;
                        }
                    }
                }
                if (fileUpdated) {
                    Files.write(file, lines);
                }
            }
        } catch (IOException e) {
            logger.error("Error updating CQL references: {}", e.getMessage());
        }
    }

}
