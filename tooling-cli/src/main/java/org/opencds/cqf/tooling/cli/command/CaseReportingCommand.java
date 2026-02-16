package org.opencds.cqf.tooling.cli.command;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.casereporting.tes.TESPackageGenerator;
import org.opencds.cqf.tooling.casereporting.transformer.ErsdTransformer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
        name = "casereporting",
        description = "Case reporting operations (eRSD, TES)",
        mixinStandardHelpOptions = true,
        subcommands = {
            CaseReportingCommand.TransformErsd.class,
            CaseReportingCommand.GeneratePackage.class,
        })
public class CaseReportingCommand implements Runnable {

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    @Command(name = "transform-ersd", description = "Transform eRSD v1 bundle to eRSD v2")
    static class TransformErsd extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "TransformErsd";
        }

        @Override
        protected Operation createOperation() {
            return new ErsdTransformer();
        }
    }

    @Command(name = "generate-package", description = "Generate a TES package from an input Bundle")
    static class GeneratePackage extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "CaseReportingTESGeneratePackage";
        }

        @Override
        protected Operation createOperation() {
            return new TESPackageGenerator();
        }
    }
}
