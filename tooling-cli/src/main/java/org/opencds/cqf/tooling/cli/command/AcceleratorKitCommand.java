package org.opencds.cqf.tooling.cli.command;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.acceleratorkit.DTProcessor;
import org.opencds.cqf.tooling.acceleratorkit.Processor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "acceleratorkit", description = "WHO Accelerator Kit operations", mixinStandardHelpOptions = true, subcommands = {
    AcceleratorKitCommand.Process.class,
    AcceleratorKitCommand.DecisionTables.class,
})
public class AcceleratorKitCommand implements Runnable {

    @Spec CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    @Command(name = "process", description = "Process a WHO Accelerator Kit data dictionary")
    static class Process extends OperationCommand {
        @Override protected String getOperationName() { return "ProcessAcceleratorKit"; }
        @Override protected Operation createOperation() { return new Processor(); }
    }

    @Command(name = "decision-tables", description = "Process WHO Accelerator Kit decision tables")
    static class DecisionTables extends OperationCommand {
        @Override protected String getOperationName() { return "ProcessDecisionTables"; }
        @Override protected Operation createOperation() { return new DTProcessor(); }
    }
}
