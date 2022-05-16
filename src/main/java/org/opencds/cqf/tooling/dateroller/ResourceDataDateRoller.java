package org.opencds.cqf.tooling.dateroller;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResourceDataDateRoller {
    private static Logger logger = LoggerFactory.getLogger(ResourceDataDateRoller.class);
    //        logger.debug("test");

    /*
    Opioid r4 Resources:
    patient
    MedicationRequest
    MedicationStatement
    Condition
    Encounter
    Observation

*/
    public static void rollBundleDates(FhirContext fhirContext, IBaseResource iBaseResource) {
        switch (fhirContext.getVersion().getVersion().name()) {
            case "R4":
                ArrayList<Resource> r4ResourceArrayList = BundleUtils.getR4ResourcesFromBundle((org.hl7.fhir.r4.model.Bundle) iBaseResource);
                r4ResourceArrayList.forEach(resource -> {
                    rollDatesInR4Resource(resource);
                });
                break;
            case "Stu3":
                ArrayList<org.hl7.fhir.dstu3.model.Resource> stu3resourceArrayList = BundleUtils.getStu3ResourcesFromBundle((org.hl7.fhir.dstu3.model.Bundle) iBaseResource);
                stu3resourceArrayList.forEach(resource -> {
                    rollDatesInStu3Resource(resource);
                });
                break;
        }
    }

    private static DataDateRollerSettings populateDataDateRollerSettings(org.hl7.fhir.r4.model.Resource resource){
        DataDateRollerSettings dataDateRollerSettings = new DataDateRollerSettings();
        Property extension = resource.getChildByName("extension");
        List <Base> extValues = extension.getValues();
        for(Base extValue: extValues){
            List <Base> urlBase = extValue.getChildByName("url").getValues();
            String url = ((UriType) urlBase.get(0)).getValue();
            if(url.equalsIgnoreCase("http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller")){
                String firstExtensionUrlValue = extValue.getChildByName("extension").getValues().get(0).getChildByName("url").getValues().get(0).toString();
                String secondExtensionUrlValue = extValue.getChildByName("extension").getValues().get(1).getChildByName("url").getValues().get(0).toString();
                if(null != firstExtensionUrlValue && null != secondExtensionUrlValue) {
                    dataDateRollerSettings = getDataDateRollerSettings(dataDateRollerSettings, extValue, firstExtensionUrlValue, 0);
                    dataDateRollerSettings = getDataDateRollerSettings(dataDateRollerSettings, extValue, secondExtensionUrlValue,1);
                }
                else{
                    throw new IllegalArgumentException("Extension http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller is not formatted correctly.");
                }
                return dataDateRollerSettings;
            }
        }
        System.out.println("ghost");
        return null;
    }

    private static DataDateRollerSettings getDataDateRollerSettings(DataDateRollerSettings dataDateRollerSettings, Base extValue, String extensionUrlValue, int extensionPosition) {
        if(extensionUrlValue.contains("dateLastUpdated")) {
            dataDateRollerSettings = getLastUpdatedSettings(dataDateRollerSettings, extValue.getChildByName("extension").getValues().get(extensionPosition).getNamedProperty("value").getValues().get(0));
        }
        if(extensionUrlValue.contains("frequency")){
                dataDateRollerSettings = getFrequencySettings(dataDateRollerSettings, extValue.getChildByName("extension").getValues().get(extensionPosition).getNamedProperty("value").getValues().get(0));
        }
        return dataDateRollerSettings;
    }

    private static DataDateRollerSettings getFrequencySettings(DataDateRollerSettings dataDateRollerSettings, Base frequency){
        dataDateRollerSettings.setDurationUnitCode(frequency.getNamedProperty("code").getValues().get(0).toString());
        String codeValue = frequency.getNamedProperty("value").getValues().get(0).toString();
        dataDateRollerSettings.setDurationLength(Float.parseFloat(codeValue.substring(codeValue.indexOf("[") + 1, codeValue.indexOf("]"))));
        return dataDateRollerSettings;
    }
    private static DataDateRollerSettings getLastUpdatedSettings(DataDateRollerSettings dataDateRollerSettings, Base lastUpdated){
        String lastUpdatedStringDate = lastUpdated.toString();
        lastUpdatedStringDate = lastUpdatedStringDate.substring(lastUpdatedStringDate.indexOf("[") + 1, lastUpdatedStringDate.indexOf("]"));
        LocalDate newLocalDate = LocalDate.parse(lastUpdatedStringDate);
        dataDateRollerSettings.setLastDateUpdated(newLocalDate);
        return dataDateRollerSettings;
    }

    public static void rollDatesInR4Resource(IBaseResource resource) {
        org.hl7.fhir.r4.model.Resource r4Resource = (org.hl7.fhir.r4.model.Resource) resource;
/*
        if(r4Resource.getResourceType().name().equalsIgnoreCase("observation")){
            Observation obs = (Observation) r4Resource;
            Extension dataDateRoller = obs.getExtensionByUrl("http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller");
            if(null != dataDateRoller) {
                DateTimeType newBaseEffectiveDate = new DateTimeType();
                obs.setEffective(newBaseEffectiveDate);
            }
        }
*/
        DataDateRollerSettings dataDateRollerSettings = populateDataDateRollerSettings(r4Resource);
        if(null != dataDateRollerSettings) {
            if (DataDateRollerUtils.isCurrentDateGreaterThanInterval(dataDateRollerSettings)) {
                Field[] fields = r4Resource.getClass().getDeclaredFields();

                for (Field field : fields) {
                    if ((field.getType().getName().equals("org.hl7.fhir.r4.model.DateTimeType")) ||
                            (field.getType().getName().equals("org.hl7.fhir.r4.model.DateType"))) {
                        System.out.println(r4Resource.getChildByName(field.getName()).getValues().get(0));
                    }
                    if (field.getType().getName().equals("org.hl7.fhir.r4.model.MedicationRequest$MedicationRequestDispenseRequestComponent")) {
                        System.out.println("From getR4DateTimeType start:  " + getR4DateTimeTypeFromPeriod(r4Resource.getChildByName(field.getName()).getValues().get(0).getChildByName("validityPeriod"), "start"));
                        System.out.println("From getR4DateTimeType end:  " + getR4DateTimeTypeFromPeriod(r4Resource.getChildByName(field.getName()).getValues().get(0).getChildByName("validityPeriod"), "end"));
                    }
                    if (field.getType().getName().equals("org.hl7.fhir.r4.model.Period")) {
                        System.out.println(getR4DateTimeTypeFromPeriod(r4Resource.getChildByName(field.getName()), "start"));
                        System.out.println(getR4DateTimeTypeFromPeriod(r4Resource.getChildByName(field.getName()), "end"));
                    }
                    //Observation.effectiveDate comes in like this ??
                    if (field.getName().equalsIgnoreCase("effective") && field.getType().getName().equals("org.hl7.fhir.r4.model.Type")) {
                        try {
                            String effectiveStringDate = r4Resource.getNamedProperty("effective").getValues().get(0).toString();
                            effectiveStringDate = effectiveStringDate.substring(effectiveStringDate.indexOf("[") + 1, effectiveStringDate.indexOf("]"));
                            LocalDate newEffectiveDate = DataDateRollerUtils.rollDate(LocalDate.parse(effectiveStringDate), dataDateRollerSettings);
                            DateTimeType newBaseEffectiveDate = new DateTimeType();
                            ZoneId defaultZoneId = ZoneId.systemDefault();
                            newBaseEffectiveDate.setValue(Date.from(newEffectiveDate.atStartOfDay(defaultZoneId).toInstant()));
                            r4Resource.getNamedProperty("effective").getValues().set(0, (Base) newBaseEffectiveDate);
                        } catch (Exception ex) {
                            System.out.println("Rolling date in field " + field.getName() + " did not work due to unknown error.");
                            continue;
                        }
                    }
                }
            }
        }
    }

    private static void setR4ResourceNewRolledDate(org.hl7.fhir.r4.model.Resource r4Resource){
        String resourceType = r4Resource.fhirType();
        switch(r4Resource.fhirType()){
            case "Observation":
//                (Observation)r4Resource.
                break;
        }
    }

    private static DateTimeType getR4DateTimeTypeFromPeriod(Property period, String periodPosition){
        return (DateTimeType)period.getValues().get(0).getChildByName(periodPosition).getValues().get(0);
    }

    public static void rollDatesInStu3Resource(IBaseResource resource) {
        org.hl7.fhir.dstu3.model.Resource stu3Resource = (org.hl7.fhir.dstu3.model.Resource) resource;
    }

    public static void main(String args []){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = LocalDate.parse("2022-01-31").atStartOfDay();
        LocalDateTime effective = LocalDate.parse("2022-04-01").atStartOfDay();
        LocalDate newDate = now.minusDays(last.minusDays(effective.getLong(ChronoField.EPOCH_DAY)).getLong(ChronoField.EPOCH_DAY)).toLocalDate();
        DateFormat dFormat = new SimpleDateFormat("yyyy-mm-dd");

        DataDateRollerSettings ddrSettings = new DataDateRollerSettings();
        ddrSettings.setLastDateUpdated(LocalDate.parse("2022-04-30"));
        ddrSettings.setDurationLength(Float.parseFloat("40.0"));
        ddrSettings.setDurationUnitCode("d");
        System.out.println(DataDateRollerUtils.isCurrentDateGreaterThanInterval(ddrSettings));


    }
}
