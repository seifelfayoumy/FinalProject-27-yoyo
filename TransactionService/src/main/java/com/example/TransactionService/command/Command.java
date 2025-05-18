package com.example.TransactionService.command;

/**
 * Command interface defining the execute operation for the Command pattern.
 * @param <T> The return type of the command execution
 */
public interface Command<T> {
    /**
     * Executes the command and returns the result.
     * 
     * @return The result of the command execution
     */
    T execute();
}
