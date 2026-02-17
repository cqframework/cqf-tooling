package org.opencds.cqf.tooling.operations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.utilities.OperationUtils;

/**
 * Adapter that bridges the legacy {@link org.opencds.cqf.tooling.Operation} base class
 * to the new {@link ExecutableOperation} pattern. This allows new-style operations
 * (annotated with {@link Operation} and {@link OperationParam}) to be used through the
 * existing CLI infrastructure which expects {@code Operation.execute(String[] args)}.
 *
 * <p>The adapter performs reflection-based parameter binding:
 * <ol>
 *   <li>Parses {@code String[] args} into key=value pairs</li>
 *   <li>Scans {@link OperationParam} fields on the delegate</li>
 *   <li>Matches args against aliases, applies defaults, validates required params</li>
 *   <li>Invokes setters via reflection</li>
 *   <li>Calls {@link ExecutableOperation#execute()}</li>
 * </ol>
 */
public class ExecutableOperationAdapter extends org.opencds.cqf.tooling.Operation {

    private final ExecutableOperation delegate;

    public ExecutableOperationAdapter(ExecutableOperation delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(String[] args) {
        if (hasHelpArg(args)) {
            System.out.println(OperationUtils.getHelpMenu(delegate));
            return;
        }

        Map<String, String> parsedArgs = parseArgs(args);
        bindParams(parsedArgs);
        delegate.execute();
    }

    private boolean hasHelpArg(String[] args) {
        for (String arg : args) {
            if (OperationUtils.isHelpArg(arg)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> parseArgs(String[] args) {
        Map<String, String> result = new HashMap<>();
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                continue;
            }
            String stripped = arg.startsWith("--") ? arg.substring(2) : arg.substring(1);
            int eqIndex = stripped.indexOf('=');
            if (eqIndex > 0) {
                String key = stripped.substring(0, eqIndex).toLowerCase();
                String value = stripped.substring(eqIndex + 1);
                result.put(key, value);
            }
        }
        return result;
    }

    private void bindParams(Map<String, String> args) {
        for (Field field : delegate.getClass().getDeclaredFields()) {
            OperationParam param = field.getAnnotation(OperationParam.class);
            if (param == null) {
                continue;
            }

            String value = findArgValue(args, param.alias());

            if (value == null && !param.defaultValue().isEmpty()) {
                value = param.defaultValue();
            }

            if (value == null && param.required()) {
                throw new InvalidOperationArgs(
                        String.format("Missing required parameter: %s", OperationUtils.formatAliases(param.alias())));
            }

            if (value != null) {
                invokeSetter(param.setter(), value);
            }
        }
    }

    private String findArgValue(Map<String, String> args, String[] aliases) {
        for (String alias : aliases) {
            String value = args.get(alias.toLowerCase());
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private void invokeSetter(String setterName, String rawValue) {
        Class<?> paramType = OperationUtils.getParamType(delegate, setterName);
        Object typedValue = OperationUtils.mapParamType(rawValue, paramType);
        try {
            Method setter = findSetter(setterName, paramType);
            setter.invoke(delegate, typedValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new InvalidOperationArgs(String.format("Failed to invoke setter %s: %s", setterName, e.getMessage()));
        }
    }

    private Method findSetter(String name, Class<?> paramType) {
        try {
            return delegate.getClass().getMethod(name, paramType);
        } catch (NoSuchMethodException e) {
            try {
                return delegate.getClass().getDeclaredMethod(name, paramType);
            } catch (NoSuchMethodException ex) {
                throw new InvalidOperationArgs(
                        String.format("Setter method %s(%s) not found", name, paramType.getSimpleName()));
            }
        }
    }
}
