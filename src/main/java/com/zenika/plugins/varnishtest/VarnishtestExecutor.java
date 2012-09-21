package com.zenika.plugins.varnishtest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.maven.plugin.Mojo;

class VarnishtestExecutor extends DefaultExecutor {
	
	VarnishtestExecutor(Mojo mojo) {
		if (mojo == null) {
			throw new IllegalArgumentException("Null mojo");
		}
		super.setStreamHandler(new VarnishtestStreamHandler(mojo));
	}

	@Override
	public void setExitValue(int value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setExitValues(int[] values) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setStreamHandler(ExecuteStreamHandler streamHandler) {
		throw new UnsupportedOperationException();
	}

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
