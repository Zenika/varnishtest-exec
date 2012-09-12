package com.zenika.plugins.varnishtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.maven.plugin.Mojo;

import com.google.common.io.Closeables;

class VarnishtestStreamHandler implements ExecuteStreamHandler {
	
	private enum ThreadType {OUTPUT, ERROR};
	
	private Thread outputThread = null;
	private Thread errorThread = null;
	
	private final Mojo mojo;
	
	VarnishtestStreamHandler(Mojo mojo) {
		if (mojo == null) {
			throw new IllegalArgumentException("Null Mojo");
		}
		this.mojo = mojo;
	}

	@Override
	public void setProcessOutputStream(final InputStream is) {
		outputThread = new Thread(new VarnishtestConsoleLogger(is, ThreadType.OUTPUT));
	}

	@Override
	public void setProcessErrorStream(final InputStream is) {
		errorThread = new Thread(new VarnishtestConsoleLogger(is, ThreadType.ERROR));
	}

	@Override
	public void setProcessInputStream(OutputStream os) throws IOException {
	}

	@Override
	public void start() {
		if (outputThread != null) {
			outputThread.start();
		}
		if (errorThread != null) {
			errorThread.start();
		}
	}

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

		@Override
		public void run() {
			Reader reader = null;
			BufferedReader lineReader = null;
			try {
				reader = new InputStreamReader(is);
				lineReader = new BufferedReader(reader);
				
				String line = null;
				while ((line = lineReader.readLine()) != null) {
					logOutput(line);
				}
			}
			catch (IOException e) {
				VarnishtestStreamHandler.this.mojo.getLog().error(e);
				throw new IllegalStateException(e);
			}
			finally {
				Closeables.closeQuietly(lineReader);
				Closeables.closeQuietly(reader);
			}
		}

		private void logOutput(String line) {
			if (threadType == ThreadType.OUTPUT) {
				VarnishtestStreamHandler.this.mojo.getLog().info(line);
			}
			else {
				VarnishtestStreamHandler.this.mojo.getLog().error(line);
			}
		}
	}
	
}
