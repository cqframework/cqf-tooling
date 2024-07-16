
package org.opencds.cqf.tooling.utilities;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

public class LogUtils {
    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(LogUtils.class);
    private static final Map<String, String> resourceWarnings = new LinkedHashMap<>();

    private LogUtils() {}

    public static void putException(String id, Exception e) {
        e.printStackTrace();
        resourceWarnings.put(LocalDateTime.now() + ": " + id,
                e.getMessage() == null ? e.toString() : e.getMessage());
    }

    public static void putException(String id, String warning) {
        resourceWarnings.put(LocalDateTime.now() + ": " + id, warning);
    }

    public static void info(String message) {
        ourLog.info(message);
    }

    private static String stripTimestamp(String value) {
        if (value == null) {
            return null;
        }

        String[] values = value.split(" ");
        value = values[values.length - 1];
        int lastColon = value.lastIndexOf(':');
        if (lastColon >= 0) {
            value = value.substring(lastColon + 1);
        }
        return value;
    }

    public static void warn(String libraryName) {
        if (resourceWarnings.isEmpty()) {
            return;
        }
        StringBuilder exceptionMessage = new StringBuilder();
        for (Map.Entry<String, String> resourceException : resourceWarnings.entrySet()) {
            exceptionMessage.append("\r\n          Resource could not be processed: ");
            exceptionMessage.append(FilenameUtils.getBaseName(stripTimestamp(resourceException.getKey())));
            exceptionMessage.append("\r\n                    ");
            exceptionMessage.append(truncateMessage(resourceException.getValue()));
        }
        ourLog.warn("{} could not be processed: {}", libraryName, exceptionMessage);
        resourceWarnings.clear();
    }

    private static String truncateMessage(String message) {
        int maxSize = 500;
        if (message == null) {
            return "null message";
        }
        String[] messages = message.split("\r\n");
        int cutoffIndex = 0;
        for (String string : messages) {
            int stringIndex = cutoffIndex + string.length() + 4;
            cutoffIndex = Math.min(stringIndex, maxSize);
            if (cutoffIndex == maxSize) {
                break;
            }
        }
        return message.length() < cutoffIndex ? message : message.substring(0, cutoffIndex) + "...";
    }
}
