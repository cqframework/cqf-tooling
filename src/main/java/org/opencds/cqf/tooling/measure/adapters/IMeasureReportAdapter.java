package org.opencds.cqf.tooling.measure.adapters;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
/**
 * @author Adam Stevenson
 */
public interface IMeasureReportAdapter {
    String getReportType();
    String getPatientId();
    String getMeasureId();
    Date getPeriodStart();
    Date getPeriodEnd();
    BigDecimal getGroupScore(String groupId);
    List<Group> getGroups();
/**
 * @author Adam Stevenson
 */
    public class Group {
        String name;
        BigDecimal score;

        public String getName() { return name; }
        public BigDecimal getScore() { return score; }
    }
}
