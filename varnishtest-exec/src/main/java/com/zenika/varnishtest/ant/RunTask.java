package com.zenika.varnishtest.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;

import com.zenika.varnishtest.CommandLineBuilder;
import com.zenika.varnishtest.VarnishtestException;
import com.zenika.varnishtest.VarnishtestReport;
import com.zenika.varnishtest.VarnishtestRunner;

/**
 * Runs tests with varnishtest.
 *
 * <pre>{@code <project xmlns:varnishtest="antlib:com.zenika.varnishtest.ant"
 *     name="minimal-configuration" basedir="." default="test">
 * 
 *     <target name="test" description="Run varnishtest">
 *         <varnishtest:run>
 *             <fileset dir="src/test/varnish">
 *                 <include name="**.vtc"/>
 *             </fileset>
 *         </varnishtest:run>
 *     </target>
 * 
 * </project>}</pre>
 *
 * @author Dridi Boukelmoune
 * @ant.task
 */
public class RunTask extends Task {

	/**
	 * Default test execution timeout in seconds.
	 */
	public static final int DEFAULT_TIMEOUT = 20;

	private final CommandLineBuilder builder = new CommandLineBuilder();
	private final List<ResourceCollection> collections = new ArrayList<ResourceCollection>();
	private final LogOutputHandler handler = new LogOutputHandler(this);
	
	private File workingDirectory = null;
	private File reportsDirectory = null;
	private int timeout = DEFAULT_TIMEOUT;
	private int testCaseCounter = 1;

	/**
	 * Initialize default values.
	 * @since 0.2
	 */
	@Override
	public void init() {
		workingDirectory = getProject().getBaseDir();
	}

	/**
	 * Run the varnish test cases (vtc).
	 */
	@Override
	public void execute() {
		createReportsDirectory();

		for (ResourceCollection collection : collections) {
			@SuppressWarnings("unchecked")
			Iterator<Resource> iterator = collection.iterator();
			while (iterator.hasNext()) {
				runTestCase(iterator.next(), testCaseCounter);
				testCaseCounter++;
			}
		}
	}

	private void createReportsDirectory() {
		if (reportsDirectory == null) {
			return;
		}

		reportsDirectory.mkdirs();

		if ( ! reportsDirectory.exists() ) {
			throw new BuildException("Varnishtest reports directory not created : " + reportsDirectory);
		}
	}

	private void writeReport(VarnishtestReport report, int number) {
		if (reportsDirectory == null) {
			return;
		}

		try {
			report.write(reportsDirectory, number);
		}
		catch (IOException e) {
			throw new BuildException("Could not write varnishtest's report", e);
		}
	}

	private void runTestCase(Resource resource, int testCaseCounter) {
		VarnishtestRunner runner = new VarnishtestRunner(builder, workingDirectory);
		try {
			VarnishtestReport report = runner.runTestCase(new File(resource.toString()), handler, timeout);
			writeReport(report, testCaseCounter);
		}
		catch (VarnishtestException e) {
			writeReport(e.getReport(), testCaseCounter);
			throw new BuildException("Test failure : " + resource, e);
		}
		catch (IOException e) {
			throw new BuildException("An error occured", e);
		}
	}

	/**
	 * Add a set of varnish test cases (vtc).
	 * @param collection a set of vtc
	 * @throws BuildException when a test case is not on a filesystem
	 */
	public void add(ResourceCollection collection) {
		if ( collection.isFilesystemOnly() ) {
			collections.add(collection);
		}
		else {
			throw new BuildException("Only FileSystem resources are supported.");
		}
	}

	/**
	 * Add a macro to varnishtest's command-line.
	 * @param macro The key/value macro
	 */
	public void addConfiguredMacro(Macro macro) {
		try {
			macro.addTo(builder);
		}
		catch (RuntimeException e) {
			throw new BuildException(e);
		}
	}

	/**
	 * Set a different command for varnishtest, for instance
	 * {@code /usr/local/bin/varnishtest}.
	 * @param varnishtestCommand The command to run
	 */
	public void setVarnishtestCommand(String varnishtestCommand) {
		builder.setVarnishtestCommand(varnishtestCommand);
	}

	/**
	 * Set a different command for varnishd, for instance
	 * {@code /usr/sbin/varnishd} for unprivileged users without
	 * {@code /usr/sbin} in their {@code PATH}.
	 * @param varnishdCommand The command to run
	 */
	public void setVarnishdCommand(String varnishdCommand) {
		builder.setVarnishdCommand(varnishdCommand);
	}

	/**
	 * Set the directory in which varnishtest will be executed,
	 * defaults to the base directory.
	 * @param workingDirectory The working directory
	 *
	 * @since 0.2
	 */
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/**
	 * Set the directory where the reports will be written. It is not defined
	 * by default and therefore, tests reports are not written by default.
	 * @param reportsDirectory The report files directory
	 */
	public void setReportsDirectory(File reportsDirectory) {
		this.reportsDirectory = reportsDirectory;
	}

	/**
	 * Set the per-test timeout in seconds, defaults to {@value #DEFAULT_TIMEOUT} seconds.
	 * @param timeout The test timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
