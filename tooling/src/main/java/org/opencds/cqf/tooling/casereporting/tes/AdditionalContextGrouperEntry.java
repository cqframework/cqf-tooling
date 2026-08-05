package org.opencds.cqf.tooling.casereporting.tes;

public class AdditionalContextGrouperEntry extends TESGrouperEntry {
    private String additionalContextGrouperUrl;

    public String getAdditionalContextGrouperUrl() {
        return additionalContextGrouperUrl;
    }

    private String additionalContextGrouperTitle;

    public String getAdditionalContextGrouperTitle() {
        return additionalContextGrouperTitle;
    }

    private String additionalContextGrouperValueSetUrl;

    public String getAdditionalContextGrouperValueSetUrl() {
        return additionalContextGrouperValueSetUrl;
    }

    private String additionalContextGrouperValueSetTitle;

    public String getAdditionalContextGrouperValueSetTitle() {
        return additionalContextGrouperValueSetTitle;
    }

    private String additionalContextGrouperValueSetCodeSystem;

    public String getAdditionalContextGrouperValueSetCodeSystem() {
        return additionalContextGrouperValueSetCodeSystem;
    }

        private String additionalContextGrouperCode;

        public String getAdditionalContextGrouperCode() {
            return additionalContextGrouperCode;
        }

        private String additionalContextGrouperCodeDisplay;

        public String getAdditionalContextGrouperCodeDisplay() {
            return additionalContextGrouperCodeDisplay;
        }

        private String additionalContextGrouperCodeSystemUrl;

        public String getAdditionalContextGrouperCodeSystemUrl() {
            return additionalContextGrouperCodeSystemUrl;
        }

        public AdditionalContextGrouperEntry(
                String conditionGrouperUrl,
                String conditionGrouperTitle,
                String acGrouperCanonicalUrl,
                String additionalContextGrouperTitle,
                String additionalContextGrouperValueSetUrl,
                String additionalContextGrouperValueSetTitle,
                String additionalContextGrouperValueSetCodeSystem,
                String additionalContextGrouperCode,
                String additionalContextGrouperCodeDisplay,
                String additionalContextGrouperCodeSystemUrl) {
            super(conditionGrouperUrl, conditionGrouperTitle);
            this.additionalContextGrouperUrl = acGrouperCanonicalUrl;
            this.additionalContextGrouperTitle = additionalContextGrouperTitle;
            this.additionalContextGrouperValueSetUrl = additionalContextGrouperValueSetUrl;
            this.additionalContextGrouperValueSetTitle = additionalContextGrouperValueSetTitle;
            this.additionalContextGrouperValueSetCodeSystem = additionalContextGrouperValueSetCodeSystem;
            this.additionalContextGrouperCode = additionalContextGrouperCode;
            this.additionalContextGrouperCodeDisplay = additionalContextGrouperCodeDisplay;
            this.additionalContextGrouperCodeSystemUrl = additionalContextGrouperCodeSystemUrl;
        }
    }