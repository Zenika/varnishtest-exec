package com.zenika.plugins.varnishtest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * This class can run multiple test cases with the same configuration. 
 * 
 * @author Dridi Boukelmoune
 */
class VarnishtestRunner {
	
	private final CommandLine commandLine;
	private VarnishtestReport report;
	
	/**
	 * Constructor.
	 * @param commandLine the command line with common arguments
	 */
	VarnishtestRunner(CommandLine commandLine) {
		if (commandLine == null) {
			throw new NullPointerException("Null command line");
		}
		this.commandLine = new CommandLine(commandLine);
	}
	
	/**
	 * The result of the latest execution.
	 * @return the varnishtest report
	 */
	VarnishtestReport getReport() {
		return report;
	}
	
	/**
	 * Runs a test case.
	 * FIXME it should return the report
	 * 
	 * @param log the Maven logger
	 * @param testCase the test case to run
	 * @param timeout the test timeout
	 * 
	 * @throws MojoExecutionException when communication with varnishtest breaks
	 * @throws MojoFailureException when a test case fails
	 */
	void runTestCase(Log log, File testCase, int timeout) throws MojoExecutionException, MojoFailureException {
		
		VarnishtestStreamHandler streamHandler = new VarnishtestStreamHandler(log);
		VarnishtestExecutor executor = new VarnishtestExecutor(streamHandler);
		
		if (timeout > 0) {
			ExecuteWatchdog watchdog = new ExecuteWatchdog( TimeUnit.SECONDS.toMillis(timeout) );
			executor.setWatchdog(watchdog);
		}
		
		CommandLine testCaseCommandLine = new CommandLine(commandLine);
		testCaseCommandLine.addArgument(testCase.getPath(), false);
		
		try {
			log.info("Executing : " + testCaseCommandLine);
			executor.execute(testCaseCommandLine); // TODO configure environment
		}
		catch (ExecuteException e) {
			throw new MojoFailureException("ExecuteException : " + e.getMessage(), e);
		}
		catch (IOException e) {
			throw new MojoExecutionException("IOException : " + e.getMessage(), e);
		}
		finally {
			// XXX breaking Demeter's law
			report = executor.getStreamHandler().getReport();
		}
		
	}
}
