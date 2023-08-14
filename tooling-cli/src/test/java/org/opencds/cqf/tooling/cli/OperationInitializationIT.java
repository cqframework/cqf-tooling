package org.opencds.cqf.tooling.cli;

import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.exception.OperationNotFound;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OperationInitializationIT {
   @Test
   void missingOperationName() {
      String[] args = new String[]{};
      Assert.assertThrows(OperationNotFound.class, () -> Main.main(args));
   }

   @Test
   void invalidOperationName() {
      String[] args = new String[]{ "-NonexistentOperationName" };
      Assert.assertThrows(OperationNotFound.class, () -> Main.main(args));
   }

   @Test
   void InvalidOperationDeclaration() {
      String[] args = new String[]{ "BundleResources", "-ptr=some/directory/path" };
      Assert.assertThrows(InvalidOperationArgs.class, () -> Main.main(args));
   }

   @Test
   void missingRequiredOperationArgs() {
      String[] args = new String[]{ "-BundleResources" };
      Assert.assertThrows(InvalidOperationArgs.class, () -> Main.main(args));
   }
}
