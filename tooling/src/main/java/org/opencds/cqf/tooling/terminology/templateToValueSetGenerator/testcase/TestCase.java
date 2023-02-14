package org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.testcase;

public class TestCase {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public DataElement getInputDataElement() {
        return inputDataElement;
    }

    public void setInputDataElement(DataElement inputDataElement) {
        this.inputDataElement = inputDataElement;
    }
    public DataElement getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(DataElement expectedResult) {
        this.expectedResult = expectedResult;
    }

    private String id;
    private String name;
    private String description;
    private String input;
    private DataElement inputDataElement;
    private DataElement expectedResult;
}
