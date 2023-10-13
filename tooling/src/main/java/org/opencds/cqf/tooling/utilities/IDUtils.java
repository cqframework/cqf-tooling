package org.opencds.cqf.tooling.utilities;

import org.opencds.cqf.tooling.exception.InvalidIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class IDUtils {

    private static final Logger logger = LoggerFactory.getLogger(IDUtils.class);

    // regex defined https://www.hl7.org/fhir/datatypes.html#id
    private static final String fhirRegex = "[A-Za-z0-9\\-\\.]{1,64}";

    private static Pattern idPattern;

    private static Pattern getIdPattern() {
        if(idPattern == null) {
            idPattern = Pattern.compile(fhirRegex);
        }
        return idPattern;
    }

    // validateId checks that the provided id matches the fhir defined regex pattern & contains letters to satisfy
    // requirements for HAPI server. An InvalidIdException is thrown if these conditions are not met.
    public static void validateId(String id){
        if (!getIdPattern().matcher(id).find() || !id.matches(".*[a-zA-z]+.*") || id.length() > 64) {
            logger.error("Provided id: {} is not an alphanumeric string matching regex: {}", id, fhirRegex);
            throw new InvalidIdException("The provided id does not meet the constraints.");
        }
    }

    public static String toId(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        if (name.endsWith(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }

        name = name.toLowerCase().trim()
                // remove these characters
                .replace("(", "").replace(")", "").replace("[", "").replace("]", "").replace("\n", "")
                // replace these with ndash
                .replace(":", "-")
                .replace(",", "-")
                .replace("_", "-")
                .replace("/", "-")
                .replace(" ", "-")
                .replace(".", "-")
                // remove multiple ndash
                .replace("----", "-").replace("---", "-").replace("--", "-").replace(">", "greater-than")
                .replace("<", "less-than");

        validateId(name);
        return name;
    }

    public static String toUpperId(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        if (name.endsWith(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }

        name = name.trim()
                // remove these characters
                .replace("(", "").replace(")", "").replace("[", "").replace("]", "").replace("\n", "")
                .replace(":", "")
                .replace(",", "")
                .replace("_", "")
                .replace("/", "")
                .replace(" ", "")
                .replace(".", "")
                .replace("-", "")
                .replace(">", "")
                .replace("<", "");

        validateId(name);
        return name;
    }

    public static String libraryNameToId(String name, String version) {
        String nameAndVersion = "library-" + name + "-" + version;
        nameAndVersion = nameAndVersion.replace("_", "-");
        validateId(nameAndVersion);
        return nameAndVersion;
    }
}