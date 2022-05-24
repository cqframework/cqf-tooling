package org.opencds.cqf.tooling.dateroller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.gson.JsonObject;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataDateRollerUtils {
    private static Logger logger = LoggerFactory.getLogger(ResourceDataDateRoller.class);

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

    public static LocalDate getOldDateFromR4Resource(org.hl7.fhir.r4.model.Resource r4Resource, Field field) {
        String strDateToRoll = r4Resource.getNamedProperty(field.getName()).getValues().get(0).toString();
        return stringDateFromR4ResourceToLocalDate(strDateToRoll);
    }

    public static LocalDate getLocalDateFromPeriod(Property period, String periodPosition) {
        DateTimeType startTimeType = getR4DateTimeTypeFromPeriod(period, periodPosition);
        return stringDateFromR4ResourceToLocalDate(getR4DateTimeTypeFromPeriod(period, periodPosition).toString());
    }

    public static LocalDate stringDateFromR4ResourceToLocalDate(String strDateToRoll) {
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

    public static Property createNewR4PropertyFromR4Resource(Resource r4Resource, Field field, LocalDate newLocalDate) {
        DateTimeType ddType = new DateTimeType();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date newDate = new Date(newLocalDate.toEpochDay());
        ddType.setValue(Date.from(newLocalDate.atStartOfDay(defaultZoneId).toInstant()));
        Property oldProperty = r4Resource.getNamedProperty(field.getName());
        return new Property(oldProperty.getName(), oldProperty.getTypeCode(), oldProperty.getDefinition(), oldProperty.getMinCardinality(), oldProperty.getMaxCardinality(), new ArrayList<>());
    }

    public static IBaseResource jsonToIBaseResource(JsonObject jsonObject) {
//        ResourceUtils.
        return null;
//        IBaseResource baseResource = IBaseResource;
    }

    public static DateTimeType getR4DateTimeTypeFromPeriod(Property period, String periodPosition) {
        return (DateTimeType) period.getValues().get(0).getChildByName(periodPosition).getValues().get(0);
    }

    public static void incrementLastUpdated(Resource r4Resource) {
        List<Extension> extensionList = new ArrayList<>();
        Extension newDDRExtension = new Extension();
        newDDRExtension.setUrl("http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller");

        Property extension = r4Resource.getChildByName("extension");
        List<Base> extValues = extension.getValues();
        for (Base extValue : extValues) {
            List<Base> urlBase = extValue.getChildByName("url").getValues();
            String url = ((UriType) urlBase.get(0)).getValue();
            if (url.equalsIgnoreCase("http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller")) {
                for (Base base : extValue.getChildByName("extension").getValues()) {
                    String extensionUrlValue = base.getChildByName("url").getValues().get(0).toString();
                    if (extensionUrlValue.contains("dateLastUpdated")) {        // Set new dateLastUpdated
                        DateTimeType ddType = new DateTimeType();
                        String nowString = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now());
                        ddType.setValueAsString(nowString);
                        Extension newLastUpdatedExtension = new Extension("dateLastUpdated");
                        newLastUpdatedExtension.setValue(ddType);
                        newDDRExtension.addExtension(newLastUpdatedExtension);
                    } else if (extensionUrlValue.contains("frequency")) {
                        newDDRExtension.addExtension((Extension) base);
                    }
                }
                extensionList.add(newDDRExtension);
            } else {        //add all current non-dataDateRoller extensions
                extensionList.add((Extension) extValue);
            }
        }
        DomainResource dResource = (DomainResource) r4Resource;
        dResource.setExtension(extensionList);
    }
}
