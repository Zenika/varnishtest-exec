package com.zenika.plugins.varnishtest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


class VarnishtestRunner {
	
	private final CommandLine commandLine;
	
	VarnishtestRunner(CommandLine commandLine) {
		this.commandLine = commandLine;
	}
	
	void runTestCase(Mojo mojo, File testCase, int timeout) throws MojoExecutionException, MojoFailureException {
		
		Executor executor = new VarnishtestExecutor(mojo);
		
		if (timeout > 0) {
			ExecuteWatchdog watchdog = new ExecuteWatchdog( TimeUnit.SECONDS.toMillis(timeout) );
			executor.setWatchdog(watchdog);
		}
		
		CommandLine testCaseCommandLine = new CommandLine(commandLine);
		testCaseCommandLine.addArgument(testCase.getPath(), false);
		
		try {
			mojo.getLog().info("Executing : " + testCaseCommandLine);
			int exitValue = executor.execute(testCaseCommandLine); // TODO configure environment
			
			if ( executor.isFailure(exitValue) ) {
				throw new MojoFailureException("The test case failed : " + testCase);
			}
		}
		catch (ExecuteException e) {
			throw new MojoExecutionException("ExecuteException : " + e.getMessage(), e);
		}
		catch (IOException e) {
			throw new MojoExecutionException("IOException : " + e.getMessage(), e);
		}
		
	}
}
