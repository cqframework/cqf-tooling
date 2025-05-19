package org.opencds.cqf.tooling.common;

import org.opencds.cqf.tooling.common.r4.SoftwareSystemHelper;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class BaseSoftwareSystemHelperTest {

	@Test
	public void testGetRootDir() {
		assertEquals("Root Dir", (new SoftwareSystemHelper("Root Dir")).getRootDir());
	}


	@Test
	public void testGetSystemIsValid() {
		SoftwareSystemHelper softwareSystemHelper = new SoftwareSystemHelper("Root Dir");
		assertTrue(softwareSystemHelper.getSystemIsValid(new SoftwareSystem("Name", "1.0.2", null)));
	}

	@Test
	public void testGetSystemIsValid2() {
		SoftwareSystemHelper softwareSystemHelper = new SoftwareSystemHelper("Root Dir");
		assertFalse(softwareSystemHelper.getSystemIsValid(new SoftwareSystem(null, "1.0.2", null)));
	}

	@Test
	public void testGetSystemIsValid3() {
		SoftwareSystemHelper softwareSystemHelper = new SoftwareSystemHelper("Root Dir");
		assertFalse(softwareSystemHelper.getSystemIsValid(new SoftwareSystem("", "1.0.2", null)));
	}

	@Test
	public void testGetSystemIsValid4() {
		SoftwareSystemHelper softwareSystemHelper = new SoftwareSystemHelper("Root Dir");
		assertFalse(softwareSystemHelper.getSystemIsValid(new SoftwareSystem("Name", "", null)));
	}

	@Test
	public void testGetSystemIsValid5() {
		assertFalse((new SoftwareSystemHelper("Root Dir")).getSystemIsValid(null));
	}



}
