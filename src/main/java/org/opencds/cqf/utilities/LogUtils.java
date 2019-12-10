
package org.opencds.cqf.utilities;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

public class LogUtils 
{    
    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(LogUtils.class);
    
    private static final Map<String, String> resourceWarnings = new HashMap<String, String>();    

    public static void putWarning(String id, String warning) {
        resourceWarnings.put(id, warning);
    }

    public static void info(String message) {
        ourLog.info(message);
    }

    public static void warn(String libraryName) {
        String exceptionMessage = "";
        //TODO: come up with a better answer for SUPER long errors (that include all the text of the measure narrative, for example)
        for (Map.Entry<String, String> resourceException : resourceWarnings.entrySet()) {
            String resourceExceptionMessage = resourceException.getValue();
            resourceExceptionMessage = (resourceExceptionMessage.indexOf("\r\n") > -1 ? resourceExceptionMessage.substring(0, resourceExceptionMessage.indexOf("\r\n")) + "..." : resourceExceptionMessage);
            resourceExceptionMessage = (resourceExceptionMessage.indexOf("\r") > -1 ? resourceExceptionMessage.substring(0, resourceExceptionMessage.indexOf("\r")) + "..." : resourceExceptionMessage);
            resourceExceptionMessage = (resourceExceptionMessage.indexOf("\n") > -1 ? resourceExceptionMessage.substring(0, resourceExceptionMessage.indexOf("\n")) + "..." : resourceExceptionMessage);
            exceptionMessage += "\r\n          Resource could not be processed: " + FilenameUtils.getBaseName(resourceException.getKey()) + "\r\n                    "  + resourceExceptionMessage;
        }
        ourLog.warn("Measure could not be processed: " + libraryName + exceptionMessage);
        resourceWarnings.clear(); 
    } 
}
