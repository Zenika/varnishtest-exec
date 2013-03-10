package com.zenika.varnishtest;

import java.io.IOException;

/**
 * This interface provides callback methods for varnishtest standard output and
 * error handling. Methods are called line-by-line and it is the responsibility
 * of the implementors to guarantee thread safety as different outputs might
 * come from different threads.
 * 
 * @author Dridi Boukelmoune
 */
public interface OutputHandler {
	/**
	 * Handles a line of text from the standard output.
	 * @param line a non-{@code null} string
	 */
	void handleOutput(String line);
	
	/**
	 * Handles a line of text from the standard error.
	 * @param line a non-{@code null} string
	 */
	void handleError(String line);
	
	/**
	 * Handles an IOException if any.
	 * @param e the exception
	 */
	void handleIOException(IOException e);
}
