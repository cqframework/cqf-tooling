package org.opencds.cqf.tooling.terminology;

public class CMSFlatMultiValueSetGenerator extends FlatMultiValueSetGeneratorBase {

    private static final String outputPath = "src/main/resources/org/opencds/cqf/tooling/terminology/output/vsac";  // -outputpath (-op)
    private static final String encoding = "json";                                  // -encoding (-e)
    private static final String publisher = "NLM";                                  // -publisher (-p)
    private static final String publisherNamespace = "http://cts.nlm.nih.gov/fhir"; // -publishernamespace (-pns)
    private static final String valueSetIdentifierSystem = "urn:ietf:rfc:3986";     // -valuesetidentifiersystem (-vsis)

    // Code sheet defaults
    private static final int codeSheetNum = -1;        // -codesheetnum (-csn)         // Codes Sheet
    private static final int codeListRow = 2;         // -codelistrow (-clr)          // Row at which the codes start
    private static final int valueSetTitleCol = 2;    // -valuesettitlecol (-vstc)    // ValueSet Title
    private static final int valueSetOidCol = 3;      // -valuesetoidcol (-vsoc)      // ValueSet OID
    private static final int valueSetVersionCol = 5;  // -valuesetversioncol (-vsvc)  // ValueSet Version
    private static final int codeCol = 11;            // -codecol (-cc)               // Code column
    private static final int descriptionCol = 12;     // -descriptioncol (-dc)        // Code Description Column
    private static final int systemNameCol = 13;      // -systemnamecol (-snc)        // Code System Name Column
    private static final int systemOidCol = 14;       // -systemoidcol (-soc)         // Code System OID Column
    private static final int versionCol = 15;         // -versioncol (-vc)            // Code System Version Column
    private static final int expansionIdCol = 16;     // -expansionidcol (-eic)       // Expansion Id Column

    public CMSFlatMultiValueSetGenerator()
    {
        super(outputPath, encoding, publisher, publisherNamespace, valueSetIdentifierSystem, codeSheetNum, codeListRow,
            valueSetTitleCol, valueSetOidCol, valueSetVersionCol, codeCol, descriptionCol, systemNameCol, systemOidCol,
            versionCol, expansionIdCol);
    }
}
