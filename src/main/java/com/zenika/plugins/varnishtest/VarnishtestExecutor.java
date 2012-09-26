package com.zenika.plugins.varnishtest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.environment.EnvironmentUtils;

/**
 * An {@link Executor} that creates a process with an array of commnand-line
 * arguments instead of a string.
 * 
 * @author Dridi Boukelmoune
 * @author Olivier Bourgain
 * @see #launch(CommandLine, Map, File)
 */
class VarnishtestExecutor extends DefaultExecutor {
	
	/**
	 * Constructor.
	 * @param streamHandler the varnishtest stream handler
	 */
	VarnishtestExecutor(VarnishtestStreamHandler streamHandler) {
		if (streamHandler == null) {
			throw new NullPointerException("Null stream handler");
		}
		super.setStreamHandler(streamHandler);
	}

	/**
	 * Varnishtest exits with a non-zero on failure.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void setExitValue(int value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Varnishtest exits with a non-zero on failure.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void setExitValues(int[] values) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * A co-variant signature of {@link Executor#getStreamHandler()}.
	 */
	@Override
	public VarnishtestStreamHandler getStreamHandler() {
		return (VarnishtestStreamHandler) super.getStreamHandler();
	}

	/**
	 * The stream handler is transmitted in the constructor.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void setStreamHandler(ExecuteStreamHandler streamHandler) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a process with an array of arguments. It seems to be the only
	 * safe way to create a process with arguments containing spaces. The
	 * implementation of {@link Runtime#exec(String, String[], File)} doesn't
	 * appear to process single or double quotes when tokenizing a
	 * command-line.
	 * 
	 * @see Runtime#exec(String[], String[], File)
	 */
	@Override
	protected Process launch(CommandLine commandLine, @SuppressWarnings("rawtypes") Map environment,
			File workingDirectory) throws IOException {

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

		String[] envVars = EnvironmentUtils.toStrings(environment);
		String[] arguments = commandLine.getArguments();
		String[] command = new String[arguments.length + 1];
		
		command[0] = commandLine.getExecutable();
		System.arraycopy(arguments, 0, command, 1, arguments.length);

		return Runtime.getRuntime().exec(command, envVars, workingDirectory);
	}
}
