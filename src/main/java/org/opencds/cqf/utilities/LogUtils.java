
package org.opencds.cqf.utilities;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

public class LogUtils 
{    
    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(LogUtils.class);
    private static final Map<String, String> resourceWarnings = new LinkedHashMap<String, String>();    

    public static void putWarning(String id, String warning) {
        resourceWarnings.put(LocalDateTime.now().toString() + ": " + id, warning);
    }

    public static void info(String message) {
        ourLog.warn(message);
    }

    public static void warn(String libraryName) {
        if (resourceWarnings.isEmpty()) {
            return;
        }
        String exceptionMessage = "";
        for (Map.Entry<String, String> resourceException : resourceWarnings.entrySet()) {
            String resourceExceptionMessage = truncateMessage(resourceException.getValue()); 
            String resource =  FilenameUtils.getBaseName(resourceException.getKey());           
            exceptionMessage += "\r\n          Resource could not be processed: " + resource + "\r\n                    "  + resourceExceptionMessage;
        }
        ourLog.warn(libraryName +" could not be processed: "  + exceptionMessage);
        resourceWarnings.clear(); 
    } 

    private static String truncateMessage(String message) {   
        int maxSize = 500;     
        int cutoffIndex = message.indexOf("\r\n");
        cutoffIndex = cutoffIndex > -1 ? cutoffIndex : message.indexOf("\r");
        cutoffIndex = cutoffIndex > -1 ? cutoffIndex : message.indexOf("\n");
        cutoffIndex = cutoffIndex > maxSize ? maxSize : cutoffIndex;
        int cutoffIndex2 = -1;
        if (cutoffIndex > -1) {
            cutoffIndex2 = message.indexOf("\r\n", cutoffIndex + 1);
            cutoffIndex2 = cutoffIndex2 > -1 ? cutoffIndex2 : message.indexOf("\r", cutoffIndex + 1);
            cutoffIndex2 = cutoffIndex2 > -1 ? cutoffIndex2 : message.indexOf("\n", cutoffIndex + 1);
            cutoffIndex2 = cutoffIndex2 > maxSize ? maxSize : cutoffIndex2;
        } 

        cutoffIndex = cutoffIndex2 > -1 ? cutoffIndex2 : (cutoffIndex > -1 ? cutoffIndex : maxSize); 
        return message.length() < cutoffIndex ? message : message.substring(0, cutoffIndex) + "...";
    }
}
