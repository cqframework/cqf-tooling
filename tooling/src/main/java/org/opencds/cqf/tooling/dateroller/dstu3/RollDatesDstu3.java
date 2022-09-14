package org.opencds.cqf.tooling.dateroller.dstu3;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.dstu3.model.*;
import org.opencds.cqf.tooling.dateroller.DataDateRollerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RollDatesDstu3 {
    private static Logger logger = LoggerFactory.getLogger(RollDatesDstu3.class);
    public static void rollDatesInResource(IBaseResource resource) {
        Resource dstu3Resource = (Resource) resource;
        logger.info("resource having date rolled:  " + dstu3Resource.getNamedProperty("id").getValues().get(0));
        DataDateRollerSettingsDstu3 dataDateRollerSettings = new DataDateRollerSettingsDstu3();
        if (dataDateRollerSettings.populateDataDateRollerSettings(dstu3Resource)) {
            if (DataDateRollerUtilsDstu3.isCurrentDateGreaterThanInterval(dataDateRollerSettings)) {
                Field[] fields = dstu3Resource.getClass().getDeclaredFields();
                for (Field field : fields) {
                    try {
                        Property resourceProperty = dstu3Resource.getNamedProperty(field.getName());
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
                                        LocalDate dateToRole = DataDateRollerUtilsDstu3.getOldDateFromResource(dstu3Resource, field);
                                        if(null == dateToRole){
                                            continue;
                                        }
                                        LocalDate newLocalDate = DataDateRollerUtilsDstu3.rollDate(dateToRole, dataDateRollerSettings);
                                        DateTimeType ddType = new DateTimeType();
                                        ZoneId defaultZoneId = ZoneId.systemDefault();
                                        ddType.setValue(Date.from(newLocalDate.atStartOfDay(defaultZoneId).toInstant()));
                                        Property oldProperty = dstu3Resource.getNamedProperty(field.getName());
                                        List<Base> values = new ArrayList<>();
                                        values.add(0, ddType);
                                        Property newProperty = new Property(oldProperty.getName(), oldProperty.getTypeCode(), oldProperty.getDefinition(), oldProperty.getMinCardinality(), oldProperty.getMaxCardinality(), values);
                                        Base newBase = dstu3Resource.makeProperty(newProperty.getName().hashCode(), newProperty.getName());
                                        if (newProperty.getTypeCode().contains("dateTime")) {
                                            ((DateTimeType) newBase).setValue(Date.from(newLocalDate.atStartOfDay(defaultZoneId).toInstant()));
                                        } else if (newProperty.getTypeCode().contains("date")) {
                                            ((DateType) newBase).setValue(Date.from(newLocalDate.atStartOfDay(defaultZoneId).toInstant()));
                                        }
                                    } else if (null != resourceProperty.getValues() &&
                                            resourceProperty.getValues().size() > 0 &&
                                            resourceProperty.getValues().get(0).fhirType().equalsIgnoreCase("period")) {
                                        handlePeriod(dstu3Resource, dataDateRollerSettings, field.getName());
                                    }
                                }
                            }
                            if (field.getName().contains("dispenseRequest")) {
                                if(null != dstu3Resource.getNamedProperty(field.getName()).getValues() &&
                                        dstu3Resource.getNamedProperty(field.getName()).getValues().size() > 0 &&
                                        null != dstu3Resource.getNamedProperty(field.getName()).getValues().get(0).getNamedProperty("validityPeriod") &&
                                        null != dstu3Resource.getNamedProperty(field.getName()).getValues().get(0).getNamedProperty("validityPeriod").getValues() &&
                                        dstu3Resource.getNamedProperty(field.getName()).getValues().get(0).getNamedProperty("validityPeriod").getValues().size() > 0) {
                                    Period newPeriod = rollPeriodDates(dstu3Resource.getNamedProperty(field.getName()).getValues().get(0).getNamedProperty("validityPeriod"), dataDateRollerSettings);
                                    MedicationRequest medReq = (MedicationRequest) dstu3Resource;
                                    medReq.getDispenseRequest().setValidityPeriod(newPeriod);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.debug(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                DataDateRollerUtilsDstu3.incrementLastUpdated(dstu3Resource);
            }
        }
    }

    private static void handlePeriod(Resource resource, DataDateRollerSettingsDstu3 dataDateRollerSettings, String fieldName) {
        Property oldPeriodProperty = resource.getNamedProperty(fieldName);
        Period newPeriod = rollPeriodDates(oldPeriodProperty, dataDateRollerSettings);
        Base newBase = resource.makeProperty(fieldName.hashCode(), fieldName);
        ((Period) newBase).setEnd(newPeriod.getEnd());
        ((Period) newBase).setStart(newPeriod.getStart());
    }

    private static Period rollPeriodDates(Property period, DataDateRollerSettingsDstu3 dataDateRollerSettings) {
        LocalDate startLocalDate = DataDateRollerUtilsDstu3.getLocalDateFromPeriod(period, "start");
        LocalDate endLocalDate = DataDateRollerUtilsDstu3.getLocalDateFromPeriod(period, "end");
        startLocalDate = DataDateRollerUtilsDstu3.rollDate(startLocalDate, dataDateRollerSettings);
        endLocalDate = DataDateRollerUtilsDstu3.rollDate(endLocalDate, dataDateRollerSettings);
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Period newPeriod = new Period();
        newPeriod.setStart(Date.from(startLocalDate.atStartOfDay(defaultZoneId).toInstant()));
        newPeriod.setEnd(Date.from(endLocalDate.atStartOfDay(defaultZoneId).toInstant()));
        return newPeriod;
    }


}
