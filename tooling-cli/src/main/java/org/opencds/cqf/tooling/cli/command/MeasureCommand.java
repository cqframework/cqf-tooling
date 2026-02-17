package org.opencds.cqf.tooling.cli.command;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.measure.r4.RefreshR4MeasureOperation;
import org.opencds.cqf.tooling.measure.stu3.RefreshStu3MeasureOperation;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
        name = "measure",
        description = "Measure operations",
        mixinStandardHelpOptions = true,
        subcommands = {
            MeasureCommand.RefreshR4.class,
            MeasureCommand.RefreshStu3.class,
            MeasureCommand.Test.class,
        })
public class MeasureCommand implements Runnable {

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    @Command(name = "refresh-r4", description = "Refresh R4 Measure resources")
    static class RefreshR4 extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "RefreshR4Measure";
        }

        @Override
        protected Operation createOperation() {
            return new RefreshR4MeasureOperation();
        }
    }

    @Command(name = "refresh-stu3", description = "Refresh STU3 Measure resources")
    static class RefreshStu3 extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "RefreshStu3Measure";
        }

        @Override
        protected Operation createOperation() {
            return new RefreshStu3MeasureOperation();
        }
    }

    @Command(name = "test", description = "Execute a Measure test case")
    static class Test extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "ExecuteMeasureTest";
        }

        @Override
        protected Operation createOperation() {
            return new ExecutableOperationAdapter(new org.opencds.cqf.tooling.operations.measure.ExecuteMeasureTest());
        }
    }
}
