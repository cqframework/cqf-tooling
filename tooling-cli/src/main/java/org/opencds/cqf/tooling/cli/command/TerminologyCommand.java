package org.opencds.cqf.tooling.cli.command;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.terminology.CMSFlatMultiValueSetGenerator;
import org.opencds.cqf.tooling.terminology.EnsureExecutableValueSetOperation;
import org.opencds.cqf.tooling.terminology.GenericValueSetGenerator;
import org.opencds.cqf.tooling.terminology.HEDISValueSetGenerator;
import org.opencds.cqf.tooling.terminology.RCKMSJurisdictionsGenerator;
import org.opencds.cqf.tooling.terminology.SpreadsheetToCQLOperation;
import org.opencds.cqf.tooling.terminology.SpreadsheetValidateVSandCS;
import org.opencds.cqf.tooling.terminology.ToJsonValueSetDbOperation;
import org.opencds.cqf.tooling.terminology.VSACBatchValueSetGenerator;
import org.opencds.cqf.tooling.terminology.VSACValueSetGenerator;
import org.opencds.cqf.tooling.terminology.distributable.DistributableValueSetGenerator;
import org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.TemplateToValueSetGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "terminology", description = "Terminology and ValueSet operations", mixinStandardHelpOptions = true, subcommands = {
    TerminologyCommand.VsacXlsx.class,
    TerminologyCommand.Distributable.class,
    TerminologyCommand.VsacMulti.class,
    TerminologyCommand.VsacBatch.class,
    TerminologyCommand.HedisXlsx.class,
    TerminologyCommand.Xlsx.class,
    TerminologyCommand.Template.class,
    TerminologyCommand.EnsureExecutable.class,
    TerminologyCommand.ToJsonDb.class,
    TerminologyCommand.Jurisdictions.class,
    TerminologyCommand.SpreadsheetToCql.class,
    TerminologyCommand.Validate.class,
})
public class TerminologyCommand implements Runnable {

    @Spec CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    @Command(name = "vsac-xlsx", description = "Convert VSAC Excel spreadsheet to FHIR ValueSet")
    static class VsacXlsx extends OperationCommand {
        @Override protected String getOperationName() { return "VsacXlsxToValueSet"; }
        @Override protected Operation createOperation() { return new VSACValueSetGenerator(); }
    }

    @Command(name = "distributable", description = "Convert distributable Excel spreadsheet to ValueSet")
    static class Distributable extends OperationCommand {
        @Override protected String getOperationName() { return "DistributableXlsxToValueSet"; }
        @Override protected Operation createOperation() { return new DistributableValueSetGenerator(); }
    }

    @Command(name = "vsac-multi", description = "Convert CMS flat multi-ValueSet spreadsheet")
    static class VsacMulti extends OperationCommand {
        @Override protected String getOperationName() { return "VsacMultiXlsxToValueSet"; }
        @Override protected Operation createOperation() { return new CMSFlatMultiValueSetGenerator(); }
    }

    @Command(name = "vsac-batch", description = "Batch convert VSAC Excel spreadsheets to ValueSets")
    static class VsacBatch extends OperationCommand {
        @Override protected String getOperationName() { return "VsacXlsxToValueSetBatch"; }
        @Override protected Operation createOperation() { return new VSACBatchValueSetGenerator(); }
    }

    @Command(name = "hedis-xlsx", description = "Convert HEDIS Excel spreadsheet to ValueSet")
    static class HedisXlsx extends OperationCommand {
        @Override protected String getOperationName() { return "HedisXlsxToValueSet"; }
        @Override protected Operation createOperation() { return new HEDISValueSetGenerator(); }
    }

    @Command(name = "xlsx", description = "Convert generic Excel spreadsheet to FHIR ValueSet")
    static class Xlsx extends OperationCommand {
        @Override protected String getOperationName() { return "XlsxToValueSet"; }
        @Override protected Operation createOperation() { return new GenericValueSetGenerator(); }
    }

    @Command(name = "template", description = "Generate ValueSets from a template spreadsheet")
    static class Template extends OperationCommand {
        @Override protected String getOperationName() { return "TemplateToValueSetGenerator"; }
        @Override protected Operation createOperation() { return new TemplateToValueSetGenerator(); }
    }

    @Command(name = "ensure-executable", description = "Ensure ValueSet has an expansion (executable/computable)")
    static class EnsureExecutable extends OperationCommand {
        @Override protected String getOperationName() { return "EnsureExecutableValueSet"; }
        @Override protected Operation createOperation() { return new EnsureExecutableValueSetOperation(); }
    }

    @Command(name = "to-json-db", description = "Convert ValueSets to JSON database format")
    static class ToJsonDb extends OperationCommand {
        @Override protected String getOperationName() { return "ToJsonValueSetDb"; }
        @Override protected Operation createOperation() { return new ToJsonValueSetDbOperation(); }
    }

    @Command(name = "jurisdictions", description = "Convert RCKMS jurisdictions spreadsheet to CodeSystem")
    static class Jurisdictions extends OperationCommand {
        @Override protected String getOperationName() { return "JurisdictionsXlsxToCodeSystem"; }
        @Override protected Operation createOperation() { return new RCKMSJurisdictionsGenerator(); }
    }

    @Command(name = "spreadsheet-to-cql", description = "Convert spreadsheet rows to CQL expressions")
    static class SpreadsheetToCql extends OperationCommand {
        @Override protected String getOperationName() { return "SpreadsheetToCQL"; }
        @Override protected Operation createOperation() { return new SpreadsheetToCQLOperation(); }
    }

    @Command(name = "validate", description = "Validate ValueSets and CodeSystems from spreadsheet")
    static class Validate extends OperationCommand {
        @Override protected String getOperationName() { return "SpreadsheetValidateVSandCS"; }
        @Override protected Operation createOperation() { return new SpreadsheetValidateVSandCS(); }
    }
}
