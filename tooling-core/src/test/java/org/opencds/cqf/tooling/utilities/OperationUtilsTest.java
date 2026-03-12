package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.testng.annotations.Test;

public class OperationUtilsTest {

    // Test fixture: a simple ExecutableOperation with annotated fields
    static class SampleOperation implements ExecutableOperation {
        @OperationParam(
                alias = {"input", "i"},
                setter = "setInput",
                required = true,
                description = "Input path")
        private String input;

        @OperationParam(
                alias = {"count", "c"},
                setter = "setCount",
                required = false,
                defaultValue = "10",
                description = "Item count")
        private Integer count;

        @OperationParam(
                alias = {"verbose", "v"},
                setter = "setVerbose",
                description = "Verbose output")
        private Boolean verbose;

        private String notAnnotated;

        public void setInput(String input) {
            this.input = input;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public void setVerbose(Boolean verbose) {
            this.verbose = verbose;
        }

        // Zero-arg method with a name that could match
        public void reset() {
            this.input = null;
            this.count = null;
        }

        @Override
        public void execute() {}
    }

    // getParamType

    @Test
    public void getParamType_stringSetter_returnsString() {
        Class<?> type = OperationUtils.getParamType(new SampleOperation(), "setInput");
        assertEquals(type, String.class);
    }

    @Test
    public void getParamType_integerSetter_returnsInteger() {
        Class<?> type = OperationUtils.getParamType(new SampleOperation(), "setCount");
        assertEquals(type, Integer.class);
    }

    @Test
    public void getParamType_booleanSetter_returnsBoolean() {
        Class<?> type = OperationUtils.getParamType(new SampleOperation(), "setVerbose");
        assertEquals(type, Boolean.class);
    }

    @Test
    public void getParamType_nonExistentMethod_throws() {
        assertThrows(
                InvalidOperationArgs.class, () -> OperationUtils.getParamType(new SampleOperation(), "noSuchMethod"));
    }

    @Test
    public void getParamType_zeroArgMethod_throws() {
        // Regression: used to throw ArrayIndexOutOfBoundsException instead of InvalidOperationArgs
        assertThrows(
                InvalidOperationArgs.class, () -> OperationUtils.getParamType(new SampleOperation(), "reset"));
    }

    // mapParamType

    @Test
    public void mapParamType_stringToString_passThrough() {
        String result = OperationUtils.mapParamType("hello", String.class);
        assertEquals(result, "hello");
    }

    @Test
    public void mapParamType_stringToInteger_converts() {
        Integer result = OperationUtils.mapParamType("42", Integer.class);
        assertEquals(result, Integer.valueOf(42));
    }

    @Test
    public void mapParamType_stringToIntegerNegative_converts() {
        Integer result = OperationUtils.mapParamType("-5", Integer.class);
        assertEquals(result, Integer.valueOf(-5));
    }

    @Test
    public void mapParamType_stringToBoolean_trueConverts() {
        Boolean result = OperationUtils.mapParamType("true", Boolean.class);
        assertTrue(result);
    }

    @Test
    public void mapParamType_stringToBoolean_falseConverts() {
        Boolean result = OperationUtils.mapParamType("false", Boolean.class);
        assertFalse(result);
    }

    @Test
    public void mapParamType_stringToBoolean_nonBooleanIsFalse() {
        // Boolean.valueOf("yes") returns false per Java spec -- document this behavior
        Boolean result = OperationUtils.mapParamType("yes", Boolean.class);
        assertFalse(result, "Boolean.valueOf treats anything other than 'true' as false");
    }

    @Test
    public void mapParamType_unsupportedType_throws() {
        assertThrows(InvalidOperationArgs.class, () -> OperationUtils.mapParamType("1.5", Double.class));
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void mapParamType_invalidInteger_throws() {
        OperationUtils.mapParamType("not-a-number", Integer.class);
    }

    // getOperationParamCount

    @Test
    public void getOperationParamCount_returnsAnnotatedFieldCount() {
        assertEquals(OperationUtils.getOperationParamCount(new SampleOperation()), 3);
    }

    // formatAliases

    @Test
    public void formatAliases_singleAlias_formatted() {
        assertEquals(OperationUtils.formatAliases(new String[] {"input"}), "-input");
    }

    @Test
    public void formatAliases_multipleAliases_pipeSeparated() {
        assertEquals(OperationUtils.formatAliases(new String[] {"input", "i"}), "-input | -i");
    }

    @Test
    public void formatAliases_threeAliases_allFormatted() {
        assertEquals(OperationUtils.formatAliases(new String[] {"path", "p", "inputpath"}), "-path | -p | -inputpath");
    }

    // isHelpArg

    @Test
    public void isHelpArg_dashH_true() {
        assertTrue(OperationUtils.isHelpArg("-h"));
    }

    @Test
    public void isHelpArg_dashHelp_true() {
        assertTrue(OperationUtils.isHelpArg("-help"));
    }

    @Test
    public void isHelpArg_dashQuestion_true() {
        assertTrue(OperationUtils.isHelpArg("-?"));
    }

    @Test
    public void isHelpArg_otherArg_false() {
        assertFalse(OperationUtils.isHelpArg("-input"));
        assertFalse(OperationUtils.isHelpArg("help"));
        assertFalse(OperationUtils.isHelpArg("--help"));
    }

    // getHelpMenu

    @Test
    public void getHelpMenu_containsAllParamDescriptions() {
        String menu = OperationUtils.getHelpMenu(new SampleOperation());
        assertNotNull(menu);
        assertTrue(menu.contains("Input path"), "Should contain input description");
        assertTrue(menu.contains("Item count"), "Should contain count description");
        assertTrue(menu.contains("Verbose output"), "Should contain verbose description");
    }

    @Test
    public void getHelpMenu_containsAliases() {
        String menu = OperationUtils.getHelpMenu(new SampleOperation());
        assertTrue(menu.contains("-input"), "Should contain primary alias");
        assertTrue(menu.contains("-i"), "Should contain short alias");
    }
}
