package com.example.SearchService.command;

/**
 * Command interface defining the contract for all command objects.
 * Part of the Command design pattern implementation.
 * 
 * @param <T> The type of the result returned by the execute method
 */
public interface Command<T> {
    /**
     * Executes the command and returns the result.
     * 
     * @return The result of the command execution
     */
    T execute();
} 