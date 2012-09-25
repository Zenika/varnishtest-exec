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


class VarnishtestRunner {
	
	private final CommandLine commandLine;
	private VarnishtestReport report;
	
	VarnishtestRunner(CommandLine commandLine) {
		this.commandLine = commandLine;
	}
	
	VarnishtestReport getReport() {
		return report;
	}
	
	void runTestCase(Log log, File testCase, int timeout) throws MojoExecutionException, MojoFailureException {
		
		VarnishtestExecutor executor = new VarnishtestExecutor(log);
		
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
			report = executor.getStreamHandler().getReport();
		}
		
	}
}
