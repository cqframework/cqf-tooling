package org.opencds.cqf.tooling.operations;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.testng.annotations.Test;

public class ExecutableOperationAdapterTest {

    // A test operation with various parameter types
    @Operation(name = "TestOp")
    static class TestOperation implements ExecutableOperation {
        @OperationParam(
                alias = {"inputpath", "ip"},
                setter = "setInputPath",
                required = true,
                description = "Input")
        private String inputPath;

        @OperationParam(
                alias = {"encoding", "e"},
                setter = "setEncoding",
                defaultValue = "json",
                description = "Encoding")
        private String encoding;

        @OperationParam(
                alias = {"count", "c"},
                setter = "setCount",
                description = "Count")
        private Integer count;

        @OperationParam(
                alias = {"verbose", "v"},
                setter = "setVerbose",
                description = "Verbose")
        private Boolean verbose;

        private boolean executed = false;

        public void setInputPath(String inputPath) {
            this.inputPath = inputPath;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public void setVerbose(Boolean verbose) {
            this.verbose = verbose;
        }

        @Override
        public void execute() {
            executed = true;
        }

        // Accessors for verification
        String getInputPath() {
            return inputPath;
        }

        String getEncoding() {
            return encoding;
        }

        Integer getCount() {
            return count;
        }

        Boolean getVerbose() {
            return verbose;
        }

        boolean isExecuted() {
            return executed;
        }
    }

    // An operation with no parameters
    @Operation(name = "NoParamOp")
    static class NoParamOperation implements ExecutableOperation {
        private boolean executed = false;

        @Override
        public void execute() {
            executed = true;
        }

        boolean isExecuted() {
            return executed;
        }
    }

    // Basic execution

    @Test
    public void execute_bindsRequiredParam_andExecutesDelegate() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-ip=/some/path"});

        assertEquals(op.getInputPath(), "/some/path");
        assertTrue(op.isExecuted());
    }

    @Test
    public void execute_bindsAllParams() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-inputpath=/input", "-e=xml", "-count=5", "-verbose=true"});

        assertEquals(op.getInputPath(), "/input");
        assertEquals(op.getEncoding(), "xml");
        assertEquals(op.getCount(), Integer.valueOf(5));
        assertTrue(op.getVerbose());
    }

    // Default values

    @Test
    public void execute_appliesDefaultValue_whenNotProvided() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-ip=/input"});

        assertEquals(op.getEncoding(), "json", "Default value should be applied");
    }

    // Required parameter validation

    @Test
    public void execute_missingRequiredParam_throws() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        assertThrows(InvalidOperationArgs.class, () -> adapter.execute(new String[] {"-e=json"}));
    }

    // Optional parameters left null

    @Test
    public void execute_optionalParamNotProvided_remainsNull() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-ip=/input"});

        assertNull(op.getCount(), "Unset optional Integer should remain null");
        assertNull(op.getVerbose(), "Unset optional Boolean should remain null");
    }

    // Double-dash arguments

    @Test
    public void execute_doubleDashArgs_parsedCorrectly() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"--inputpath=/input", "--encoding=xml"});

        assertEquals(op.getInputPath(), "/input");
        assertEquals(op.getEncoding(), "xml");
    }

    // Case-insensitive keys

    @Test
    public void execute_caseInsensitiveKeys() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-INPUTPATH=/input", "-Encoding=xml"});

        assertEquals(op.getInputPath(), "/input");
        assertEquals(op.getEncoding(), "xml");
    }

    // Non-dash arguments are skipped

    @Test
    public void execute_nonDashArgs_skipped() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"SomeOperationName", "-ip=/input"});

        assertEquals(op.getInputPath(), "/input");
        assertTrue(op.isExecuted());
    }

    // Help argument short-circuits execution

    @Test
    public void execute_helpArg_doesNotExecuteDelegate() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-h"});

        assertNull(op.getInputPath(), "Params should not be bound when help is requested");
        assertTrue(!op.isExecuted(), "Delegate should not execute when help is requested");
    }

    @Test
    public void execute_helpArgDashQuestion_doesNotExecuteDelegate() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-?"});

        assertTrue(!op.isExecuted());
    }

    @Test
    public void execute_helpArgAmongOtherArgs_doesNotExecute() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-ip=/input", "-help"});

        assertTrue(!op.isExecuted(), "Help flag should short-circuit even with other args present");
    }

    // No-param operation

    @Test
    public void execute_noParamOperation_executesSuccessfully() {
        NoParamOperation op = new NoParamOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {});

        assertTrue(op.isExecuted());
    }

    // Args without equals sign are skipped

    @Test
    public void execute_argWithoutEquals_skipped() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-ip=/input", "-standalone"});

        assertEquals(op.getInputPath(), "/input");
        assertTrue(op.isExecuted());
    }

    // Value with equals sign in it

    @Test
    public void execute_valueContainingEquals_preservedFully() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-ip=/path/with=equals"});

        assertEquals(op.getInputPath(), "/path/with=equals",
                "Value containing '=' should be preserved fully");
    }

    // Value with spaces

    @Test
    public void execute_valueWithSpaces_preservedFully() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        adapter.execute(new String[] {"-ip=/path/with spaces/in it"});

        assertEquals(op.getInputPath(), "/path/with spaces/in it");
    }

    // Empty args array

    @Test
    public void execute_emptyArgs_requiredParamThrows() {
        TestOperation op = new TestOperation();
        ExecutableOperationAdapter adapter = new ExecutableOperationAdapter(op);

        assertThrows(InvalidOperationArgs.class, () -> adapter.execute(new String[] {}));
    }
}
