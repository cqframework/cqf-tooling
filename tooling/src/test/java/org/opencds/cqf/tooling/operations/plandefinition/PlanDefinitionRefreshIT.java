package org.opencds.cqf.tooling.operations.plandefinition;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.PlanDefinition;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class PlanDefinitionRefreshIT {

    FhirContext fhirContext = FhirContext.forR5Cached();

    @Test
    void testSingleMeasureWithoutUpdate() {
        PlanDefinition planDefinitionToRefresh = (PlanDefinition) fhirContext.newJsonParser().parseResource(ZIKAVIRUSINTVPLANDEFINITION);
        PlanDefinitionRefresh planDefinitionRefresh = new PlanDefinitionRefresh(fhirContext,
                "src/test/resources/org/opencds/cqf/tooling/testfiles/refreshIG/input/cql/");

        IBaseResource result = planDefinitionRefresh.refreshPlanDefinition(planDefinitionToRefresh);

        Assert.assertTrue(result instanceof PlanDefinition);
        PlanDefinition refreshedPlanDefinition = (PlanDefinition) result;

        // test date update
        Assert.assertTrue(DateUtils.isSameDay(new Date(), refreshedPlanDefinition.getDate()));

        // Contained Resource tests
        Assert.assertTrue(refreshedPlanDefinition.hasContained());

        // Extension tests before update (should be the same)
        Assert.assertEquals(refreshedPlanDefinition.getExtension().size(),
                planDefinitionToRefresh.getExtension().size());

        // Library tests before update (should be the same)
        Assert.assertEquals(refreshedPlanDefinition.getLibrary().get(0).getId(),
                planDefinitionToRefresh.getLibrary().get(0).getId());
    }

    @Test
    void testSingleMeasureWithUpdate() {
        PlanDefinition planDefinitionToRefresh = (PlanDefinition) fhirContext.newJsonParser().parseResource(ZIKAVIRUSINTVPLANDEFINITION);
        PlanDefinitionRefresh planDefinitionRefresh = new PlanDefinitionRefresh(fhirContext,
                "src/test/resources/org/opencds/cqf/tooling/testfiles/refreshIG/input/cql/");

        PlanDefinition beforeUpdate = planDefinitionToRefresh.copy();
        planDefinitionRefresh.refreshPlanDefinition(planDefinitionToRefresh);

        // test date update
        Assert.assertTrue(DateUtils.isSameDay(new Date(), planDefinitionToRefresh.getDate()));

        // Contained Resource tests before update
        Assert.assertTrue(planDefinitionToRefresh.hasContained());

        // DataRequirement tests before update (should not be the same)
        Assert.assertNotEquals(beforeUpdate.getExtension().size(),
                planDefinitionToRefresh.getExtension().size());

        // Library tests before update (should not be the same)
        Assert.assertEquals(beforeUpdate.getLibrary().get(0).getId(), planDefinitionToRefresh.getLibrary().get(0).getId());
    }

    private final String ZIKAVIRUSINTVPLANDEFINITION = "{\n" +
            "  \"resourceType\" : \"PlanDefinition\",\n" +
            "  \"id\" : \"zika-virus-intervention\",\n" +
            "  \"text\" : {\n" +
            "    \"status\" : \"generated\",\n" +
            "    \"div\" : \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">\\n         \\n      <table class=\\\"grid dict\\\">\\n            \\n        <tr>\\n               \\n          <td>\\n                  \\n            <b>Id: </b>\\n               \\n          </td>\\n            \\n        </tr>\\n            \\n        <tr>\\n               \\n          <td style=\\\"padding-left: 25px; padding-right: 25px;\\\">PlanDefinition/zika-virus-intervention</td>\\n            \\n        </tr>\\n         \\n      </table>\\n         \\n      <p/>\\n         \\n      <table class=\\\"grid dict\\\">\\n            \\n        <tr>\\n               \\n          <td>\\n                  \\n            <b>Identifier: </b>\\n               \\n          </td>\\n            \\n        </tr>\\n            \\n        <tr>\\n               \\n          <td style=\\\"padding-left: 25px; padding-right: 25px;\\\">\\n                  \\n            <b>value: </b>\\n                  \\n            <span>zika-virus-intervention</span>\\n               \\n          </td>\\n            \\n        </tr>\\n         \\n      </table>\\n         \\n      <p/>\\n         \\n      <table class=\\\"grid dict\\\">\\n            \\n        <tr>\\n               \\n          <td>\\n                  \\n            <b>Title: </b>\\n               \\n          </td>\\n            \\n        </tr>\\n            \\n        <tr>\\n               \\n          <td style=\\\"padding-left: 25px; padding-right: 25px;\\\">Example Zika Virus Intervention</td>\\n            \\n        </tr>\\n         \\n      </table>\\n         \\n      <p/>\\n         \\n      <table class=\\\"grid dict\\\">\\n            \\n        <tr>\\n               \\n          <td>\\n                  \\n            <b>Status: </b>\\n               \\n          </td>\\n            \\n        </tr>\\n            \\n        <tr>\\n               \\n          <td style=\\\"padding-left: 25px; padding-right: 25px;\\\">active</td>\\n            \\n        </tr>\\n         \\n      </table>\\n         \\n      <p/>\\n         \\n      <table class=\\\"grid dict\\\">\\n            \\n        <tr>\\n               \\n          <td>\\n                  \\n            <b>Description: </b>\\n               \\n          </td>\\n            \\n        </tr>\\n            \\n        <tr>\\n               \\n          <td style=\\\"padding-left: 25px; padding-right: 25px;\\\">Zika Virus Management intervention describing the CDC Guidelines for Zika Virus Reporting and Management.</td>\\n            \\n        </tr>\\n         \\n      </table>\\n         \\n      <p/>\\n         \\n      <table class=\\\"grid dict\\\">\\n            \\n        <tr>\\n               \\n          <td>\\n                  \\n            <b>Topic: </b>\\n               \\n          </td>\\n            \\n        </tr>\\n            \\n        <tr>\\n               \\n          <td style=\\\"padding-left: 25px; padding-right: 25px;\\\">\\n                  \\n            <span>\\n                     \\n              <b>text: </b>\\n                     \\n              <span>Zika Virus Management</span>\\n                  \\n            </span>\\n               \\n          </td>\\n            \\n        </tr>\\n         \\n      </table>\\n         \\n      <p/>\\n         \\n      <table class=\\\"grid dict\\\">\\n            \\n        <tr>\\n               \\n          <td>\\n                  \\n            <b>Library: </b>\\n               \\n          </td>\\n            \\n        </tr>\\n            \\n        <tr>\\n               \\n          <td style=\\\"padding-left: 25px; padding-right: 25px;\\\">\\n                  \\n            <b>reference: </b>\\n                  \\n            <span>Library/zika-virus-intervention-logic</span>\\n               \\n          </td>\\n            \\n        </tr>\\n         \\n      </table>\\n         \\n      <p/>\\n         \\n      <h2>Actions</h2>\\n         \\n      <p style=\\\"width: 100%;\\\" class=\\\"hierarchy\\\">\\n            \\n        <span>\\n               \\n          <b>Step: </b>\\n               \\n          <br/>\\n               \\n          <span style=\\\"padding-left: 25px;\\\">\\n                  \\n            <b>title: </b>\\n                  \\n            <span>Zika Virus Assessment</span>\\n                  \\n            <br/>\\n               \\n          </span>\\n               \\n          <span style=\\\"padding-left: 25px;\\\">\\n                  \\n            <b>condition: </b>\\n                  \\n            <br/>\\n                  \\n            <span style=\\\"padding-left: 50px;\\\">\\n                     \\n              <b>type: </b>\\n                     \\n              <span>applicability</span>\\n                     \\n              <br/>\\n                  \\n            </span>\\n                  \\n            <span style=\\\"padding-left: 50px;\\\">\\n                     \\n              <b>expression: </b>\\n                     \\n              <span>Is Patient Pregnant</span>\\n                     \\n              <br/>\\n                  \\n            </span>\\n               \\n          </span>\\n               \\n          <span style=\\\"padding-left: 25px;\\\">\\n                  \\n            <span>\\n                     \\n              <b>Step: </b>\\n                     \\n              <br/>\\n                     \\n              <span style=\\\"padding-left: 50px;\\\">\\n                        \\n                <b>condition: </b>\\n                        \\n                <br/>\\n                        \\n                <span style=\\\"padding-left: 75px;\\\">\\n                           \\n                  <b>type: </b>\\n                           \\n                  <span>applicability</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                        \\n                <span style=\\\"padding-left: 75px;\\\">\\n                           \\n                  <b>expression: </b>\\n                           \\n                  <span>Should Administer Zika Virus Exposure Assessment</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                     \\n              </span>\\n                     \\n              <span style=\\\"padding-left: 50px;\\\">\\n                        \\n                <b>condition: </b>\\n                        \\n                <br/>\\n                        \\n                <span style=\\\"padding-left: 75px;\\\">\\n                           \\n                  <b>reference: </b>\\n                           \\n                  <br/>\\n                           \\n                  <span style=\\\"padding-left: 100px;\\\">#administer-zika-virus-exposure-assessment</span>\\n                           \\n                  <br/>\\n                           \\n                  <span>\\n\\t\\t\\t\\t\\t\\t\\t \\n                    <span style=\\\"padding-left: 75px;\\\">\\n\\t\\t\\t\\t\\t\\t\\t\\t\\n                      <b>description: </b>\\n\\t\\t\\t\\t\\t\\t\\t\\t\\n                      <span>Administer Zika Virus Exposure Assessment</span>\\n\\t\\t\\t\\t\\t\\t\\t\\t\\n                      <br/>\\n\\t\\t\\t\\t\\t\\t\\t \\n                    </span>\\n\\t\\t\\t\\t\\t\\t\\t \\n                    <span style=\\\"padding-left: 75px;\\\">\\n\\t\\t\\t\\t\\t\\t\\t\\t\\n                      <b>category: </b>\\n\\t\\t\\t\\t\\t\\t\\t\\t\\n                      <span>procedure</span>\\n\\t\\t\\t\\t\\t\\t\\t\\t\\n                      <br/>\\n\\t\\t\\t\\t\\t\\t\\t \\n                    </span>\\n                           \\n                  </span>\\n                        \\n                </span>\\n                     \\n              </span>\\n                     \\n              <span style=\\\"padding-left: 50px;\\\"/>\\n                  \\n            </span>\\n                  \\n            <span>\\n                     \\n              <b>Step: </b>\\n                     \\n              <br/>\\n                     \\n              <span style=\\\"padding-left: 75px;\\\">\\n                        \\n                <b>condition: </b>\\n                        \\n                <br/>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>type: </b>\\n                           \\n                  <span>applicability</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>expression: </b>\\n                           \\n                  <span>Should Order Serum + Urine rRT-PCR Test</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                     \\n              </span>\\n                     \\n              <span style=\\\"padding-left: 75px;\\\">\\n                        \\n                <b>condition: </b>\\n                        \\n                <br/>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>reference: </b>\\n                           \\n                  <br/>\\n                           \\n                  <span style=\\\"padding-left: 125px;\\\">ActivityDefinition/order-serum-urine-rrt-pcr-test</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                     \\n              </span>\\n                     \\n              <span style=\\\"padding-left: 50px;\\\"/>\\n                  \\n            </span>\\n                  \\n            <span>\\n                     \\n              <b>Step: </b>\\n                     \\n              <br/>\\n                     \\n              <span style=\\\"padding-left: 75px;\\\">\\n                        \\n                <b>condition: </b>\\n                        \\n                <br/>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>type: </b>\\n                           \\n                  <span>applicability</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>expression: </b>\\n                           \\n                  <span>Should Order Serum Zika Virus IgM + Dengue Virus IgM</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                     \\n              </span>\\n                     \\n              <span style=\\\"padding-left: 75px;\\\">\\n                        \\n                <b>condition: </b>\\n                        \\n                <br/>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>reference: </b>\\n                           \\n                  <br/>\\n                           \\n                  <span style=\\\"padding-left: 125px;\\\">#order-serum-zika-dengue-virus-igm</span>\\n                           \\n                  <br/>\\n                           \\n                  <span>\\n                              \\n                    <span style=\\\"padding-left: 50px;\\\">\\n                                 \\n                      <span style=\\\"padding-left: 50px;\\\">\\n                                    \\n                        <b>description: </b>\\n                                    \\n                        <span>Order Serum Zika and Dengue Virus IgM</span>\\n                                    \\n                        <br/>\\n                                 \\n                      </span>\\n                                 \\n                      <span style=\\\"padding-left: 75px;\\\">\\n                                    \\n                        <b>related:</b>\\n                                    \\n                        <br/>\\n                                    \\n                        <span style=\\\"padding-left: 100px;\\\">\\n                                       \\n                          <b>type: </b>\\n                                       \\n                          <span>documentation</span>\\n                                       \\n                          <br/>\\n                                    \\n                        </span>\\n                                    \\n                        <span style=\\\"padding-left: 100px;\\\">\\n                                       \\n                          <b>display: </b>\\n                                       \\n                          <span>Explanation of diagnostic tests for Zika virus and which to use based on the patients clinical and exposure history.</span>\\n                                       \\n                          <br/>\\n                                    \\n                        </span>\\n                                 \\n                      </span>\\n                                 \\n                      <span style=\\\"padding-left: 100px;\\\">\\n                                    \\n                        <b>category: </b>\\n                                    \\n                        <span>diagnostic</span>\\n                                    \\n                        <br/>\\n                                 \\n                      </span>\\n                              \\n                    </span>\\n                           \\n                  </span>\\n                           \\n                  <span/>\\n                        \\n                </span>\\n                     \\n              </span>\\n                     \\n              <span style=\\\"padding-left: 50px;\\\"/>\\n                  \\n            </span>\\n                  \\n            <span>\\n                     \\n              <b>Step: </b>\\n                     \\n              <br/>\\n                     \\n              <span style=\\\"padding-left: 75px;\\\">\\n                        \\n                <b>condition: </b>\\n                        \\n                <br/>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>type: </b>\\n                           \\n                  <span>applicability</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>expression: </b>\\n                           \\n                  <span>Should Consider IgM Antibody Testing</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                     \\n              </span>\\n                     \\n              <span style=\\\"padding-left: 75px;\\\">\\n                        \\n                <b>condition: </b>\\n                        \\n                <br/>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>reference: </b>\\n                           \\n                  <br/>\\n                           \\n                  <span style=\\\"padding-left: 125px;\\\">ActivityDefinition/consider-igm-antibody-testing</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                     \\n              </span>\\n                     \\n              <span style=\\\"padding-left: 50px;\\\"/>\\n                  \\n            </span>\\n                  \\n            <span>\\n                     \\n              <b>Step: </b>\\n                     \\n              <br/>\\n                     \\n              <span style=\\\"padding-left: 75px;\\\">\\n                        \\n                <b>condition: </b>\\n                        \\n                <br/>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>type: </b>\\n                           \\n                  <span>applicability</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                        \\n                <span style=\\\"padding-left: 100px;\\\">\\n                           \\n                  <b>expression: </b>\\n                           \\n                  <span>Should Provide Mosquito Prevention and Contraception Advice</span>\\n                           \\n                  <br/>\\n                        \\n                </span>\\n                     \\n              </span>\\n                     \\n              <span style=\\\"padding-left: 50px;\\\">\\n                        \\n                <span>\\n                           \\n                  <b>Step: </b>\\n                           \\n                  <br/>\\n                           \\n                  <span style=\\\"padding-left: 75px;\\\">\\n                              \\n                    <b>condition: </b>\\n                              \\n                    <br/>\\n                              \\n                    <span style=\\\"padding-left: 100px;\\\">\\n                                 \\n                      <b>reference: </b>\\n                                 \\n                      <br/>\\n                                 \\n                      <span style=\\\"padding-left: 125px;\\\">#provide-mosquito-prevention-advice</span>\\n                                 \\n                      <span>\\n                                    \\n                        <span style=\\\"padding-left: 75px;\\\">\\n                                       \\n                          <br/>\\n                                       \\n                          <span style=\\\"padding-left: 100px;\\\">\\n                                          \\n                            <b>description: </b>\\n                                          \\n                            <span>Provide mosquito prevention advice</span>\\n                                          \\n                            <br/>\\n                                       \\n                          </span>\\n                                       \\n                          <span style=\\\"padding-left: 75px;\\\">\\n                                          \\n                            <b>related:</b>\\n                                          \\n                            <br/>\\n                                          \\n                            <span style=\\\"padding-left: 100px;\\\">\\n                                             \\n                              <b>type: </b>\\n                                             \\n                              <span>documentation</span>\\n                                             \\n                              <br/>\\n                                          \\n                            </span>\\n                                          \\n                            <span style=\\\"padding-left: 100px;\\\">\\n                                             \\n                              <b>display: </b>\\n                                             \\n                              <span>Advice for patients about how to avoid Mosquito bites.</span>\\n                                             \\n                              <br/>\\n                                          \\n                            </span>\\n                                       \\n                          </span>\\n                                       \\n                          <span style=\\\"padding-left: 75px;\\\">\\n                                          \\n                            <b>related:</b>\\n                                          \\n                            <br/>\\n                                          \\n                            <span style=\\\"padding-left: 100px;\\\">\\n                                             \\n                              <b>type: </b>\\n                                             \\n                              <span>documentation</span>\\n                                             \\n                              <br/>\\n                                          \\n                            </span>\\n                                          \\n                            <span style=\\\"padding-left: 100px;\\\">\\n                                             \\n                              <b>display: </b>\\n                                             \\n                              <span>Advice for patients about which mosquito repellents are effective and safe to use in pregnancy. [DEET, IF3535 and Picardin are safe during]</span>\\n                                             \\n                              <br/>\\n                                          \\n                            </span>\\n                                       \\n                          </span>\\n                                       \\n                          <span style=\\\"padding-left: 100px;\\\">\\n                                          \\n                            <b>category: </b>\\n                                          \\n                            <span>communication</span>\\n                                          \\n                            <br/>\\n                                       \\n                          </span>\\n                                    \\n                        </span>\\n                                 \\n                      </span>\\n                              \\n                    </span>\\n                           \\n                  </span>\\n                           \\n                  <span style=\\\"padding-left: 75px;\\\"/>\\n                        \\n                </span>\\n                        \\n                <span>\\n                           \\n                  <b>Step: </b>\\n                           \\n                  <br/>\\n                           \\n                  <span style=\\\"padding-left: 75px;\\\">\\n                              \\n                    <b>condition: </b>\\n                              \\n                    <br/>\\n                              \\n                    <span style=\\\"padding-left: 100px;\\\">\\n                                 \\n                      <b>reference: </b>\\n                                 \\n                      <br/>\\n                                 \\n                      <span style=\\\"padding-left: 125px;\\\">ActivityDefinition/provide-contraception-advice</span>\\n                                 \\n                      <br/>\\n                              \\n                    </span>\\n                           \\n                  </span>\\n                        \\n                </span>\\n                     \\n              </span>\\n                  \\n            </span>\\n               \\n          </span>\\n            \\n        </span>\\n         \\n      </p>\\n      \\n    </div>\"\n" +
            "  },\n" +
            "  \"url\" : \"http://example.org/PlanDefinition/zika-virus-intervention\",\n" +
            "  \"identifier\" : [{\n" +
            "    \"system\" : \"urn:ietf:rfc:3986\",\n" +
            "    \"value\" : \"urn:oid:2.16.840.1.113883.4.642.11.5\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"use\" : \"official\",\n" +
            "    \"value\" : \"zika-virus-intervention\"\n" +
            "  }],\n" +
            "  \"version\" : \"2.0.0\",\n" +
            "  \"name\" : \"ExampleZikaVirusIntervention\",\n" +
            "  \"title\" : \"Example Zika Virus Intervention\",\n" +
            "  \"status\" : \"active\",\n" +
            "  \"date\" : \"2017-01-12\",\n" +
            "  \"description\" : \"Zika Virus Management intervention describing the CDC Guidelines for Zika Virus Reporting and Management.\",\n" +
            "  \"useContext\" : [{\n" +
            "    \"code\" : {\n" +
            "      \"system\" : \"http://terminology.hl7.org/CodeSystem/usage-context-type\",\n" +
            "      \"code\" : \"age\"\n" +
            "    },\n" +
            "    \"valueRange\" : {\n" +
            "      \"low\" : {\n" +
            "        \"value\" : 12,\n" +
            "        \"unit\" : \"a\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"code\" : {\n" +
            "      \"system\" : \"http://terminology.hl7.org/CodeSystem/usage-context-type\",\n" +
            "      \"code\" : \"user\"\n" +
            "    },\n" +
            "    \"valueCodeableConcept\" : {\n" +
            "      \"coding\" : [{\n" +
            "        \"system\" : \"http://snomed.info/sct\",\n" +
            "        \"code\" : \"309343006\",\n" +
            "        \"display\" : \"Physician\"\n" +
            "      }]\n" +
            "    }\n" +
            "  }],\n" +
            "  \"topic\" : [{\n" +
            "    \"text\" : \"Zika Virus Management\"\n" +
            "  }],\n" +
            "  \"relatedArtifact\" : [{\n" +
            "    \"type\" : \"derived-from\",\n" +
            "    \"document\" : {\n" +
            "      \"url\" : \"https://www.cdc.gov/mmwr/volumes/65/wr/mm6539e1.htm?s_cid=mm6539e1_w\"\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"type\" : \"predecessor\",\n" +
            "    \"resource\" : \"http://example.org/fhir/PlanDefinition/zika-virus-intervention-initial\"\n" +
            "  }],\n" +
            "  \"library\" : [\"http://example.org/fhir/Library/zika-virus-intervention-logic\"],\n" +
            "  \"action\" : [{\n" +
            "    \"title\" : \"Zika Virus Assessment\",\n" +
            "    \"trigger\" : [{\n" +
            "      \"type\" : \"named-event\",\n" +
            "      \"name\" : \"patient-view\"\n" +
            "    }],\n" +
            "    \"condition\" : [{\n" +
            "      \"kind\" : \"applicability\",\n" +
            "      \"expression\" : {\n" +
            "        \"language\" : \"text/cql\",\n" +
            "        \"expression\" : \"Is Patient Pregnant\"\n" +
            "      }\n" +
            "    }],\n" +
            "    \"action\" : [{\n" +
            "      \"condition\" : [{\n" +
            "        \"kind\" : \"applicability\",\n" +
            "        \"expression\" : {\n" +
            "          \"language\" : \"text/cql\",\n" +
            "          \"expression\" : \"Should Administer Zika Virus Exposure Assessment\"\n" +
            "        }\n" +
            "      }],\n" +
            "      \"definitionCanonical\" : \"http://example.org/fhir/ActivityDefinition/administer-zika-virus-exposure-assessment\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"condition\" : [{\n" +
            "        \"kind\" : \"applicability\",\n" +
            "        \"expression\" : {\n" +
            "          \"language\" : \"text/cql\",\n" +
            "          \"expression\" : \"Should Order Serum + Urine rRT-PCR Test\"\n" +
            "        }\n" +
            "      }],\n" +
            "      \"definitionCanonical\" : \"http://example.org/fhir/ActivityDefinition/order-serum-urine-rrt-pcr-test\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"condition\" : [{\n" +
            "        \"kind\" : \"applicability\",\n" +
            "        \"expression\" : {\n" +
            "          \"language\" : \"text/cql\",\n" +
            "          \"expression\" : \"Should Order Serum Zika Virus IgM + Dengue Virus IgM\"\n" +
            "        }\n" +
            "      }],\n" +
            "      \"definitionCanonical\" : \"http://example.org/fhir/ActivityDefinition/order-serum-zika-dengue-virus-igm\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"condition\" : [{\n" +
            "        \"kind\" : \"applicability\",\n" +
            "        \"expression\" : {\n" +
            "          \"language\" : \"text/cql\",\n" +
            "          \"expression\" : \"Should Consider IgM Antibody Testing\"\n" +
            "        }\n" +
            "      }],\n" +
            "      \"definitionCanonical\" : \"http://example.org/fhir/ActivityDefinition/consider-igm-antibody-testing\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"condition\" : [{\n" +
            "        \"kind\" : \"applicability\",\n" +
            "        \"expression\" : {\n" +
            "          \"language\" : \"text/cql\",\n" +
            "          \"expression\" : \"Should Provide Mosquito Prevention and Contraception Advice\"\n" +
            "        }\n" +
            "      }],\n" +
            "      \"action\" : [{\n" +
            "        \"definitionCanonical\" : \"http://example.org/fhir/ActivityDefinition/provide-mosquito-prevention-advice\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"definitionCanonical\" : \"http://example.org/fhir/ActivityDefinition/provide-contraception-advice\"\n" +
            "      }]\n" +
            "    }]\n" +
            "  }]\n" +
            "}";
}
