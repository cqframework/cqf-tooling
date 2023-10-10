package org.opencds.cqf.tooling.utilities;

import org.opencds.cqf.tooling.exception.InvalidIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.regex.Pattern;

public class IDUtils {

    private static final Logger logger = LoggerFactory.getLogger(IDUtils.class);

    // regex defined https://www.hl7.org/fhir/datatypes.html#id
    private static final String regex = "[A-Za-z0-9\\-\\.]{1,64}";

    private static Pattern idPattern;

    private static Pattern getIdPattern() {
        if(idPattern == null) {
            idPattern = Pattern.compile(regex);
        }
        return idPattern;
    }

    // validateId checks that the provided id matches the defined regex pattern.
    // throws an exception if not.
    public static void validateId(String id){
        if(!getIdPattern().matcher(id).find()) {
            logger.error("Provided id: {} does not match specified regex: {}", id, regex);
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