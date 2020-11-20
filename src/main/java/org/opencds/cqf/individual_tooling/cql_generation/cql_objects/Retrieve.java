package org.opencds.cqf.individual_tooling.cql_generation.cql_objects;

public class Retrieve {
    private String resourceType;

    public Retrieve(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public Retrieve() {
        
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public String toString() {
        String content = "[" + resourceType + "] " + resourceType + "\n";
        return content;
    }
}