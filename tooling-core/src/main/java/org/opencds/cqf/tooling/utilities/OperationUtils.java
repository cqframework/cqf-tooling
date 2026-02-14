package org.opencds.cqf.tooling.utilities;

import com.jakewharton.fliptables.FlipTable;
import org.apache.commons.lang3.ArrayUtils;
import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.OperationParam;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class OperationUtils {
   private static final String[] HELP_ARGS = new String[] { "-h", "-help", "-?" };

   private OperationUtils() {}

   public static Class<?> getParamType(ExecutableOperation operation, String methodName) {
      for (Method m : operation.getClass().getDeclaredMethods()) {
         if (m.getName().equals(methodName)) {
            if (m.getParameterCount() > 1) {
               continue;
            }
            return m.getParameterTypes()[0];
         }
      }
      throw new InvalidOperationArgs(String.format(
              "Unable to find setter method for %s with a single parameter", methodName));
   }

   // Parameter types currently supported: String, Integer, Boolean
   public static <T> T mapParamType(String value, Class<T> clazz) {
      if (clazz.isAssignableFrom(value.getClass())) {
         return clazz.cast(value);
      }

      if (clazz.isAssignableFrom(Integer.class)) {
         return clazz.cast(Integer.decode(value));
      } else if (clazz.isAssignableFrom(Boolean.class)) {
         return clazz.cast(Boolean.valueOf(value));
      }

      throw new InvalidOperationArgs(
              "Operation parameters are not currently supported for type: " + clazz.getSimpleName());
   }

   public static String getHelpMenu(ExecutableOperation operation) {
      String[] headers = new String[]{ "Parameter", "Description" };
      String[][] rows = new String[getOperationParamCount(operation)][2];
      int idx = 0;
      for (Field field : operation.getClass().getDeclaredFields()) {
         if (field.isAnnotationPresent(OperationParam.class)) {
            rows[idx][0] = formatAliases(field.getAnnotation(OperationParam.class).alias());
            rows[idx++][1] = field.getAnnotation(OperationParam.class).description();
         }
      }

      return System.lineSeparator() + FlipTable.of(headers, rows);
   }

   public static int getOperationParamCount(ExecutableOperation operation) {
      int count = 0;
      for (Field field : operation.getClass().getDeclaredFields()) {
         if (field.isAnnotationPresent(OperationParam.class)) {
            ++count;
         }
      }
      return count;
   }

   public static String formatAliases(String[] aliases) {
      return Arrays.toString(aliases).replace("[", "-")
              .replace(", ", " | -").replace("]", "");
   }

   public static boolean isHelpArg(String arg) {
      return ArrayUtils.contains(HELP_ARGS, arg);
   }
}
