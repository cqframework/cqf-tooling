package org.opencds.cqf.tooling.common;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper;

public class BaseCqfmSoftwareSystemHelperTest {

	@Test
	public void testGetRootDir() {
		assertEquals("Root Dir", (new CqfmSoftwareSystemHelper("Root Dir")).getRootDir());
	}


	@Test
	public void testGetCqfToolingDeviceName() {
		assertEquals("cqf-tooling", (new CqfmSoftwareSystemHelper("Root Dir")).getCqfToolingDeviceName());
	}

	@Test
	public void testGetCqfmSoftwareSystemExtensionUrl() {
		assertEquals("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem",
				(new CqfmSoftwareSystemHelper("Root Dir")).getCqfmSoftwareSystemExtensionUrl());
	}

	@Test
	public void testGetSystemIsValid() {
		CqfmSoftwareSystemHelper cqfmSoftwareSystemHelper = new CqfmSoftwareSystemHelper("Root Dir");
		assertTrue(cqfmSoftwareSystemHelper.getSystemIsValid(new CqfmSoftwareSystem("Name", "1.0.2", null)));
	}

	@Test
	public void testGetSystemIsValid2() {
		CqfmSoftwareSystemHelper cqfmSoftwareSystemHelper = new CqfmSoftwareSystemHelper("Root Dir");
		assertFalse(cqfmSoftwareSystemHelper.getSystemIsValid(new CqfmSoftwareSystem(null, "1.0.2", null)));
	}

	@Test
	public void testGetSystemIsValid3() {
		CqfmSoftwareSystemHelper cqfmSoftwareSystemHelper = new CqfmSoftwareSystemHelper("Root Dir");
		assertFalse(cqfmSoftwareSystemHelper.getSystemIsValid(new CqfmSoftwareSystem("", "1.0.2", null)));
	}

	@Test
	public void testGetSystemIsValid4() {
		CqfmSoftwareSystemHelper cqfmSoftwareSystemHelper = new CqfmSoftwareSystemHelper("Root Dir");
		assertFalse(cqfmSoftwareSystemHelper.getSystemIsValid(new CqfmSoftwareSystem("Name", null, null)));
	}

	@Test
	public void testGetSystemIsValid5() {
		CqfmSoftwareSystemHelper cqfmSoftwareSystemHelper = new CqfmSoftwareSystemHelper("Root Dir");
		assertFalse(cqfmSoftwareSystemHelper.getSystemIsValid(new CqfmSoftwareSystem("Name", "", null)));
	}

	@Test
	public void testGetSystemIsValid6() {
		assertFalse((new CqfmSoftwareSystemHelper("Root Dir")).getSystemIsValid(null));
	}



}
