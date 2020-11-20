package org.opencds.cqf.individual_tooling.cql_generation.drool.json_objects;

public class PredicatePart {
    private String partType;
    private String partAlias;
    private String text;
    private DIN dataInputNode;
    private String resourceType;
    private CriteriaResourceParamDTO criteriaResourceParamDTO;
    private PredicatePartConcepts[] predicatePartConcepts;
    private SourcePredicatePartDTO sourcePredicatePartDTO;

    public String getPartType() {
        return partType;
    }

    public void setPartType(String partType) {
        this.partType = partType;
    }

    public String getPartAlias() {
        return partAlias;
    }

    public void setPartAlias(String partAlias) {
        this.partAlias = partAlias;
    }

    public boolean hasPartAlias() {
        return this.partAlias != null;
    }

    public DIN getDataInputNode() {
        return dataInputNode;
    }

    public void setDataInputNode(DIN dataInputNode) {
        this.dataInputNode = dataInputNode;
    }

    public boolean hasDataInputNode() {
        if (this.dataInputNode != null) {
            return true;
        }
        else return false;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public CriteriaResourceParamDTO getCriteriaResourceParamDTO() {
        return criteriaResourceParamDTO;
    }

    public void setCriteriaResourceParamDTO(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        this.criteriaResourceParamDTO = criteriaResourceParamDTO;
    }

    public boolean hasCriteriaResourceParamDTO() {
        if (this.criteriaResourceParamDTO != null) {
            return true;
        }
        else return false;
    }

    public PredicatePartConcepts[] getPredicatePartConcepts() {
        return predicatePartConcepts;
    }

    public void setPredicatePartConcepts(PredicatePartConcepts[] predicatePartConcepts) {
        this.predicatePartConcepts = predicatePartConcepts;
    }

    public boolean hasPredicatePartConcepts() {
        if (this.predicatePartConcepts != null && this.predicatePartConcepts.length > 0) {
            return true;
        }
        else return false;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public SourcePredicatePartDTO getSourcePredicatePartDTO() {
        return sourcePredicatePartDTO;
    }

    public void setSourcePredicatePartDTO(SourcePredicatePartDTO sourcePredicatePartDTO) {
        this.sourcePredicatePartDTO = sourcePredicatePartDTO;
    }

    public boolean hasSourcePredicatePartDTO() {
        if (this.sourcePredicatePartDTO != null) {
            return true;
        }
        else return false;
    }
}
