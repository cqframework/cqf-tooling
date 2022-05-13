package org.opencds.cqf.tooling.dateroller;

import java.time.LocalDate;

public class DataDateRollerSettings {
    private LocalDate lastDateUpdated;
    private  Float durationLength;
    private String durationUnitCode;

    public LocalDate getLastDateUpdated() {return lastDateUpdated;}
    public void setLastDateUpdated(LocalDate lastDateUpdated) {this.lastDateUpdated = lastDateUpdated;}

    public Float getDurationLength() {return durationLength;}
    public void setDurationLength(Float durationLength) {this.durationLength = durationLength;}

    public String getDurationUnitCode() {return durationUnitCode;}
    public void setDurationUnitCode(String durationUnitCode) {this.durationUnitCode = durationUnitCode;}
}
