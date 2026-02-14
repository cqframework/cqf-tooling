package org.opencds.cqf.tooling.cli.command;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.dateroller.DataDateRollerOperation;
import org.opencds.cqf.tooling.modelinfo.StructureDefinitionToModelInfo;
import org.opencds.cqf.tooling.operation.ExtractMatBundleOperation;
import org.opencds.cqf.tooling.operation.PostmanCollectionOperation;
import org.opencds.cqf.tooling.operation.ProfilesToSpreadsheet;
import org.opencds.cqf.tooling.operation.QICoreElementsToSpreadsheet;
import org.opencds.cqf.tooling.operation.StripGeneratedContentOperation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "utility", description = "Miscellaneous utility operations", mixinStandardHelpOptions = true, subcommands = {
    UtilityCommand.DateRoller.class,
    UtilityCommand.MatExtract.class,
    UtilityCommand.ModelInfo.class,
    UtilityCommand.StripContent.class,
    UtilityCommand.Postman.class,
    UtilityCommand.ProfilesToSheet.class,
    UtilityCommand.QiCoreElements.class,
})
public class UtilityCommand implements Runnable {

    @Spec CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    @Command(name = "dateroller", description = "Roll test data dates to current date range")
    static class DateRoller extends OperationCommand {
        @Override protected String getOperationName() { return "RollTestsDataDates"; }
        @Override protected Operation createOperation() { return new DataDateRollerOperation(); }
    }

    @Command(name = "mat-extract", description = "Extract resources and CQL from a MAT export Bundle")
    static class MatExtract extends OperationCommand {
        @Override protected String getOperationName() { return "ExtractMatBundle"; }
        @Override protected Operation createOperation() { return new ExtractMatBundleOperation(); }
    }

    @Command(name = "modelinfo-generate", description = "Generate ModelInfo from StructureDefinitions")
    static class ModelInfo extends OperationCommand {
        @Override protected String getOperationName() { return "GenerateMIs"; }
        @Override protected Operation createOperation() { return new StructureDefinitionToModelInfo(); }
    }

    @Command(name = "strip-content", description = "Strip generated content from resources")
    static class StripContent extends OperationCommand {
        @Override protected String getOperationName() { return "StripGeneratedContent"; }
        @Override protected Operation createOperation() { return new StripGeneratedContentOperation(); }
    }

    @Command(name = "postman", description = "Generate a Postman collection from measure Bundles")
    static class Postman extends OperationCommand {
        @Override protected String getOperationName() { return "PostmanCollection"; }
        @Override protected Operation createOperation() { return new PostmanCollectionOperation(); }
    }

    @Command(name = "profiles-to-spreadsheet", description = "Export profiles to spreadsheet format")
    static class ProfilesToSheet extends OperationCommand {
        @Override protected String getOperationName() { return "ProfilesToSpreadsheet"; }
        @Override protected Operation createOperation() { return new ProfilesToSpreadsheet(); }
    }

    @Command(name = "qicore-elements", description = "Export QICore elements to spreadsheet format")
    static class QiCoreElements extends OperationCommand {
        @Override protected String getOperationName() { return "QICoreElementsToSpreadsheet"; }
        @Override protected Operation createOperation() { return new QICoreElementsToSpreadsheet(); }
    }
}
