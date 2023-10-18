package org.opencds.cqf.tooling.utilities;

import org.junit.Assert;
import org.junit.Test;
import org.opencds.cqf.tooling.exception.InvalidIdException;

public class IDUtilsTest {

    @Test
    public void testValidateId_Valid() {
        String[] alphanumericIds = {
            "4b6c5c3f-9252-413c-a231-a5d0d8f2edcc",
            "a1",
            "abcdef-ghijkl-mnopqrs-tuvwxyz",
            "valid-id-1",
        };
        for (String validId: alphanumericIds) {
            IDUtils.validateId(validId, false);
        }

        String[] numericIds = {
                "12345-67891-01112-12131",
                "0",
        };
        for (String validId: numericIds) {
            IDUtils.validateId(validId, true);
        }
    }

    @Test
    public void testValidateId_Invalid() {
        String[] invalidIds = {
                "12345-67891-01112-12131",
                "0",
                "",
                "4b6c5c3f-9252-413c-a231-a5d0d8f2edcc-4b6c5c3f-9252-413c-a231-a5d0d8f2edcc-4b6c5c3f-9252-413c-a231",

        };
        for (String invalidId: invalidIds) {
            Assert.assertThrows(InvalidIdException.class, () -> IDUtils.validateId(invalidId, false));
        }
    }
}
