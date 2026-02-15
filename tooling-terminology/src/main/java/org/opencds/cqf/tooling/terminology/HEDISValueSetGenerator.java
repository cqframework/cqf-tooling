package org.opencds.cqf.tooling.terminology;

public class HEDISValueSetGenerator extends FlatMultiValueSetGeneratorBase {

    private static final String outputPath = "src/main/resources/org/opencds/cqf/tooling/terminology/output/hedis";  // -outputpath (-op)
    private static final String encoding = "json";                                              // -encoding (-e)
    private static final String publisher = "National Committee for Quality Assurance (NCQA)";  // -publisher (-p)
    private static final String publisherNamespace = "http://ncqa.org/fhir/hedis";              // -publishernamespace (-pns)
    private static final String valueSetIdentifierSystem = "urn:ietf:rfc:3986";                 // -valuesetidentifiersystem (-vsis) // ValueSet.Identifier System

    // Code sheet defaults
    private static final int codeSheetNum = 2;       // -codesheetnum (-csn)         // Codes Sheet
    private static final int codeListRow = 1;        // -codelistrow (-clr)          // Row at which the codes start
    private static final int valueSetTitleCol = 0;   // -valuesettitlecol (-vstc)    // ValueSet Title
    private static final int valueSetOidCol = 1;     // -valuesetoidcol (-vsoc)      // ValueSet OID
    private static final int valueSetVersionCol = 2; // -valuesetversioncol (-vsvc)  // ValueSet Version
    private static final int codeCol = 3;            // -codecol (-cc)               // Code column
    private static final int descriptionCol = 4;     // -descriptioncol (-dc)        // Code Description Column
    private static final int systemNameCol = 5;      // -systemnamecol (-snc)        // Code System Name Column
    private static final int systemOidCol = 6;       // -systemoidcol (-soc)         // Code System OID Column
    private static final int versionCol = 7;         // -versioncol (-vc)            // Code System Version Column
    private static final int expansionId = -1;       // -expansionid (-eic)          // N/A

    public HEDISValueSetGenerator()
    {
        super(outputPath, encoding, publisher, publisherNamespace, valueSetIdentifierSystem, codeSheetNum, codeListRow,
            valueSetTitleCol, valueSetOidCol, valueSetVersionCol, codeCol, descriptionCol, systemNameCol, systemOidCol,
            versionCol, expansionId);
    }
}
