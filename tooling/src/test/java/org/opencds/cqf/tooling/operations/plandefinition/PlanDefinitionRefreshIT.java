package org.opencds.cqf.tooling.operations.plandefinition;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Measure;
import org.hl7.fhir.r5.model.PlanDefinition;
import org.opencds.cqf.tooling.operations.measure.MeasureRefresh;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class PlanDefinitionRefreshIT {

    FhirContext fhirContext = FhirContext.forR5Cached();

    @Test
    void testSingleMeasureWithoutUpdate() {
        PlanDefinition planDefinitionToRefresh = (PlanDefinition) fhirContext.newJsonParser().parseResource(USECRPLANDEFINITION);
        PlanDefinitionRefresh planDefinitionRefresh = new PlanDefinitionRefresh(fhirContext,
                "src/test/resources/org/opencds/cqf/tooling/testfiles/refreshIG/input/cql/");

        IBaseResource result = planDefinitionRefresh.refreshPlanDefinition(planDefinitionToRefresh);

        Assert.assertTrue(result instanceof PlanDefinition);
        PlanDefinition refreshedPlanDefinition = (PlanDefinition) result;

        // test date update
        Assert.assertTrue(DateUtils.isSameDay(new Date(), refreshedPlanDefinition.getDate()));

        // Contained Resource tests before update (should be the same)
        Assert.assertEquals(refreshedPlanDefinition.getContained().size(), planDefinitionToRefresh.getContained().size());
        Assert.assertEquals(StringUtils.deleteWhitespace(refreshedPlanDefinition.getContained().get(0).getId()),
                StringUtils.deleteWhitespace(planDefinitionToRefresh.getContained().get(0).getId()));

        // Extension tests before update (should be the same)
        Assert.assertEquals(refreshedPlanDefinition.getExtension().size(),
                planDefinitionToRefresh.getExtension().size());

        // Library tests before update (should be the same)
        Assert.assertEquals(refreshedPlanDefinition.getLibrary().size(), planDefinitionToRefresh.getLibrary().size());
    }

    @Test
    void testSingleMeasureWithUpdate() {
        PlanDefinition planDefinitionToRefresh = (PlanDefinition) fhirContext.newJsonParser().parseResource(USECRPLANDEFINITION);
        PlanDefinitionRefresh planDefinitionRefresh = new PlanDefinitionRefresh(fhirContext,
                "src/test/resources/org/opencds/cqf/tooling/testfiles/refreshIG/input/cql/");

        PlanDefinition beforeUpdate = planDefinitionToRefresh.copy();
        planDefinitionRefresh.refreshPlanDefinition(planDefinitionToRefresh);

        // test date update
        Assert.assertTrue(DateUtils.isSameDay(new Date(), planDefinitionToRefresh.getDate()));

        // Contained Resource tests before update (should not be the same)
        Assert.assertNotEquals(beforeUpdate.getContained().size(), planDefinitionToRefresh.getContained().size());
        /*Assert.assertNotEquals(StringUtils.deleteWhitespace(beforeUpdate.getContained().get(0).getId()),
                StringUtils.deleteWhitespace(measureToRefresh.getContained().get(0).getId()));*/

        // DataRequirement tests before update (should not be the same)
        Assert.assertNotEquals(beforeUpdate.getExtension().size(),
                planDefinitionToRefresh.getExtension().size());

        // Library tests before update (should not be the same)
        Assert.assertEquals(beforeUpdate.getLibrary().size(), planDefinitionToRefresh.getLibrary().size());
    }

    private final String USECRPLANDEFINITION = "{\n" +
            "  \"resourceType\": \"PlanDefinition\",\n" +
            "  \"id\": \"us-ecr-specification\",\n" +
            "  \"meta\": {\n" +
            "    \"profile\": [\n" +
            "      \"http://hl7.org/fhir/us/ecr/StructureDefinition/ersd-plandefinition\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"text\" : {\n" +
            "    \"status\" : \"extensions\",\n" +
            "    \"div\" : \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n    <table class=\\\"grid dict\\\">\\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Id: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">us-ecr-specification</td>\\n        </tr>\\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Url: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\"><a href=\\\"http://ersd.aimsplatform.org/fhir/PlanDefinition/us-ecr-specification\\\">http://ersd.aimsplatform.org/fhir/PlanDefinition/us-ecr-specification</a></td>\\n        </tr>\\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Version: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">2022.1.0</td>\\n        </tr>\\n        \\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Name: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">US_eCR_Specification</td>\\n        </tr>\\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Title: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">US eCR Specification</td>\\n        </tr>\\n        \\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Status: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">active</td>\\n        </tr>\\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Experimental: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">true</td>\\n        </tr>\\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Type: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">\\n                \\n                    \\n                        \\n                        <p style=\\\"margin-bottom: 5px;\\\">\\n                            <b>system: </b> <span><a href=\\\"http://terminology.hl7.org/CodeSystem/plan-definition-type\\\">http://terminology.hl7.org/CodeSystem/plan-definition-type</a></span>\\n                        </p>\\n                        \\n                        \\n                        <p style=\\\"margin-bottom: 5px;\\\">\\n                            <b>code: </b> <span>workflow-definition</span>\\n                        </p>\\n                        \\n                        \\n                        <p style=\\\"margin-bottom: 5px;\\\">\\n                            <b>display: </b> <span>Workflow Definition</span>\\n                        </p>\\n                        \\n                    \\n                \\n                \\n            </td>\\n        </tr>\\n        \\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Date: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">2023-03-31 12:32:29-0500</td>\\n        </tr>\\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Publisher: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">Centers for Disease Control and Prevention (CDC)</td>\\n        </tr>\\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Description: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">An example ersd PlanDefinition</td>\\n        </tr>\\n        \\n        \\n        \\n        \\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Jurisdiction: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">US</td>\\n        </tr>\\n        \\n        \\n        \\n        \\n        \\n        \\n        \\n        <tr>\\n            <th scope=\\\"row\\\"><b>Effective Period: </b></th>\\n            <td style=\\\"padding-left: 4px;\\\">2020-12-01..</td>\\n        </tr>\\n        \\n        \\n        \\n        <tr>\\n          <th scope=\\\"row\\\"><b>Related Artifacts: </b></th>\\n          <td style=\\\"padding-left: 4px;\\\">\\n            \\n            \\n            \\n            <p><b>Dependencies</b></p>\\n            <ul>\\n              \\n                <li><a href=\\\"http://ersd.aimsplatform.org/fhir/Library/rctc\\\">http://ersd.aimsplatform.org/fhir/Library/rctc</a></li>\\n              \\n            </ul>\\n            \\n            \\n            \\n            \\n            \\n          </td>\\n        </tr>\\n        \\n\\n        \\n        <tr>\\n          <th scope=\\\"row\\\"><b>Libraries: </b></th>\\n          <td style=\\\"padding-left: 4px;\\\">\\n            <table class=\\\"grid-dict\\\">\\n              \\n                <tr><td><a href=\\\"http://fhir.org/guides/cdc/opioid-cds/Library/OpioidCDSREC11\\\">http://fhir.org/guides/cdc/opioid-cds/Library/OpioidCDSREC11</a></td></tr>\\n              \\n            </table>\\n          </td>\\n        </tr>\\n        \\n\\n        \\n        <tr>\\n          <th scope=\\\"row\\\"><b>Actions: </b></th>\\n          <td style=\\\"padding-left: 4px;\\\">\\n            <table class=\\\"grid-dict\\\">\\n              \\n                <tr>\\n                  <td>\\n                    Start the reporting workflow in response to an encounter-start event<br/>\\n                    <b>When:</b> <i>named-event:</i> encounter-start<br/>\\n                    \\n                    <b>Then:</b>\\n                    \\n                      \\n                      \\n                    \\n                  </td>\\n                </tr>\\n              \\n                <tr>\\n                  <td>\\n                    Check suspected disorders for immediate reportability and setup jobs for future reportability checks.<br/>\\n                    \\n                    \\n                    <b>Then:</b>\\n                    \\n                      <table class=\\\"grid-dict\\\">\\n                        \\n                          <tr>\\n                            <td>\\n                              Check Trigger Codes based on Suspected Reportable and Lab Order Test Names Value set.<br/>\\n                              \\n                              <b>If:</b> <i>applicability:</i>  <i>(%suspectedDisorderConditions.exists() or %suspectedDisorderLabOrders.exists())</i><br/>\\n                              <b>Then:</b>\\n                              \\n                                \\n                                \\n                              \\n                            </td>\\n                          </tr>\\n                        \\n                          <tr>\\n                            <td>\\n                              <b> :</b> <br/>\\n                              \\n                              <b>If:</b> <i>applicability:</i>  <i>(%inprogressencounter.where((status = 'in-progress' and %encounterStartDate + 1 day * %normalReportingDuration &amp;gt;= now()) or (status = 'finished' and %encounterEndDate + 72 hours &amp;gt;= now())).select(true))</i><br/>\\n                              <b>Then:</b>\\n                              \\n                                \\n                                \\n                              \\n                            </td>\\n                          </tr>\\n                        \\n                          <tr>\\n                            <td>\\n                              <b> :</b> <br/>\\n                              \\n                              <b>If:</b> <i>applicability:</i>  <i>(%terminatedencounter.where((status = 'in-progress' and %encounterStartDate + 1 day * %normalReportingDuration &amp;lt; now()) or (status = 'finished' and %encounterEndDate + 72 hours &amp;lt; now())).select(true))</i><br/>\\n                              <b>Then:</b>\\n                              \\n                                \\n                                \\n                              \\n                            </td>\\n                          </tr>\\n                        \\n                          <tr>\\n                            <td>\\n                              <b> :</b> <br/>\\n                              \\n                              <b>If:</b> <i>applicability:</i>  <i>(%lateCompletedEncounter.exists(status = 'finished'))</i><br/>\\n                              <b>Then:</b>\\n                              \\n                                \\n                                \\n                              \\n                            </td>\\n                          </tr>\\n                        \\n                      </table>\\n                    \\n                  </td>\\n                </tr>\\n              \\n                <tr>\\n                  <td>\\n                    Check Reportability and setup jobs for future reportability checks.<br/>\\n                    \\n                    \\n                    <b>Then:</b>\\n                    \\n                      <table class=\\\"grid-dict\\\">\\n                        \\n                          <tr>\\n                            <td>\\n                              Check Trigger Codes based on RCTC Value sets.<br/>\\n                              \\n                              <b>If:</b> <i>applicability:</i>  <i>((%encounterStartDate + 1 day * %normalReportingDuration &amp;gt;= now()) and (%conditions.exists() or %encounters.exists() or %immunizations.exists() or %labOrders.exists() or %labTests.exists() or %labResults.exists() or %medicationAdministrations.exists() or %medicationOrders.exists() or %medicationDispenses.exists()))</i><br/>\\n                              <b>Then:</b>\\n                              \\n                                \\n                                \\n                              \\n                            </td>\\n                          </tr>\\n                        \\n                          <tr>\\n                            <td>\\n                              <b> :</b> <br/>\\n                              \\n                              <b>If:</b> <i>applicability:</i>  <i>(%lastReportSubmissionDate &amp;lt; now() - 72 hours)</i><br/>\\n                              <b>Then:</b>\\n                              \\n                                \\n                                \\n                              \\n                            </td>\\n                          </tr>\\n                        \\n                          <tr>\\n                            <td>\\n                              <b> :</b> <br/>\\n                              \\n                              <b>If:</b> <i>applicability:</i>  <i>(%inprogressencounter.where(status = 'in-progress' and %encounterStartDate + 1 day * %normalReportingDuration &amp;gt;= now() or (status = 'finished' and %encounterEndDate + 72 hours &amp;gt;= now())).exists())</i><br/>\\n                              <b>Then:</b>\\n                              \\n                                \\n                                \\n                              \\n                            </td>\\n                          </tr>\\n                        \\n                          <tr>\\n                            <td>\\n                              <b> :</b> <br/>\\n                              \\n                              <b>If:</b> <i>applicability:</i>  <i>(%termencounter.where((status = 'in-progress' and %encounterStartDate + 1 day * %normalReportingDuration &amp;lt; now()) or (status = 'finished' and %encounterEndDate + 72 hours &amp;lt; now())).select(true))</i><br/>\\n                              <b>Then:</b>\\n                              \\n                                \\n                                \\n                              \\n                            </td>\\n                          </tr>\\n                        \\n                          <tr>\\n                            <td>\\n                              <b> :</b> <br/>\\n                              \\n                              <b>If:</b> <i>applicability:</i>  <i>(%completedEncounter.exists(status = 'finished'))</i><br/>\\n                              <b>Then:</b>\\n                              \\n                                \\n                                \\n                              \\n                            </td>\\n                          </tr>\\n                        \\n                      </table>\\n                    \\n                  </td>\\n                </tr>\\n              \\n                <tr>\\n                  <td>\\n                    Create eICR<br/>\\n                    \\n                    \\n                    <b>Then:</b>\\n                    \\n                      \\n                      \\n                    \\n                  </td>\\n                </tr>\\n              \\n                <tr>\\n                  <td>\\n                    Validate eICR<br/>\\n                    \\n                    \\n                    <b>Then:</b>\\n                    \\n                      \\n                      \\n                    \\n                  </td>\\n                </tr>\\n              \\n                <tr>\\n                  <td>\\n                    Route and send eICR<br/>\\n                    \\n                    \\n                    <b>Then:</b>\\n                    \\n                      \\n                      \\n                    \\n                  </td>\\n                </tr>\\n              \\n                <tr>\\n                  <td>\\n                    Start the reporting workflow in response to an encounter-modified event<br/>\\n                    <b>When:</b> <i>named-event:</i> encounter-modified<br/>\\n                    <b>If:</b> <i>applicability:</i>  <i>(%encounter.where(period.start + 1 day * %normalReportingDuration &amp;lt; now()).select(true))</i><br/>\\n                    <b>Then:</b>\\n                    \\n                      \\n                      \\n                    \\n                  </td>\\n                </tr>\\n              \\n            </table>\\n          </td>\\n        </tr>\\n        \\n    </table>\\n</div>\"\n" +
            "  },\n" +
            "  \"library\": [ \"http://ecqi.healthit.gov/ecqms/Library/FHIRHelpers\" ],\n" +
            "  \"extension\": [\n" +
            "    {\n" +
            "      \"url\": \"http://hl7.org/fhir/StructureDefinition/variable\",\n" +
            "      \"valueExpression\": {\n" +
            "        \"name\": \"normalReportingDuration\",\n" +
            "        \"language\": \"text/fhirpath\",\n" +
            "        \"expression\": \"14\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://hl7.org/fhir/StructureDefinition/variable\",\n" +
            "      \"valueExpression\":\n" +
            "      {\n" +
            "        \"name\": \"encounterStartDate\",\n" +
            "        \"language\": \"text/fhirpath\",\n" +
            "        \"expression\": \"{{context.encounterStartDate}}\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://hl7.org/fhir/StructureDefinition/variable\",\n" +
            "      \"valueExpression\":\n" +
            "      {\n" +
            "        \"name\": \"encounterEndDate\",\n" +
            "        \"language\": \"text/fhirpath\",\n" +
            "        \"expression\": \"{{context.encounterEndDate}}\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"url\": \"http://hl7.org/fhir/StructureDefinition/variable\",\n" +
            "      \"valueExpression\":\n" +
            "      {\n" +
            "        \"name\": \"lastReportSubmissionDate\",\n" +
            "        \"language\": \"text/fhirpath\",\n" +
            "        \"expression\": \"{{context.lastReportSubmissionDate}}\"\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"url\": \"http://ersd.aimsplatform.org/fhir/PlanDefinition/us-ecr-specification\",\n" +
            "  \"version\": \"2.1.0\",\n" +
            "  \"name\": \"US_eCR_Specification\",\n" +
            "  \"title\": \"US eCR Specification\",\n" +
            "  \"type\": {\n" +
            "    \"coding\": [\n" +
            "      {\n" +
            "        \"system\": \"http://terminology.hl7.org/CodeSystem/plan-definition-type\",\n" +
            "        \"code\": \"workflow-definition\",\n" +
            "        \"display\": \"Workflow Definition\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"status\": \"active\",\n" +
            "  \"experimental\": true,\n" +
            "  \"date\": \"2023-03-31T12:32:29.858-05:00\",\n" +
            "  \"publisher\": \"eCR\",\n" +
            "  \"contact\": [\n" +
            "    {\n" +
            "      \"name\": \"HL7 International - Public Health\",\n" +
            "      \"telecom\": [\n" +
            "        {\n" +
            "          \"system\": \"url\",\n" +
            "          \"value\": \"http://www.hl7.org/Special/committees/pher\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"description\": \"An example ersd PlanDefinition\",\n" +
            "  \"jurisdiction\": [\n" +
            "    {\n" +
            "      \"coding\": [\n" +
            "        {\n" +
            "          \"system\": \"urn:iso:std:iso:3166\",\n" +
            "          \"code\": \"US\",\n" +
            "          \"display\": \"United States of America\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"text\": \"United States of America\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"effectivePeriod\": {\n" +
            "    \"start\": \"2020-12-01\"\n" +
            "  },\n" +
            "  \"relatedArtifact\": [\n" +
            "    {\n" +
            "      \"type\": \"depends-on\",\n" +
            "      \"label\": \"RCTC Value Set Library of Trigger Codes\",\n" +
            "      \"resource\": \"http://ersd.aimsplatform.org/fhir/Library/rctc\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"action\": [\n" +
            "    {\n" +
            "      \"id\": \"start-workflow\",\n" +
            "      \"description\": \"This action represents the start of the reporting workflow in response to the encounter-start event.\",\n" +
            "      \"textEquivalent\": \"Start the reporting workflow in response to an encounter-start event\",\n" +
            "      \"code\": [\n" +
            "        {\n" +
            "          \"coding\": [\n" +
            "            {\n" +
            "              \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "              \"code\": \"initiate-reporting-workflow\",\n" +
            "              \"display\": \"Initiate a reporting workflow\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"trigger\": [\n" +
            "        {\n" +
            "          \"id\": \"encounter-start\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-named-eventtype-extension\",\n" +
            "              \"valueCodeableConcept\": {\n" +
            "                \"coding\": [\n" +
            "                  {\n" +
            "                    \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-triggerdefinition-namedevents\",\n" +
            "                    \"code\": \"encounter-start\",\n" +
            "                    \"display\": \"Indicates the start of an encounter\"\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"named-event\",\n" +
            "          \"name\": \"encounter-start\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"input\": [\n" +
            "        {\n" +
            "          \"id\": \"patient\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "              \"valueString\": \"Patient/{{context.patientId}}\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Patient\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"encounter\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "              \"valueString\": \"Encounter/{{context.encounterId}}\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Encounter\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"relatedAction\": [\n" +
            "        {\n" +
            "          \"actionId\": \"check-for-immediate-reporting\",\n" +
            "          \"relationship\": \"before-start\",\n" +
            "          \"offsetDuration\": {\n" +
            "            \"value\": 1,\n" +
            "            \"system\": \"http://unitsofmeasure.org\",\n" +
            "            \"code\": \"h\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"check-for-immediate-reporting\",\n" +
            "      \"description\": \"This action represents the start of the check suspected disorder reporting workflow in response to the encounter-start event.\",\n" +
            "      \"textEquivalent\": \"Check suspected disorders for immediate reportability and setup jobs for future reportability checks.\",\n" +
            "      \"code\": [\n" +
            "        {\n" +
            "          \"coding\": [\n" +
            "            {\n" +
            "              \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "              \"code\": \"execute-reporting-workflow\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"action\": [\n" +
            "        {\n" +
            "          \"id\": \"is-encounter-immediately-reportable\",\n" +
            "          \"description\": \"This action represents the check for suspected disorder reportability to create the patients eICR.\",\n" +
            "          \"textEquivalent\": \"Check Trigger Codes based on Suspected Reportable and Lab Order Test Names Value set.\",\n" +
            "          \"code\": [\n" +
            "            {\n" +
            "              \"coding\": [\n" +
            "                {\n" +
            "                  \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "                  \"code\": \"check-trigger-codes\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"condition\": [\n" +
            "            {\n" +
            "              \"kind\": \"applicability\",\n" +
            "              \"expression\": {\n" +
            "                \"extension\": [\n" +
            "                  {\n" +
            "                    \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternative-expression-extension\",\n" +
            "                    \"valueExpression\": {\n" +
            "                      \"language\": \"text/cql-identifier\",\n" +
            "                      \"expression\": \"Is Suspected Disorder?\",\n" +
            "                      \"reference\": \"http://ersd.aimsplatform.org/fhir/Library/RuleFilters|2.1.0\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ],\n" +
            "                \"language\": \"text/fhirpath\",\n" +
            "                \"expression\": \"%suspectedDisorderConditions.exists() or %suspectedDisorderLabOrders.exists()\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"input\": [\n" +
            "            {\n" +
            "              \"id\": \"suspectedDisorderConditions\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"Condition?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Condition\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"code\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/sdtc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"suspectedDisorderLabOrders\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"ServiceRequest?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"ServiceRequest\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"code\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/lotc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"diagnosticOrders\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"DiagnosticReport?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"DiagnosticReport\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"code\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/lotc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"relatedAction\": [\n" +
            "            {\n" +
            "              \"actionId\": \"create-eicr\",\n" +
            "              \"relationship\": \"before-start\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"continue-check-reportable\",\n" +
            "          \"code\": [\n" +
            "            {\n" +
            "              \"coding\": [\n" +
            "                {\n" +
            "                  \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "                  \"code\": \"evaluate-condition\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"condition\": [\n" +
            "            {\n" +
            "              \"kind\": \"applicability\",\n" +
            "              \"expression\": {\n" +
            "                \"extension\": [\n" +
            "                  {\n" +
            "                    \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternative-expression-extension\",\n" +
            "                    \"valueExpression\": {\n" +
            "                      \"language\": \"text/cql-identifier\",\n" +
            "                      \"expression\": \"Is Encounter In Progress and Within Normal Reporting Duration or 72h or less after end of encounter?\",\n" +
            "                      \"reference\": \"http://ersd.aimsplatform.org/fhir/Library/RuleFilters|2.1.0\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ],\n" +
            "                \"language\": \"text/fhirpath\",\n" +
            "                \"expression\": \"%inprogressencounter.where((status = 'in-progress' and %encounterStartDate + 1 day * %normalReportingDuration >= now()) or (status = 'finished' and %encounterEndDate + 72 hours >= now())).select(true)\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"input\":\n" +
            "          [\n" +
            "            {\n" +
            "              \"id\": \"inprogressencounter\",\n" +
            "              \"extension\":\n" +
            "              [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"encounter\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Encounter\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"relatedAction\": [\n" +
            "            {\n" +
            "              \"actionId\": \"check-reportable\",\n" +
            "              \"relationship\": \"before-start\",\n" +
            "              \"offsetDuration\": {\n" +
            "                \"value\": 6,\n" +
            "                \"comparator\": \"<=\",\n" +
            "                \"system\": \"http://unitsofmeasure.org\",\n" +
            "                \"code\": \"h\"\n" +
            "              }\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"terminate-late-encounter\",\n" +
            "          \"code\": [\n" +
            "            {\n" +
            "              \"coding\": [\n" +
            "                {\n" +
            "                  \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "                  \"code\": \"terminate-reporting-workflow\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"condition\": [\n" +
            "            {\n" +
            "              \"kind\": \"applicability\",\n" +
            "              \"expression\": {\n" +
            "                \"extension\": [\n" +
            "                  {\n" +
            "                    \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternative-expression-extension\",\n" +
            "                    \"valueExpression\": {\n" +
            "                      \"language\": \"text/cql-identifier\",\n" +
            "                      \"expression\": \"Is Encounter Late\",\n" +
            "                      \"reference\": \"http://ersd.aimsplatform.org/fhir/Library/RuleFilters|2.1.0\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ],\n" +
            "                \"language\": \"text/fhirpath\",\n" +
            "                \"expression\": \"%terminatedencounter.where((status = 'in-progress' and %encounterStartDate + 1 day * %normalReportingDuration < now()) or (status = 'finished' and %encounterEndDate + 72 hours < now())).select(true)\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"input\": [\n" +
            "            {\n" +
            "              \"id\": \"terminatedencounter\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"encounter\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Encounter\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"is-late-encounter-completed\",\n" +
            "          \"code\": [\n" +
            "            {\n" +
            "              \"coding\": [\n" +
            "                {\n" +
            "                  \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "                  \"code\": \"complete-reporting\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"condition\": [\n" +
            "            {\n" +
            "              \"kind\": \"applicability\",\n" +
            "              \"expression\": {\n" +
            "                \"extension\": [\n" +
            "                  {\n" +
            "                    \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternative-expression-extension\",\n" +
            "                    \"valueExpression\": {\n" +
            "                      \"language\": \"text/cql-identifier\",\n" +
            "                      \"expression\": \"Is Encounter Complete\",\n" +
            "                      \"reference\": \"http://ersd.aimsplatform.org/fhir/Library/RuleFilters|2.1.0\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ],\n" +
            "                \"language\": \"text/fhirpath\",\n" +
            "                \"expression\": \"%lateCompletedEncounter.exists(status = 'finished')\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"input\": [\n" +
            "            {\n" +
            "              \"id\": \"lateCompletedEncounter\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"encounter\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Encounter\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"check-reportable\",\n" +
            "      \"description\": \"This action represents the check for suspected reportability of the eICR.\",\n" +
            "      \"textEquivalent\": \"Check Reportability and setup jobs for future reportability checks.\",\n" +
            "      \"code\": [\n" +
            "        {\n" +
            "          \"coding\": [\n" +
            "            {\n" +
            "              \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "              \"code\": \"execute-reporting-workflow\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"action\": [\n" +
            "        {\n" +
            "          \"id\": \"is-encounter-reportable\",\n" +
            "          \"description\": \"This action represents the check for reportability to create the patients eICR.\",\n" +
            "          \"textEquivalent\": \"Check Trigger Codes based on RCTC Value sets.\",\n" +
            "          \"code\": [\n" +
            "            {\n" +
            "              \"coding\": [\n" +
            "                {\n" +
            "                  \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "                  \"code\": \"check-trigger-codes\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"condition\": [\n" +
            "            {\n" +
            "              \"kind\": \"applicability\",\n" +
            "              \"expression\": {\n" +
            "                \"extension\": [\n" +
            "                  {\n" +
            "                    \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternative-expression-extension\",\n" +
            "                    \"valueExpression\": {\n" +
            "                      \"language\": \"text/cql-identifier\",\n" +
            "                      \"expression\": \"Is Encounter Reportable and Within Normal Reporting Duration?\",\n" +
            "                      \"reference\": \"http://ersd.aimsplatform.org/fhir/Library/RuleFilters|2.1.0\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ],\n" +
            "                \"language\": \"text/fhirpath\",\n" +
            "                \"expression\": \"(%encounterStartDate + 1 day * %normalReportingDuration >= now()) and (%conditions.exists() or %diagnosticResultValues.exists() or %encounters.exists() or %immunizations.exists() or %labOrders.exists() or %labTests.exists() or %labResults.exists() or %labResultValues.exists() or %medicationAdministrations.exists() or %medicationOrders.exists() or %medicationDispenses.exists())\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"input\": [\n" +
            "            {\n" +
            "              \"id\": \"conditions\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"Condition?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Condition\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"code\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/dxtc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"encounters\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"encounter\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Encounter\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"reasonCode\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/dxtc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"immunizations\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"Immunization?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Immunization\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"vaccineCode\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/mrtc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"labOrders\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"ServiceRequest?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"ServiceRequest\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"code\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/lotc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"labTests\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"Observation?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Observation\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"code\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/lotc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"diagnosticOrders\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"DiagnosticReport?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"DiagnosticReport\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"code\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/lotc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"medicationOrders\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"MedicationRequest?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"MedicationRequest\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"medication\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/mrtc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"medicationDispenses\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"MedicationDispense?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"MedicationDispense\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"medication\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/mrtc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"medicationAdministrations\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "                  \"valueString\": \"MedicationAdministration?patient=Patient/{{context.patientId}}\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"MedicationAdministration\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"medication\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/mrtc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"labResults\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"labTests\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Observation\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"value\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/ostc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"labResultValues\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"labTests\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Observation\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"value\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/lrtc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"diagnosticResults\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"diagnosticOrders\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"DiagnosticReport\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"code\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/ostc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            {\n" +
            "              \"id\": \"diagnosticResultValues\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"diagnosticOrders\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"DiagnosticReport\",\n" +
            "              \"codeFilter\": [\n" +
            "                {\n" +
            "                  \"path\": \"code\",\n" +
            "                  \"valueSet\": \"http://ersd.aimsplatform.org/fhir/ValueSet/lrtc\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"relatedAction\": [\n" +
            "            {\n" +
            "              \"actionId\": \"create-eicr\",\n" +
            "              \"relationship\": \"before-start\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"check-update-eicr\",\n" +
            "          \"code\": [\n" +
            "            {\n" +
            "              \"coding\": [\n" +
            "                {\n" +
            "                  \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "                  \"code\": \"evaluate-condition\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"condition\": [\n" +
            "            {\n" +
            "              \"kind\": \"applicability\",\n" +
            "              \"expression\": {\n" +
            "                \"extension\": [\n" +
            "                  {\n" +
            "                    \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternative-expression-extension\",\n" +
            "                    \"valueExpression\": {\n" +
            "                      \"language\": \"text/cql-identifier\",\n" +
            "                      \"expression\": \"Most recent eICR sent over 72 hours ago?\",\n" +
            "                      \"reference\": \"http://ersd.aimsplatform.org/fhir/Library/RuleFilters|2.1.0\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ],\n" +
            "                \"language\": \"text/fhirpath\",\n" +
            "                \"expression\": \"%lastReportSubmissionDate < now() - 72 hours\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"relatedAction\": [\n" +
            "            {\n" +
            "              \"actionId\": \"create-eicr\",\n" +
            "              \"relationship\": \"before-start\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"is-encounter-in-progress\",\n" +
            "          \"code\": [\n" +
            "            {\n" +
            "              \"coding\": [\n" +
            "                {\n" +
            "                  \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "                  \"code\": \"evaluate-condition\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"condition\": [\n" +
            "            {\n" +
            "              \"kind\": \"applicability\",\n" +
            "              \"expression\": {\n" +
            "                \"extension\": [\n" +
            "                  {\n" +
            "                    \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternative-expression-extension\",\n" +
            "                    \"valueExpression\": {\n" +
            "                      \"language\": \"text/cql-identifier\",\n" +
            "                      \"expression\": \"Is Encounter In Progress and Within Normal Reporting Duration or 72h or less after end of encounter?\",\n" +
            "                      \"reference\": \"http://ersd.aimsplatform.org/fhir/Library/RuleFilters|2.1.0\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ],\n" +
            "                \"language\": \"text/fhirpath\",\n" +
            "                \"expression\": \"%inprogressencounter.where(status = 'in-progress' and %encounterStartDate + 1 day * %normalReportingDuration >= now() or (status = 'finished' and %encounterEndDate + 72 hours >= now())).exists()\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"input\": [\n" +
            "            {\n" +
            "              \"id\": \"inprogressencounter\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"encounter\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Encounter\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"relatedAction\": [\n" +
            "            {\n" +
            "              \"actionId\": \"check-reportable\",\n" +
            "              \"relationship\": \"before-start\",\n" +
            "              \"offsetDuration\": {\n" +
            "                \"value\": 6,\n" +
            "                \"comparator\": \"<=\",\n" +
            "                \"system\": \"http://unitsofmeasure.org\",\n" +
            "                \"code\": \"h\"\n" +
            "              }\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"terminate-encounter\",\n" +
            "          \"code\": [\n" +
            "            {\n" +
            "              \"coding\": [\n" +
            "                {\n" +
            "                  \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "                  \"code\": \"terminate-reporting-workflow\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"condition\": [\n" +
            "            {\n" +
            "              \"kind\": \"applicability\",\n" +
            "              \"expression\": {\n" +
            "                \"extension\": [\n" +
            "                  {\n" +
            "                    \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternative-expression-extension\",\n" +
            "                    \"valueExpression\": {\n" +
            "                      \"language\": \"text/cql-identifier\",\n" +
            "                      \"expression\": \"Is Encounter Late\",\n" +
            "                      \"reference\": \"http://ersd.aimsplatform.org/fhir/Library/RuleFilters|2.1.0\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ],\n" +
            "                \"language\": \"text/fhirpath\",\n" +
            "                \"expression\": \"%termencounter.where((status = 'in-progress' and %encounterStartDate + 1 day * %normalReportingDuration < now()) or (status = 'finished' and %encounterEndDate + 72 hours < now())).select(true)\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"input\": [\n" +
            "            {\n" +
            "              \"id\": \"termencounter\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"encounter\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Encounter\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"is-encounter-completed\",\n" +
            "          \"code\": [\n" +
            "            {\n" +
            "              \"coding\": [\n" +
            "                {\n" +
            "                  \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "                  \"code\": \"complete-reporting\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ],\n" +
            "          \"condition\": [\n" +
            "            {\n" +
            "              \"kind\": \"applicability\",\n" +
            "              \"expression\": {\n" +
            "                \"extension\": [\n" +
            "                  {\n" +
            "                    \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternative-expression-extension\",\n" +
            "                    \"valueExpression\": {\n" +
            "                      \"language\": \"text/cql-identifier\",\n" +
            "                      \"expression\": \"Is Encounter Complete\",\n" +
            "                      \"reference\": \"http://ersd.aimsplatform.org/fhir/Library/RuleFilters|2.1.0\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ],\n" +
            "                \"language\": \"text/fhirpath\",\n" +
            "                \"expression\": \"%completedEncounter.exists(status = 'finished')\"\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"input\": [\n" +
            "            {\n" +
            "              \"id\": \"completedEncounter\",\n" +
            "              \"extension\": [\n" +
            "                {\n" +
            "                  \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "                  \"valueString\": \"encounter\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"type\": \"Encounter\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"create-eicr\",\n" +
            "      \"description\": \"This action represents the creation of the eICR. It subsequently calls validate.\",\n" +
            "      \"textEquivalent\": \"Create eICR\",\n" +
            "      \"code\": [\n" +
            "        {\n" +
            "          \"coding\": [\n" +
            "            {\n" +
            "              \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "              \"code\": \"create-report\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"input\": [\n" +
            "        {\n" +
            "          \"id\": \"patientdata\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"patient\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Patient\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"conditiondata\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"conditions\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Condition\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"encounterdata\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"encounter\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Encounter\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-encounter\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"mrdata\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"medicationOrders\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"MedicationRequest\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-medicationrequest\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"immzdata\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"immunizations\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Immunization\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-immunization\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"labResultdata\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"labResults\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Observation\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"labOrderdata\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"labOrders\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"ServiceRequest\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/StructureDefinition/ServiceRequest\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"diagnosticResultdata\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"diagnosticResults\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"DiagnosticReport\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-diagnosticreport-lab\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"diagnosticOrderdata\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"diagnosticOrders\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"DiagnosticReport\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-diagnosticreport-lab\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"odhData-loinc\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "              \"valueString\": \"Observation?patient=Patient/{{context.patientId}}&code=http://loinc.org|11295-3\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Observation\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-social-history\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"odhData-snomed\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "              \"valueString\": \"Observation?patient=Patient/{{context.patientId}}&code=http://snomed.info/sct|224362002,364703007\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Observation\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-social-history\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"pregnancyObservations\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "              \"valueString\": \"Observation?patient=Patient/{{context.patientId}}&code=http://loinc.org|90767-5\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Observation\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-social-history\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"pregnancyConditions\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "              \"valueString\": \"Condition?patient=Patient/{{context.patientId}}&code=http://snomed.info/sct|77386006\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Condition\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"travelData-loinc\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "              \"valueString\": \"Observation?patient=Patient/{{context.patientId}}&code=http://loinc.org|29762-2\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Observation\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-social-history\"\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"travelData-snomed\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "              \"valueString\": \"Observation?patient=Patient/{{context.patientId}}&code=http://snomed.info/sct|161085007,443846001,420008001,46521000175102,34831000175105,161086008\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Observation\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-social-history\"\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"output\": [\n" +
            "        {\n" +
            "          \"id\": \"eicrreport\",\n" +
            "          \"type\": \"Bundle\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/ecr/StructureDefinition/eicr-document-bundle\"\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"relatedAction\": [\n" +
            "        {\n" +
            "          \"actionId\": \"validate-eicr\",\n" +
            "          \"relationship\": \"before-start\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"validate-eicr\",\n" +
            "      \"description\": \"This action represents the validation of the eICR. It subsequently calls route-and-send.\",\n" +
            "      \"textEquivalent\": \"Validate eICR\",\n" +
            "      \"code\": [\n" +
            "        {\n" +
            "          \"coding\": [\n" +
            "            {\n" +
            "              \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "              \"code\": \"validate-report\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"input\": [\n" +
            "        {\n" +
            "          \"id\": \"generatedeicrreport\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"eicrreport\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Bundle\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/ecr/StructureDefinition/eicr-document-bundle\"\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"output\": [\n" +
            "        {\n" +
            "          \"id\": \"valideicrreport\",\n" +
            "          \"type\": \"Bundle\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/ecr/StructureDefinition/eicr-document-bundle\"\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"relatedAction\": [\n" +
            "        {\n" +
            "          \"actionId\": \"route-and-send-eicr\",\n" +
            "          \"relationship\": \"before-start\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"route-and-send-eicr\",\n" +
            "      \"description\": \"This action represents the routing and sending of the eICR.\",\n" +
            "      \"textEquivalent\": \"Route and send eICR\",\n" +
            "      \"code\": [\n" +
            "        {\n" +
            "          \"coding\": [\n" +
            "            {\n" +
            "              \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "              \"code\": \"submit-report\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"input\": [\n" +
            "        {\n" +
            "          \"id\": \"validatedeicrreport\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-relateddata-extension\",\n" +
            "              \"valueString\": \"valideicrreport\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Bundle\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/ecr/StructureDefinition/eicr-document-bundle\"\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"output\": [\n" +
            "        {\n" +
            "          \"id\": \"submittedeicrreport\",\n" +
            "          \"type\": \"Bundle\",\n" +
            "          \"profile\": [\n" +
            "            \"http://hl7.org/fhir/us/ecr/StructureDefinition/eicr-document-bundle\"\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"encounter-modified\",\n" +
            "      \"description\": \"This action represents the start of the reporting workflow in response to the encounter-modified event\",\n" +
            "      \"textEquivalent\": \"Start the reporting workflow in response to an encounter-modified event\",\n" +
            "      \"code\": [\n" +
            "        {\n" +
            "          \"coding\": [\n" +
            "            {\n" +
            "              \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-plandefinition-actions\",\n" +
            "              \"code\": \"initiate-reporting-workflow\",\n" +
            "              \"display\": \"Initiate a reporting workflow\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"trigger\": [\n" +
            "        {\n" +
            "          \"id\": \"encounter-modified-trigger\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-named-eventtype-extension\",\n" +
            "              \"valueCodeableConcept\": {\n" +
            "                \"coding\": [\n" +
            "                  {\n" +
            "                    \"system\": \"http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-triggerdefinition-namedevents\",\n" +
            "                    \"code\": \"encounter-modified\",\n" +
            "                    \"display\": \"Indicates modifications to data elements of an encounter\"\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"named-event\",\n" +
            "          \"name\": \"encounter-modified\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"condition\": [\n" +
            "        {\n" +
            "          \"kind\": \"applicability\",\n" +
            "          \"expression\": {\n" +
            "            \"extension\": [\n" +
            "              {\n" +
            "                \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternative-expression-extension\",\n" +
            "                \"valueExpression\": {\n" +
            "                  \"language\": \"text/cql-identifier\",\n" +
            "                  \"expression\": \"Is Encounter Longer Than Normal Reporting Duration?\",\n" +
            "                  \"reference\": \"http://ersd.aimsplatform.org/fhir/Library/RuleFilters|2.1.0\"\n" +
            "                }\n" +
            "              }\n" +
            "            ],\n" +
            "            \"language\": \"text/fhirpath\",\n" +
            "            \"expression\": \"%encounter.where(period.start + 1 day * %normalReportingDuration < now()).select(true)\"\n" +
            "          }\n" +
            "        }\n" +
            "      ],\n" +
            "      \"input\": [\n" +
            "        {\n" +
            "          \"id\": \"modifiedPatient\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "              \"valueString\": \"Patient/{{context.patientId}}\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Patient\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": \"modifiedEncounter\",\n" +
            "          \"extension\": [\n" +
            "            {\n" +
            "              \"url\": \"http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-fhirquerypattern-extension\",\n" +
            "              \"valueString\": \"Encounter/{{context.encounterId}}\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"type\": \"Encounter\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"relatedAction\": [\n" +
            "        {\n" +
            "          \"actionId\": \"create-eicr\",\n" +
            "          \"relationship\": \"before-start\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
