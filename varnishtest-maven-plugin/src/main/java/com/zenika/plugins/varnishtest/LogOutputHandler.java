package com.zenika.plugins.varnishtest;

import java.io.IOException;

import org.apache.maven.plugin.logging.Log;

import com.zenika.varnishtest.OutputHandler;

/**
 * Logs varnishtest output.
 * 
 * @author Dridi Boukelmoune
 */
class LogOutputHandler implements OutputHandler {
	
	private final Log log;
	
	/**
	 * Constructor.
	 * @param log the maven logger
	 */
	LogOutputHandler(Log log) {
		this.log = log;
	}
	
	/**
	 * Logs varnishtest standard output at the debug level, except for test results.
	 */
	@Override
	public void handleOutput(String line) {
		// XXX tested only with varnish 3.0.3
		if (line.startsWith("#")) {
			log.info(line);
		}
		else {
			log.debug(line);
		}
	}
	
	/**
	 * Logs varnishtest standard error at the error level.
	 */
	@Override
	public void handleError(String line) {
		log.error(line);
	}
	
	/**
	 * Logs potential {@link IOException} at the error level.
	 */
	@Override
	public void handleIOException(IOException e) {
		log.error(e);
	}
}
