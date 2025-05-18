package com.example.TransactionService.command;

import org.springframework.stereotype.Component;

/**
 * Invoker class for the Command pattern.
 * Responsible for executing commands without knowing what the commands do.
 */
@Component
public class CommandInvoker {

    /**
     * Executes a command and returns its result.
     * 
     * @param <T> The return type of the command
     * @param command The command to execute
     * @return The result of the command execution
     */
    public <T> T executeCommand(Command<T> command) {
        return command.execute();
    }
}
