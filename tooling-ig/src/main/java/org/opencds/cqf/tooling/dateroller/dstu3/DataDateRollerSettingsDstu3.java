package org.opencds.cqf.tooling.dateroller.dstu3;

import org.hl7.fhir.dstu3.model.*;
import java.time.LocalDate;
import java.util.List;

public class DataDateRollerSettingsDstu3 {
    private LocalDate lastDateUpdated;
    private  Float durationLength;
    private String durationUnitCode;

    public LocalDate getLastDateUpdated() {return lastDateUpdated;}
    public void setLastDateUpdated(LocalDate lastDateUpdated) {this.lastDateUpdated = lastDateUpdated;}

    public Float getDurationLength() {return durationLength;}
    public void setDurationLength(Float durationLength) {this.durationLength = durationLength;}

    public String getDurationUnitCode() {return durationUnitCode;}
    public void setDurationUnitCode(String durationUnitCode) {this.durationUnitCode = durationUnitCode;}


    public boolean populateDataDateRollerSettings(Resource resource) {
        Property extension = resource.getChildByName("extension");
        List<Base> extValues = extension.getValues();
        boolean extensionFound = false;
        for (Base extValue : extValues) {
            List<Base> urlBase = extValue.getChildByName("url").getValues();
            String url = ((UriType) urlBase.get(0)).getValue();
            if (url.equalsIgnoreCase("http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller")) {
                extensionFound = true;
                String firstExtensionUrlValue = extValue.getChildByName("extension").getValues().get(0).getChildByName("url").getValues().get(0).toString();
                String secondExtensionUrlValue = extValue.getChildByName("extension").getValues().get(1).getChildByName("url").getValues().get(0).toString();
                if (null != firstExtensionUrlValue && null != secondExtensionUrlValue) {
                    getDataDateRollerSettings(extValue, firstExtensionUrlValue, 0);
                    getDataDateRollerSettings(extValue, secondExtensionUrlValue, 1);
                } else {
                    throw new IllegalArgumentException("Extension http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller is not formatted correctly.");
                }
            }
        }
        return extensionFound;
    }

    private void getDataDateRollerSettings(Base extValue, String extensionUrlValue, int extensionPosition) {
        if (extensionUrlValue.contains("dateLastUpdated")) {
            getLastUpdatedSettings(extValue.getChildByName("extension").getValues().get(extensionPosition).getNamedProperty("value").getValues().get(0));
        }
        if (extensionUrlValue.contains("frequency")) {
            getFrequencySettings(extValue.getChildByName("extension").getValues().get(extensionPosition).getNamedProperty("value").getValues().get(0));
        }
    }

    private void getFrequencySettings(Base frequency) {
        this.setDurationUnitCode(frequency.getNamedProperty("code").getValues().get(0).toString());
        String codeValue = frequency.getNamedProperty("value").getValues().get(0).toString();
        this.setDurationLength(Float.parseFloat(codeValue.substring(codeValue.indexOf("[") + 1, codeValue.indexOf("]"))));
    }


    private void getLastUpdatedSettings(Base lastUpdated) {
        String lastUpdatedStringDate = lastUpdated.toString();
        lastUpdatedStringDate = lastUpdatedStringDate.substring(lastUpdatedStringDate.indexOf("[") + 1, lastUpdatedStringDate.indexOf("]"));
        LocalDate newLocalDate = LocalDate.parse(lastUpdatedStringDate);
        this.setLastDateUpdated(newLocalDate);
    }
}
