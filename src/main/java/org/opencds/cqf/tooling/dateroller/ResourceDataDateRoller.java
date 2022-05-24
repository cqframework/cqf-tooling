package org.opencds.cqf.tooling.dateroller;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResourceDataDateRoller {
    private static Logger logger = LoggerFactory.getLogger(ResourceDataDateRoller.class);

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

    public static void rollDatesInR4Resource(IBaseResource resource) {
        org.hl7.fhir.r4.model.Resource r4Resource = (org.hl7.fhir.r4.model.Resource) resource;
        logger.info("resource having date rolled:  " + r4Resource.getNamedProperty("id").getValues().get(0));
        DataDateRollerSettings dataDateRollerSettings = new DataDateRollerSettings();
        dataDateRollerSettings.populateDataDateRollerSettings(r4Resource);
        if (null != dataDateRollerSettings) {
            if (DataDateRollerUtils.isCurrentDateGreaterThanInterval(dataDateRollerSettings)) {
                Field[] fields = r4Resource.getClass().getDeclaredFields();
                for (Field field : fields) {
                    try {
                        Property resourceProperty = r4Resource.getNamedProperty(field.getName());
                        if (null != resourceProperty) {
                            String propertyTypeCode = resourceProperty.getTypeCode();
                            if (null != propertyTypeCode && !propertyTypeCode.isEmpty()) {
                                if (propertyTypeCode.toLowerCase().contains("datetime") ||
                                        propertyTypeCode.toLowerCase().contains("date") ||
                                        propertyTypeCode.toLowerCase().contains("dateType") ||
                                        propertyTypeCode.toLowerCase().contains("period") ||
                                        propertyTypeCode.toLowerCase().contains("timing") ||
                                        propertyTypeCode.toLowerCase().contains("instant")) {
                                    if (!resourceProperty.getValues().isEmpty() &&
                                            !resourceProperty.getValues().get(0).fhirType().equalsIgnoreCase("period")) {
                                        LocalDate dateToRole = DataDateRollerUtils.getOldDateFromR4Resource(r4Resource, field);
                                        if(null == dateToRole){
                                            continue;
                                        }
                                        LocalDate newLocalDate = DataDateRollerUtils.rollDate(dateToRole, dataDateRollerSettings);
                                        DateTimeType ddType = new DateTimeType();
                                        ZoneId defaultZoneId = ZoneId.systemDefault();
                                        ddType.setValue(Date.from(newLocalDate.atStartOfDay(defaultZoneId).toInstant()));
                                        Property oldProperty = r4Resource.getNamedProperty(field.getName());
                                        List<Base> values = new ArrayList<>();
                                        values.add(0, ddType);
                                        Property newProperty = new Property(oldProperty.getName(), oldProperty.getTypeCode(), oldProperty.getDefinition(), oldProperty.getMinCardinality(), oldProperty.getMaxCardinality(), values);
                                        Base newBase = r4Resource.makeProperty(newProperty.getName().hashCode(), newProperty.getName());
                                        if (newProperty.getTypeCode().contains("dateTime")) {
                                            ((DateTimeType) newBase).setValue(Date.from(newLocalDate.atStartOfDay(defaultZoneId).toInstant()));
                                        } else if (newProperty.getTypeCode().contains("date")) {
                                            ((DateType) newBase).setValue(Date.from(newLocalDate.atStartOfDay(defaultZoneId).toInstant()));
                                        }
                                     } else if (null != resourceProperty.getValues() &&
                                            resourceProperty.getValues().size() > 0 &&
                                             resourceProperty.getValues().get(0).fhirType().equalsIgnoreCase("period")) {
                                        handlePeriod(r4Resource, dataDateRollerSettings, field.getName());
                                    }
                                }
                            }
                            if (field.getName().contains("dispenseRequest")) {
                                if(null != r4Resource.getNamedProperty(field.getName()).getValues() &&
                                        r4Resource.getNamedProperty(field.getName()).getValues().size() > 0 &&
                                        null != r4Resource.getNamedProperty(field.getName()).getValues().get(0).getNamedProperty("validityPeriod") &&
                                        null != r4Resource.getNamedProperty(field.getName()).getValues().get(0).getNamedProperty("validityPeriod").getValues() &&
                                        r4Resource.getNamedProperty(field.getName()).getValues().get(0).getNamedProperty("validityPeriod").getValues().size() > 0) {
                                    Period newPeriod = rollPeriodDates(r4Resource.getNamedProperty(field.getName()).getValues().get(0).getNamedProperty("validityPeriod"), dataDateRollerSettings);
                                    MedicationRequest medReq = (MedicationRequest) r4Resource;
                                    medReq.getDispenseRequest().setValidityPeriod(newPeriod);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.debug(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                DataDateRollerUtils.incrementLastUpdated(r4Resource);
            }
        }
    }

    private static void handlePeriod(Resource r4Resource, DataDateRollerSettings dataDateRollerSettings, String fieldName) {
        Property oldPeriodProperty = r4Resource.getNamedProperty(fieldName);
        Period newPeriod = rollPeriodDates(oldPeriodProperty, dataDateRollerSettings);
        Base newBase = r4Resource.makeProperty(fieldName.hashCode(), fieldName);
        ((Period) newBase).setEnd(newPeriod.getEnd());
        ((Period) newBase).setStart(newPeriod.getStart());
        System.out.println();
    }

    private static Period rollPeriodDates(Property period, DataDateRollerSettings dataDateRollerSettings) {
        LocalDate startLocalDate = DataDateRollerUtils.getLocalDateFromPeriod(period, "start");
        LocalDate endLocalDate = DataDateRollerUtils.getLocalDateFromPeriod(period, "end");
        startLocalDate = DataDateRollerUtils.rollDate(startLocalDate, dataDateRollerSettings);
        endLocalDate = DataDateRollerUtils.rollDate(endLocalDate, dataDateRollerSettings);
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Period newPeriod = new Period();
        newPeriod.setStart(Date.from(startLocalDate.atStartOfDay(defaultZoneId).toInstant()));
        newPeriod.setEnd(Date.from(endLocalDate.atStartOfDay(defaultZoneId).toInstant()));
        return newPeriod;
    }


    public static void rollDatesInStu3Resource(IBaseResource resource) {
        org.hl7.fhir.dstu3.model.Resource stu3Resource = (org.hl7.fhir.dstu3.model.Resource) resource;
    }
}
