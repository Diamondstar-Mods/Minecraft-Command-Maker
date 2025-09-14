package com.diamondstar.dlb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DLBTest {

    @Test
    public void testCommandRegistration() {
        // Test command registration functionality
        DLB dlb = new DLB();
        boolean result = dlb.registerCommand("testCommand");
        assertTrue(result, "Command should be registered successfully");
    }

    @Test
    public void testCommandExecution() {
        // Test command execution functionality
        DLB dlb = new DLB();
        dlb.registerCommand("testCommand");
        String output = dlb.executeCommand("testCommand");
        assertEquals("Command executed: testCommand", output, "Command execution output should match expected");
    }

    @Test
    public void testInvalidCommandExecution() {
        // Test execution of an unregistered command
        DLB dlb = new DLB();
        String output = dlb.executeCommand("invalidCommand");
        assertEquals("Command not found: invalidCommand", output, "Output should indicate command not found");
    }

    @Test
    public void testCommandAlias() {
        // Test command alias functionality
        DLB dlb = new DLB();
        dlb.registerCommand("originalCommand");
        dlb.addAlias("originalCommand", "aliasCommand");
        String output = dlb.executeCommand("aliasCommand");
        assertEquals("Command executed: originalCommand", output, "Alias command should execute the original command");
    }

    @Test
    public void testCommandWithArguments() {
        // Test command execution with arguments
        DLB dlb = new DLB();
        dlb.registerCommand("greet");
        String output = dlb.executeCommand("greet John");
        assertEquals("Hello, John!", output, "Greeting command should return the correct message");
    }
}