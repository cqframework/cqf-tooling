package org.opencds.cqf.tooling.cli.command;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operation.ConvertR5toR4;
import org.opencds.cqf.tooling.qdm.QdmToQiCore;
import org.opencds.cqf.tooling.quick.QuickPageGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "convert", description = "FHIR version and model conversion operations", mixinStandardHelpOptions = true, subcommands = {
    ConvertCommand.R5ToR4.class,
    ConvertCommand.QdmToQiCoreCmd.class,
    ConvertCommand.QiCoreQuick.class,
})
public class ConvertCommand implements Runnable {

    @Spec CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    @Command(name = "r5-to-r4", description = "Convert R5 resources to R4")
    static class R5ToR4 extends OperationCommand {
        @Override protected String getOperationName() { return "ConvertR5toR4"; }
        @Override protected Operation createOperation() { return new ConvertR5toR4(); }
    }

    @Command(name = "qdm-to-qicore", description = "Generate QDM to QiCore mappings")
    static class QdmToQiCoreCmd extends OperationCommand {
        @Override protected String getOperationName() { return "QdmToQiCore"; }
        @Override protected Operation createOperation() { return new QdmToQiCore(); }
    }

    @Command(name = "qicore-quick", description = "Generate QiCore QUICK pages")
    static class QiCoreQuick extends OperationCommand {
        @Override protected String getOperationName() { return "QiCoreQUICK"; }
        @Override protected Operation createOperation() { return new QuickPageGenerator(); }
    }
}
