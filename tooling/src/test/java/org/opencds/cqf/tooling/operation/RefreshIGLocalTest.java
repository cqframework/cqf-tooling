package org.opencds.cqf.tooling.operation;

import org.testng.annotations.Test;

public class RefreshIGLocalTest {

   @Test
   void refreshLocalOpioidIG() {
      RefreshIGOperation refreshIGOperation = new RefreshIGOperation();
      refreshIGOperation.execute(
              new String[]{
                      "-RefreshIG",
                      "-ini=/Users/christopherschuler/Documents/workspace/cqframework/igs/opioid-cds-r4/ig.ini",
                      "-rp=/Users/christopherschuler/Documents/workspace/cqframework/igs/opioid-cds-r4/input/resources",
                      "-d", "-p", "-t", "-ss=false" });
   }
}
