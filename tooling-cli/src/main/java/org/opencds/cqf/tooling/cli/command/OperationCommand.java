package org.opencds.cqf.tooling.cli.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.opencds.cqf.tooling.Operation;
import picocli.CommandLine.Parameters;

/**
 * Base class for all CLI commands that delegate to a legacy {@link Operation}.
 * Subclasses provide the operation name and factory method; arguments are passed
 * through in the original {@code -key=value} format after a {@code --} separator.
 */
@SuppressWarnings("checkstyle:AbstractClassName")
public abstract class OperationCommand implements Callable<Integer> {

    @Parameters(arity = "0..*", description = "Operation arguments in -key=value format (use -- before args)")
    protected List<String> args = new ArrayList<>();

    protected abstract String getOperationName();

    protected abstract Operation createOperation();

    @Override
    public Integer call() {
        List<String> fullArgs = new ArrayList<>();
        fullArgs.add("-" + getOperationName());
        fullArgs.addAll(args);
        try {
            createOperation().execute(fullArgs.toArray(new String[0]));
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
