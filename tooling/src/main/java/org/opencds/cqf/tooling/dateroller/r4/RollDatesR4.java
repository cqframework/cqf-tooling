package org.opencds.cqf.tooling.dateroller.r4;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.dateroller.DataDateRollerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RollDatesR4 {
    private static Logger logger = LoggerFactory.getLogger(RollDatesR4.class);
    public static void rollDatesInResource(IBaseResource resource) {
        Resource r4Resource = (Resource) resource;
        logger.info("resource having date rolled:  " + r4Resource.getNamedProperty("id").getValues().get(0));
        DataDateRollerSettingsR4 dataDateRollerSettings = new DataDateRollerSettingsR4();
        if (dataDateRollerSettings.populateDataDateRollerSettings(r4Resource)) {
            if (DataDateRollerUtilsR4.isCurrentDateGreaterThanInterval(dataDateRollerSettings)) {
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
                                        LocalDate dateToRole = DataDateRollerUtilsR4.getOldDateFromResource(r4Resource, field);
                                        if(null == dateToRole){
                                            continue;
                                        }
                                        LocalDate newLocalDate = DataDateRollerUtilsR4.rollDate(dateToRole, dataDateRollerSettings);
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
                DataDateRollerUtilsR4.incrementLastUpdated(r4Resource);
            }
        }
    }

    private static void handlePeriod(Resource r4Resource, DataDateRollerSettingsR4 dataDateRollerSettings, String fieldName) {
        Property oldPeriodProperty = r4Resource.getNamedProperty(fieldName);
        Period newPeriod = rollPeriodDates(oldPeriodProperty, dataDateRollerSettings);
        Base newBase = r4Resource.makeProperty(fieldName.hashCode(), fieldName);
        ((Period) newBase).setEnd(newPeriod.getEnd());
        ((Period) newBase).setStart(newPeriod.getStart());
    }

    private static Period rollPeriodDates(Property period, DataDateRollerSettingsR4 dataDateRollerSettings) {
        LocalDate startLocalDate = DataDateRollerUtilsR4.getLocalDateFromPeriod(period, "start");
        LocalDate endLocalDate = DataDateRollerUtilsR4.getLocalDateFromPeriod(period, "end");
        startLocalDate = DataDateRollerUtilsR4.rollDate(startLocalDate, dataDateRollerSettings);
        endLocalDate = DataDateRollerUtilsR4.rollDate(endLocalDate, dataDateRollerSettings);
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Period newPeriod = new Period();
        newPeriod.setStart(Date.from(startLocalDate.atStartOfDay(defaultZoneId).toInstant()));
        newPeriod.setEnd(Date.from(endLocalDate.atStartOfDay(defaultZoneId).toInstant()));
        return newPeriod;
    }
}
