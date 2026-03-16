package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class LogUtilsTest {

    @Test
    public void testPutExceptionAndWarnClearsWarnings() {
        // Put some warnings, then warn() should format and clear them
        LogUtils.putException("TestOp", new RuntimeException("something broke"));
        LogUtils.putException("TestOp2", "missing input");

        // warn() should not throw and should clear the buffer
        LogUtils.warn("TestLibrary");

        // Calling warn again with no warnings should be a no-op
        LogUtils.warn("TestLibrary");
    }

    @Test
    public void testWarnWithNoWarningsIsNoOp() {
        // Ensure clean state
        LogUtils.warn("cleanup");
        // Should not throw
        LogUtils.warn("EmptyLibrary");
    }

    @Test
    public void testPutExceptionWithNullMessage() {
        // Exception with null message should use toString() fallback
        LogUtils.putException("NullMsg", new RuntimeException());
        LogUtils.warn("NullMsgLib");
    }

    @Test
    public void testPutExceptionStringWarning() {
        LogUtils.putException("SomeOp", "This is a warning message");
        LogUtils.warn("WarningLib");
    }

    @Test
    public void testInfoDoesNotThrow() {
        LogUtils.info("Test message");
    }

    @Test
    public void testTruncatesLongMessages() {
        // Build a message > 500 chars to trigger truncation
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longMessage.append("This is a long error message line. ");
        }
        LogUtils.putException("LongMsgOp", longMessage.toString());
        // warn() should truncate without throwing
        LogUtils.warn("LongMsgLib");
    }

    @Test
    public void testTruncatesNullMessage() {
        LogUtils.putException("NullContent", (String) null);
        // The null value should be handled by truncateMessage -> "null message"
        LogUtils.warn("NullContentLib");
    }

    @Test
    public void testMultipleWarningsAccumulate() {
        LogUtils.putException("Op1", "first warning");
        LogUtils.putException("Op2", "second warning");
        LogUtils.putException("Op3", "third warning");
        // All three should be formatted and cleared by a single warn() call
        LogUtils.warn("MultiLib");
        // Second call should be no-op
        LogUtils.warn("MultiLib");
    }

    @Test
    public void testMultilineMessageTruncation() {
        // Build a multi-line message to exercise the split("\r\n") path
        String multiline = "Line one\r\nLine two\r\nLine three\r\nLine four";
        LogUtils.putException("MultiLine", multiline);
        LogUtils.warn("MultiLineLib");
    }
}
