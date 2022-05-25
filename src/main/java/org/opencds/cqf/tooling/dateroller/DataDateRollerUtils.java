package org.opencds.cqf.tooling.dateroller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class DataDateRollerUtils {
    private static Logger logger = LoggerFactory.getLogger(ResourceDataDateRoller.class);

    public static IParser getParser(IOUtils.Encoding encoding, FhirContext fhirContext) {
        switch (encoding) {
            case XML:
                return fhirContext.newXmlParser();
            case JSON:
                return fhirContext.newJsonParser();
            default:
                throw new RuntimeException("Unknown encoding type: " + encoding.toString());
        }
    }

    public static LocalDate stringDateFromResourceToLocalDate(String strDateToRoll) {
        if (strDateToRoll.contains("[")) {
            strDateToRoll = strDateToRoll.substring(strDateToRoll.indexOf("[") + 1, strDateToRoll.indexOf("]"));
        }
        if (strDateToRoll.contains("T")) {
            strDateToRoll = strDateToRoll.substring(0, strDateToRoll.indexOf('T'));
        }
        try{
            return LocalDate.parse(strDateToRoll);
        }
        catch(Exception ex){
            logger.debug(ex.getMessage());
            return null;
        }
    }
}
