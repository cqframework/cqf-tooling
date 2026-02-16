package org.opencds.cqf.tooling.dateroller.r4;

import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.dateroller.DataDateRollerUtils;
import org.opencds.cqf.tooling.dateroller.ResourceDataDateRoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DataDateRollerUtilsR4 {
    private static Logger logger = LoggerFactory.getLogger(ResourceDataDateRoller.class);

    public static LocalDate rollDate(LocalDate dateToRoll, DataDateRollerSettingsR4 dataDateRollerSettings) {
        return dateToRoll.plusDays(LocalDate.now().getLong(ChronoField.EPOCH_DAY) - dataDateRollerSettings.getLastDateUpdated().getLong(ChronoField.EPOCH_DAY));
    }

    public static boolean isCurrentDateGreaterThanInterval(DataDateRollerSettingsR4 dataDateRollerSettings) {
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

    public static LocalDate getOldDateFromResource(Resource resource, Field field) {
        String strDateToRoll = resource.getNamedProperty(field.getName()).getValues().get(0).toString();
        return DataDateRollerUtils.stringDateFromResourceToLocalDate(strDateToRoll);
    }

    public static LocalDate getLocalDateFromPeriod(Property period, String periodPosition) {
        return DataDateRollerUtils. stringDateFromResourceToLocalDate(getDateTimeTypeFromPeriod(period, periodPosition).toString());
    }

    public static DateTimeType getDateTimeTypeFromPeriod(Property period, String periodPosition) {
        return (DateTimeType) period.getValues().get(0).getChildByName(periodPosition).getValues().get(0);
    }

    public static void incrementLastUpdated(Resource resource) {
        List<Extension> extensionList = new ArrayList<>();
        Extension newDDRExtension = new Extension();
        newDDRExtension.setUrl("http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller");

        Property extension = resource.getChildByName("extension");
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
        DomainResource dResource = (DomainResource) resource;
        dResource.setExtension(extensionList);
    }
}
