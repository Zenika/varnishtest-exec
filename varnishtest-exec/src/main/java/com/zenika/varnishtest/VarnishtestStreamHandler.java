package com.zenika.varnishtest;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.commons.exec.ExecuteStreamHandler;

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
	private final OutputHandler handler;
	
	/**
	 * Constructor.
	 * @param handler the output handler
	 */
	VarnishtestStreamHandler(OutputHandler handler) {
		this.handler = (handler != null) ? handler : NOP_HANDLER;
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
	public void setProcessOutputStream(final InputStream is) {
		outputThread = new Thread(new VarnishtestConsoleLogger(is, ThreadType.OUTPUT));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProcessErrorStream(final InputStream is) {
		errorThread = new Thread(new VarnishtestConsoleLogger(is, ThreadType.ERROR));
	}

	/**
	 * We do not write in varnishtest's standard input, ignored.
	 */
	public void setProcessInputStream(OutputStream os) throws IOException {
		// ignore
	}

	/**
	 * {@inheritDoc}
	 */
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

	private final class VarnishtestConsoleLogger implements Runnable {

		private final InputStream is;
		private final ThreadType threadType;

		private VarnishtestConsoleLogger(InputStream is, ThreadType threadType) {
			this.is = is;
			this.threadType = threadType;
		}

		/**
		 * Logs varnishtest's output and feeds the report.
		 */
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
				VarnishtestStreamHandler.this.handler.handleIOException(e);
				throw new IllegalStateException(e);
			}
			finally {
				closeQuietly(lineReader);
				closeQuietly(reader);
			}
		}
		
		private void closeQuietly(Closeable closeable) {
			try {
				if (closeable != null) {
					closeable.close();
				}
			}
			catch(IOException e) {
				// ignore
			}
		}

		private void logOutput(String line) {
			if (threadType == ThreadType.OUTPUT) {
				VarnishtestStreamHandler.this.handler.handleOutput(line);
			}
			else {
				VarnishtestStreamHandler.this.handler.handleError(line);
			}
		}
	}
	
	private static final OutputHandler NOP_HANDLER = new OutputHandler() {
		public void handleOutput(String line) {
		}
		public void handleError(String line) {
		}
		public void handleIOException(IOException e) {
		}
	}; 
}
