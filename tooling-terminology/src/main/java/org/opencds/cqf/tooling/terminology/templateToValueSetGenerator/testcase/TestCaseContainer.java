package org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.testcase;

import java.util.ArrayList;
import java.util.List;

public class TestCaseContainer {

    // Likely old and from a different approach to the problem.

    private String containedTestCase;
    public List<TestCaseOld> containedTestCases;

    public void pushContainedTestCase(TestCaseOld pushedCase) {
        this.containedTestCases.add(pushedCase);
    }

    public void setContainedTestCases(List<TestCaseOld> containedTestCases) {
        this.containedTestCases = containedTestCases;
    }

    public List<TestCaseOld> getContainedTestCases() { return this.containedTestCases; }

    public TestCaseContainer() {
        this.containedTestCases = new ArrayList<TestCaseOld>();
    }

    public String getContainedTestCase() { return this.containedTestCase; }

    public void setContainedTestCase(String containedTestCase) {
        this.containedTestCase = containedTestCase;
    }

}
