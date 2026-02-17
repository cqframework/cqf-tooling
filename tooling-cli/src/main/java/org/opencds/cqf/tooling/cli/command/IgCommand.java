package org.opencds.cqf.tooling.cli.command;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.ig.RefreshIG;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
        name = "ig",
        description = "Implementation Guide operations",
        mixinStandardHelpOptions = true,
        subcommands = {
            IgCommand.Refresh.class,
            IgCommand.RefreshLegacy.class,
            IgCommand.Scaffold.class,
            IgCommand.Test.class,
            IgCommand.Bundle.class,
        })
public class IgCommand implements Runnable {

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    @Command(name = "refresh", description = "Refresh an Implementation Guide (recommended)")
    static class Refresh extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "NewRefreshIG";
        }

        @Override
        protected Operation createOperation() {
            return new RefreshIG();
        }
    }

    @Command(name = "refresh-legacy", description = "Refresh an IG using the legacy processor")
    static class RefreshLegacy extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "RefreshIG";
        }

        @Override
        protected Operation createOperation() {
            return new ExecutableOperationAdapter(new org.opencds.cqf.tooling.operations.ig.RefreshIGLegacy());
        }
    }

    @Command(name = "scaffold", description = "Scaffold a new Implementation Guide")
    static class Scaffold extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "ScaffoldIG";
        }

        @Override
        protected Operation createOperation() {
            return new ExecutableOperationAdapter(new org.opencds.cqf.tooling.operations.ig.Scaffold());
        }
    }

    @Command(name = "test", description = "Test an Implementation Guide")
    static class Test extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "TestIG";
        }

        @Override
        protected Operation createOperation() {
            return new ExecutableOperationAdapter(new org.opencds.cqf.tooling.operations.ig.TestIG());
        }
    }

    @Command(name = "bundle", description = "Bundle an Implementation Guide")
    static class Bundle extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "BundleIg";
        }

        @Override
        protected Operation createOperation() {
            return new ExecutableOperationAdapter(new org.opencds.cqf.tooling.operations.ig.BundleIG());
        }
    }
}
