package org.opencds.cqf.terminology.r4;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationalMeta {

    private String canonicalUrl;
    private String copyright;
    private String jurisdiction;
    private String publisher;
    private String approvalDate;
    private String effectivePeriodStart;
    private String effectivePeriodEnd;
    private String lastReviewDate;
    private String authorName;
    private String authorTelecomSystem;
    private String authorTelecomValue;

    private String snomedVersion;
}
