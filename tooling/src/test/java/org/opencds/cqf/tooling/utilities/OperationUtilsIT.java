package org.opencds.cqf.tooling.utilities;

import org.opencds.cqf.tooling.operations.bundle.BundleResources;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OperationUtilsIT {

   @Test
   void testGetParamTypeFromMethod() {
      BundleResources bundleResources = new BundleResources();
      Class<?> clazz = OperationUtils.getParamType(bundleResources, "setOutputPath");
      Assert.assertTrue(clazz.isAssignableFrom(String.class));
   }

   @Test
   void testMapParamType() {
      String testString = "testString";
      Integer testInteger = 1;
      Boolean testBoolean = true;
      String stringMapResult = OperationUtils.mapParamType(testString, String.class);
      Integer integerMapResult = OperationUtils.mapParamType("1", Integer.class);
      Boolean booleanMapResult = OperationUtils.mapParamType("true", Boolean.class);

      Assert.assertEquals(stringMapResult, testString);
      Assert.assertEquals(integerMapResult, testInteger);
      Assert.assertEquals(booleanMapResult, testBoolean);
   }

   @Test
   void testHelpMenu() {
      String helpMenu = OperationUtils.getHelpMenu(new BundleResources());
      Assert.assertNotNull(helpMenu);
      Assert.assertEquals(helpMenu, bundleResourcesHelpMenu);
   }

   private final String bundleResourcesHelpMenu = System.lineSeparator() +
           "╔═════════════════════════╤════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗\n" +
           "║ Parameter               │ Description                                                                                                                                ║\n" +
           "╠═════════════════════════╪════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╣\n" +
           "║ -ptr | -pathtoresources │ Path to the directory containing the resource files to be consolidated into the new bundle (required)                                      ║\n" +
           "╟─────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╢\n" +
           "║ -e | -encoding          │ The file format to be used for representing the resulting Bundle { json, xml } (default json)                                              ║\n" +
           "╟─────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╢\n" +
           "║ -v | -version           │ FHIR version { stu3, r4, r5 } (default r4)                                                                                                 ║\n" +
           "╟─────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╢\n" +
           "║ -t | -type              │ The Bundle type as defined in the FHIR specification for the Bundle.type element (default transaction)                                     ║\n" +
           "╟─────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╢\n" +
           "║ -bid | -bundleid        │ A valid FHIR ID to be used as the ID for the resulting FHIR Bundle (optional)                                                              ║\n" +
           "╟─────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╢\n" +
           "║ -op | -outputPath       │ The directory path to which the generated Bundle file should be written (default src/main/resources/org/opencds/cqf/tooling/bundle/output) ║\n" +
           "╚═════════════════════════╧════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝\n";
}
