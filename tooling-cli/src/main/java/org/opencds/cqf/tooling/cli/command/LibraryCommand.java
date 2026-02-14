package org.opencds.cqf.tooling.cli.command;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operation.RefreshLibraryOperation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "library", description = "CQL Library operations", mixinStandardHelpOptions = true, subcommands = {
    LibraryCommand.GenerateR4.class,
    LibraryCommand.GenerateStu3.class,
    LibraryCommand.Refresh.class,
})
public class LibraryCommand implements Runnable {

    @Spec CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    @Command(name = "generate-r4", description = "Generate FHIR R4 Library from CQL")
    static class GenerateR4 extends OperationCommand {
        @Override protected String getOperationName() { return "CqlToR4Library"; }
        @Override protected Operation createOperation() { return new org.opencds.cqf.tooling.library.r4.LibraryGenerator(); }
    }

    @Command(name = "generate-stu3", description = "Generate FHIR STU3 Library from CQL")
    static class GenerateStu3 extends OperationCommand {
        @Override protected String getOperationName() { return "CqlToSTU3Library"; }
        @Override protected Operation createOperation() { return new org.opencds.cqf.tooling.library.stu3.LibraryGenerator(); }
    }

    @Command(name = "refresh", description = "Refresh a Library resource")
    static class Refresh extends OperationCommand {
        @Override protected String getOperationName() { return "RefreshLibrary"; }
        @Override protected Operation createOperation() { return new RefreshLibraryOperation(); }
    }
}
