package org.opencds.cqf.tooling.cli;

import org.opencds.cqf.tooling.cli.command.CqfToolingCommand;
import org.opencds.cqf.tooling.common.ThreadUtils;
import picocli.CommandLine;

public class Main {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(ThreadUtils::shutdownRunningExecutors));

        int exitCode = new CommandLine(new CqfToolingCommand()).execute(args);
        System.exit(exitCode);
    }
}
