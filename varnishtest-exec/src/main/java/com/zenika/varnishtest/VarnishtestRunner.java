package com.zenika.varnishtest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;

/**
 * This class can run multiple test cases with the same configuration. 
 * 
 * @author Dridi Boukelmoune
 */
public class VarnishtestRunner {

	private final File workingDirectory;
	private final CommandLineBuilder commandLineBuilder;

	/**
	 * Constructor.
	 * @param commandLineBuilder the command line with common arguments
	 */
	public VarnishtestRunner(CommandLineBuilder commandLineBuilder) {
		this(commandLineBuilder, null);
	}

	/**
	 * Constructor.
	 * @param commandLineBuilder the command line with common arguments
	 * @param workingDirectory working directory for varnishtest executions
	 *
	 * @since 0.2
	 */
	public VarnishtestRunner(CommandLineBuilder commandLineBuilder, File workingDirectory) {
		if (commandLineBuilder == null) {
			throw new NullPointerException("Null command line");
		}
		this.commandLineBuilder = new CommandLineBuilder(commandLineBuilder);
		this.workingDirectory = workingDirectory;
	}

	/**
	 * Runs a test case.
	 *
	 * @param testCase the test case to run
	 * 
	 * @return the report of the test execution
	 * 
	 * @throws IOException when communication with varnishtest breaks
	 * @throws VarnishtestException when a test case fails
	 */
	public VarnishtestReport runTestCase(File testCase) throws VarnishtestException, IOException {
		return runTestCase(testCase, null, -1);
	}

	/**
	 * Runs a test case.
	 * 
	 * @param testCase the test case to run
	 * @param timeout the test timeout
	 * 
	 * @return the report of the test execution
	 * 
	 * @throws IOException when communication with varnishtest breaks
	 * @throws VarnishtestException when a test case fails
	 */
	public VarnishtestReport runTestCase(File testCase, int timeout) throws VarnishtestException, IOException {
		return runTestCase(testCase, null, timeout);
	}

	/**
	 * Runs a test case.
	 * 
	 * @param testCase the test case to run
	 * @param handler the output handler
	 * 
	 * @return the report of the test execution
	 * 
	 * @throws IOException when communication with varnishtest breaks
	 * @throws VarnishtestException when a test case fails
	 */
	public VarnishtestReport runTestCase(File testCase, OutputHandler handler)
	throws VarnishtestException, IOException {
		return runTestCase(testCase, handler, -1);
	}

	/**
	 * Runs a test case.
	 * 
	 * @param testCase the test case to run
	 * @param handler the output handler
	 * @param timeout the test timeout
	 * 
	 * @return the report of the test execution
	 * 
	 * @throws IOException when communication with varnishtest breaks
	 * @throws VarnishtestException when a test case fails
	 */
	public VarnishtestReport runTestCase(File testCase, OutputHandler handler, int timeout)
	throws VarnishtestException, IOException {

		if (testCase == null) {
			throw new NullPointerException("Null test case");
		}

		VarnishtestStreamHandler streamHandler = new VarnishtestStreamHandler(handler);
		VarnishtestExecutor executor = new VarnishtestExecutor(streamHandler);

		if (timeout > 0) {
			ExecuteWatchdog watchdog = new ExecuteWatchdog( TimeUnit.SECONDS.toMillis(timeout) );
			executor.setWatchdog(watchdog);
		}

		CommandLine testCaseCommandLine = commandLineBuilder.toCommandLine();

		if (workingDirectory != null) {
			executor.setWorkingDirectory(workingDirectory);
			String relative = relativizePath(workingDirectory, testCase);
			testCaseCommandLine.addArgument(relative, false);
		}
		else {
			testCaseCommandLine.addArgument(testCase.getPath(), false);
		}

		try {
			executor.execute(testCaseCommandLine); // TODO configure environment ?
			// XXX breaking Demeter's law for the report
			return executor.getStreamHandler().getReport();
		}
		catch(ExecuteException e) {
			// XXX breaking Demeter's law for the report
			throw new VarnishtestException(executor.getStreamHandler().getReport(), e);
		}
	}

	private String relativizePath(File parent, File child) {
		assert parent != null;
		assert child != null;
		return parent.getAbsoluteFile().toURI().relativize( child.getAbsoluteFile().toURI() ).getPath();
	}
}
