package org.opencds.cqf.tooling.cli.command;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operation.GenerateCQLFromDroolOperation;
import org.opencds.cqf.tooling.operation.VmrToFhirOperation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
        name = "cql",
        description = "CQL generation operations",
        mixinStandardHelpOptions = true,
        subcommands = {
            CqlCommand.FromDrool.class,
            CqlCommand.FromVmr.class,
        })
public class CqlCommand implements Runnable {

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    @Command(name = "from-drool", description = "Generate CQL from Drool rules")
    static class FromDrool extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "GenerateCQLFromDrool";
        }

        @Override
        protected Operation createOperation() {
            return new GenerateCQLFromDroolOperation();
        }
    }

    @Command(name = "from-vmr", description = "Transform vMR data to FHIR")
    static class FromVmr extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "VmrToFhir";
        }

        @Override
        protected Operation createOperation() {
            return new VmrToFhirOperation();
        }
    }
}
