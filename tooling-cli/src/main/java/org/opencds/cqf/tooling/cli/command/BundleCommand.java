package org.opencds.cqf.tooling.cli.command;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operation.BundlePublish;
import org.opencds.cqf.tooling.operation.BundleResources;
import org.opencds.cqf.tooling.operation.BundleToResources;
import org.opencds.cqf.tooling.operation.BundleToTransactionOperation;
import org.opencds.cqf.tooling.operation.PostBundlesInDirOperation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
        name = "bundle",
        description = "FHIR Bundle operations",
        mixinStandardHelpOptions = true,
        subcommands = {
            BundleCommand.Resources.class,
            BundleCommand.ToResources.class,
            BundleCommand.ToTransaction.class,
            BundleCommand.Post.class,
            BundleCommand.Publish.class,
        })
public class BundleCommand implements Runnable {

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    @Command(name = "resources", description = "Bundle resources from a directory into a FHIR Bundle")
    static class Resources extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "BundleResources";
        }

        @Override
        protected Operation createOperation() {
            return new BundleResources();
        }
    }

    @Command(name = "to-resources", description = "Decompose a Bundle into individual resource files")
    static class ToResources extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "BundleToResources";
        }

        @Override
        protected Operation createOperation() {
            return new BundleToResources();
        }
    }

    @Command(name = "to-transaction", description = "Convert a collection Bundle to a transaction Bundle")
    static class ToTransaction extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "MakeTransaction";
        }

        @Override
        protected Operation createOperation() {
            return new BundleToTransactionOperation();
        }
    }

    @Command(name = "post", description = "POST Bundles in a directory to a FHIR server")
    static class Post extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "PostBundlesInDir";
        }

        @Override
        protected Operation createOperation() {
            return new PostBundlesInDirOperation();
        }
    }

    @Command(name = "publish", description = "Publish a Bundle")
    static class Publish extends OperationCommand {
        @Override
        protected String getOperationName() {
            return "PublishBundle";
        }

        @Override
        protected Operation createOperation() {
            return new BundlePublish();
        }
    }
}
