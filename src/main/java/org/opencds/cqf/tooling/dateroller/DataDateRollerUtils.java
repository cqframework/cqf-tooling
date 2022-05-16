package org.opencds.cqf.tooling.dateroller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.gson.JsonObject;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DataDateRollerUtils {
    public static LocalDate stringToDate(String stringToConvert) {
        LocalDate localDate = null;
        DateTimeFormatter format
                = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            localDate = LocalDate.parse(stringToConvert, format);
        } catch (IllegalArgumentException iaex) {
            System.out.println(iaex);
        }
        return localDate;
    }

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

    public static LocalDate rollDate(LocalDate dateToRoll, DataDateRollerSettings dataDateRollerSettings) {
        return dateToRoll.plusDays(LocalDate.now().getLong(ChronoField.EPOCH_DAY) - dataDateRollerSettings.getLastDateUpdated().getLong(ChronoField.EPOCH_DAY));
    }

    public static boolean isCurrentDateGreaterThanInterval(DataDateRollerSettings dataDateRollerSettings) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdated = LocalDate.parse(dataDateRollerSettings.getLastDateUpdated().toString()).atStartOfDay();
        int valueDuration = dataDateRollerSettings.getDurationLength().intValue();
        boolean canUpdateDates;
        switch (dataDateRollerSettings.getDurationUnitCode()) {
            case "d":
                canUpdateDates = ChronoUnit.DAYS.between(lastUpdated, now) > valueDuration;
                break;
            case "w":
                canUpdateDates = ChronoUnit.WEEKS.between(lastUpdated, now) > valueDuration;
                break;
            case "m":
                canUpdateDates = ChronoUnit.MONTHS.between(lastUpdated, now) > valueDuration;
                break;
            case "y":
                canUpdateDates = ChronoUnit.YEARS.between(lastUpdated, now) > valueDuration;
                break;
            default:
                throw new IllegalArgumentException("The code in the Extension http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller for duration is incorrect. Must be {d, w, m, or y}");
        }
        return canUpdateDates;
    }

    public static IBaseResource jsonToIBaseResource(JsonObject jsonObject) {
//        ResourceUtils.
        return null;
//        IBaseResource baseResource = IBaseResource;
    }
}
