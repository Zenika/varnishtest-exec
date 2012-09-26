package com.zenika.plugins.varnishtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.maven.plugin.logging.Log;

import com.google.common.io.Closeables;

/**
 * A stream handler that logs varnishtest's output and creates the test report
 * on the fly.
 * 
 * @author Dridi Boukelmoune
 */
class VarnishtestStreamHandler implements ExecuteStreamHandler {
	
	private enum ThreadType {OUTPUT, ERROR};
	
	private Thread outputThread = null;
	private Thread errorThread = null;
	
	private final VarnishtestReport report;
	private final Log log;
	
	/**
	 * Constructor.
	 * @param log the Maven logger
	 */
	VarnishtestStreamHandler(Log log) {
		this.log = log;
		this.report = new VarnishtestReport();
	}
	
	/**
	 * @return the varnishtest report
	 */
	VarnishtestReport getReport() {
		return report;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProcessOutputStream(final InputStream is) {
		outputThread = new Thread(new VarnishtestConsoleLogger(is, ThreadType.OUTPUT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProcessErrorStream(final InputStream is) {
		errorThread = new Thread(new VarnishtestConsoleLogger(is, ThreadType.ERROR));
	}

	/**
	 * We do not write in varnishtest's standard output, ignored.
	 */
	@Override
	public void setProcessInputStream(OutputStream os) throws IOException {
		// ignore
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		if (outputThread != null) {
			outputThread.start();
		}
		if (errorThread != null) {
			errorThread.start();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {
		if (outputThread != null) {
			try {
				outputThread.join();
				outputThread = null;
			}
			catch (InterruptedException e) {
				// ignore
			}
		}
		
		if (errorThread != null) {
			try {
				errorThread.join();
				errorThread = null;
			}
			catch (InterruptedException e) {
				// ignore
			}
		}
	}

	private class VarnishtestConsoleLogger implements Runnable {

		private final InputStream is;
		private final ThreadType threadType;

		private VarnishtestConsoleLogger(InputStream is, ThreadType threadType) {
			this.is = is;
			this.threadType = threadType;
		}

		/**
		 * Logs varnishtest's output and feeds the report.
		 */
		@Override
		public void run() {
			Reader reader = null;
			BufferedReader lineReader = null;
			try {
				reader = new InputStreamReader(is);
				lineReader = new BufferedReader(reader);
				
				String line = null;
				while ((line = lineReader.readLine()) != null) {
					report.log(line);
					logOutput(line);
				}
			}
			catch (IOException e) {
				VarnishtestStreamHandler.this.log.error(e);
				throw new IllegalStateException(e);
			}
			finally {
				Closeables.closeQuietly(lineReader);
				Closeables.closeQuietly(reader);
			}
		}

		private void logOutput(String line) {
			if (threadType == ThreadType.OUTPUT) {
				if (line.startsWith("#")) {
					VarnishtestStreamHandler.this.log.info(line);
				}
				else {
					VarnishtestStreamHandler.this.log.debug(line);
				}
			}
			else {
				VarnishtestStreamHandler.this.log.error(line);
			}
		}
	}
	
}
