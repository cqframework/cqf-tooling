package org.opencds.cqf.tooling.cli.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
        name = "cqf-tooling",
        description = "CQF Tooling CLI for FHIR Implementation Guide development",
        subcommands = {
            IgCommand.class,
            TerminologyCommand.class,
            LibraryCommand.class,
            MeasureCommand.class,
            BundleCommand.class,
            AcceleratorKitCommand.class,
            CaseReportingCommand.class,
            CqlCommand.class,
            ConvertCommand.class,
            UtilityCommand.class,
        },
        mixinStandardHelpOptions = true,
        versionProvider = CqfToolingCommand.VersionProvider.class)
public class CqfToolingCommand implements Runnable {

    @Spec CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    static class VersionProvider implements picocli.CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            String version = CqfToolingCommand.class.getPackage().getImplementationVersion();
            return new String[] {"cqf-tooling " + (version != null ? version : "dev")};
        }
    }
}
