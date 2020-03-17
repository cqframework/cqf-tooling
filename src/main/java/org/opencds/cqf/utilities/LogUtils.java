
package org.opencds.cqf.utilities;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

public class LogUtils 
{    
    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(LogUtils.class);
    private static final Map<String, String> resourceWarnings = new LinkedHashMap<String, String>();    

    public static void putException(String id, Exception e) {
        resourceWarnings.put(LocalDateTime.now().toString() + ": " + id,  e.getMessage() == null ? e.toString() : e.getMessage());
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
        String[] messages = message.split("\r\n");
        int cutoffIndex = 0;
        for (String string : messages) {
            int stringIndex = cutoffIndex + string.length() + 4;
            cutoffIndex = stringIndex > maxSize ? maxSize : stringIndex;
            if(cutoffIndex == maxSize) {
                break;
            }
        }
        return message.length() < cutoffIndex ? message : message.substring(0, cutoffIndex) + "...";
    }
}
