package org.opencds.cqf.tooling.utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.r5.model.Extension;
import org.opencds.cqf.tooling.utilities.constants.CqfConstants;
import org.opencds.cqf.tooling.utilities.constants.CqfmConstants;

public class LogicDefinitionUtils {

    private LogicDefinitionUtils() {
    }

    public static String getLogicDefinitionKey(Extension logicDefinition) {
        String libraryName = null;
        String name = null;
        for (Extension sub : logicDefinition.getExtension()) {
            if ("libraryName".equals(sub.getUrl()) && sub.hasValue()) {
                libraryName = sub.getValue().primitiveValue();
            } else if ("name".equals(sub.getUrl()) && sub.hasValue()) {
                name = sub.getValue().primitiveValue();
            }
        }
        return (libraryName != null && name != null) ? libraryName + "|" + name : null;
    }

    public static boolean isLogicDefinition(Extension extension) {
        return extension.hasUrl()
                && (CqfmConstants.LOGIC_DEFINITION_EXT_URL.equals(extension.getUrl())
                        || CqfConstants.LOGIC_DEFINITION_EXT_URL.equals(extension.getUrl()));
    }

    public static void deduplicate(List<Extension> extensions) {
        Set<String> seen = new HashSet<>();
        extensions.removeIf(ext -> {
            if (isLogicDefinition(ext)) {
                String key = getLogicDefinitionKey(ext);
                return key != null && !seen.add(key);
            }
            return false;
        });
    }
}
