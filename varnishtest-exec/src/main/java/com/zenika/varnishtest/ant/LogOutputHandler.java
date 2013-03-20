package com.zenika.varnishtest.ant;

import java.io.IOException;

import org.apache.tools.ant.Project;

import com.zenika.varnishtest.OutputHandler;

/**
 * Logs varnishtest output.
 *
 * @author Dridi Boukelmoune
 */
class LogOutputHandler implements OutputHandler {

	private final RunTask task;

	/**
	 * Constructor.
	 * @param task the ant project
	 */
	LogOutputHandler(RunTask task) {
		this.task = task;
	}

	/**
	 * Logs varnishtest standard output at the {@link Project#MSG_DEBUG} level,
	 * except for test results.
	 * @see Project#MSG_INFO
	 * @see Project#MSG_DEBUG
	 */
	@Override
	public void handleOutput(String line) {
		// XXX tested only with varnish 3.0.3
		if (line.startsWith("#")) {
			task.log(line, Project.MSG_INFO);
		}
		else {
			task.log(line, Project.MSG_DEBUG);
		}
	}

	/**
	 * Logs varnishtest standard error at the {@link Project#MSG_ERR} level.
	 * @see Project#MSG_ERR
	 */
	@Override
	public void handleError(String line) {
		task.log(line, Project.MSG_ERR);
	}

	/**
	 * Logs potential {@link IOException} at the {@link Project#MSG_ERR} level.
	 * @see Project#MSG_ERR
	 */
	@Override
	public void handleIOException(IOException e) {
		task.log(e.getMessage(), e, Project.MSG_ERR);
	}
}
