package org.opencds.cqf.individual_tooling.cql_generation.drool.json_objects;

public class PredicatePartConcepts {
    private OpenCdsConceptDTO openCdsConceptDTO;

    public OpenCdsConceptDTO getOpenCdsConceptDTO() {
        return openCdsConceptDTO;
    }

    public void setOpenCdsConceptDTO(OpenCdsConceptDTO openCdsConceptDTO) {
        this.openCdsConceptDTO = openCdsConceptDTO;
    }

    public boolean hasOpenCdsConceptDTO() {
        if (this.openCdsConceptDTO != null) {
            return true;
        }
        else return false;
    }
}
