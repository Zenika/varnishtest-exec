package com.zenika.varnishtest.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

import com.zenika.varnishtest.CommandLineBuilder;
import com.zenika.varnishtest.VarnishtestException;
import com.zenika.varnishtest.VarnishtestReport;
import com.zenika.varnishtest.VarnishtestRunner;

/**
 * Run tests with varnishtest.
 * 
 * @author Dridi Boukelmoune
 */
@Mojo(name = "run",
		threadSafe = false,
		requiresProject = true,
		defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class RunMojo extends AbstractMojo {

	private static final String[] EMPTY_STRING_ARRAY = new String[] {};
	private static final String[] DEFAULT_INCLUDE = new String[] { "src/test/varnish/**.vtc" };

	@Component
	private MavenProject project;

	/**
	 * Working directory for varnishtest execution.
	 * @since 0.2
	 */
	@Parameter(defaultValue = "${project.basedir}", property = "varnishtest.workingDirectory")
	private File workingDirectory;

	/**
	 * Base directory where all reports are written to.
	 */
	@Parameter(defaultValue = "${project.build.directory}/varnishtest-reports", property = "varnishtest.reportsDirectory")
	private File reportsDirectory;

	/**
	 * The command to run for varnishtest, for instance
	 * {@code /usr/local/bin/varnishtest}.
	 */
	@Parameter(defaultValue = "varnishtest", property = "varnishtest.varnishtestCommand")
	private String varnishtestCommand;

	/**
	 * The command to run for varnishd, for instance {@code /usr/sbin/varnishd}
	 * for unprivileged users without {@code /usr/sbin} in their {@code PATH}.
	 */
	@Parameter(defaultValue = "varnishd", property = "varnishtest.varnishdCommand")
	private String varnishdCommand;

	/**
	 * A list of {@code<include>} elements specifying the tests (by pattern)
	 * that should be included in testing. When not specified and when the
	 * {@code test} parameter is not specified, the default includes will be
	 * <pre> {@code<includes>
	 *   <include>src/test/varnish/**.vtc</include>
	 * </includes>}</pre>
	 * <p/>
	 * Each include item may also contain a comma-separated sublist of items,
	 * which will be treated as multiple {@code<include>} entries.
	 */
	@Parameter
	private List<String> includes;

	/**
	 * A list of {@code<exclude>} elements specifying the tests (by pattern)
	 * that should be excluded in testing. When not specified and when the
	 * {@code test} parameter is not specified, the default excludes will be
	 * <pre>{@code<excludes/>}</pre>
	 * (which excludes nothing).
	 * <p/>
	 * Each exclude item may also contain a comma-separated sublist of items,
	 * which will be treated as multiple {@code<exclude>} entries.
	 */
	@Parameter
	private List<String> excludes;

	/**
	 * List of macros to pass to varnishtest.
	 */
	@Parameter
	private Map<String, String> macros;

	/**
	 * Per-test timeout in seconds.
	 */
	@Parameter(defaultValue = "20", property = "varnishtest.timeout")
	private int timeout;

	/**
	 * Set this to 'true' to bypass varnish tests entirely.
	 */
	@Parameter(defaultValue = "false", property = "varnishtest.skip")
	private boolean skip = false;

	/**
	 * Runs the tests.
	 * 
	 * @throws MojoExecutionException if communication with varnishtest failed
	 * @throws MojoFailureException if a test case fails
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		if ( isSkipped() ) {
			getLog().info("Varnish tests are skipped.");
			return;
		}

		createReportsDirectory();

		CommandLineBuilder builder = new CommandLineBuilder()
			.setVarnishtestCommand(varnishtestCommand)
			.setVarnishdCommand(varnishdCommand);

		if (macros != null) {
			builder.setMacros(macros);
		}

		int testCaseCounter = 1;
		LogOutputHandler handler = new LogOutputHandler(getLog());

		for (String testCase : getTestCases()) {
			VarnishtestRunner runner = new VarnishtestRunner(builder, workingDirectory);
			try {
				File testCaseFile = new File(getBasedir(), testCase);
				VarnishtestReport report = runner.runTestCase(testCaseFile, handler, timeout);
				writeReport(report, testCaseCounter);
			}
			catch (VarnishtestException e) {
				writeReport(e.getReport(), testCaseCounter);
				throw new MojoFailureException("Test failure : " + testCase, e);
			}
			catch (IOException e) {
				throw new MojoExecutionException("An error occured", e);
			}
			testCaseCounter++;
		}
	}

	private void createReportsDirectory() throws MojoExecutionException {
		reportsDirectory.mkdirs();

		if ( ! reportsDirectory.exists() ) {
			throw new MojoExecutionException("Varnishtest reports directory not created : " + reportsDirectory);
		}
	}

	private void writeReport(VarnishtestReport report, int number) throws MojoExecutionException {
		try {
			report.write(reportsDirectory, number);
		}
		catch (IOException e) {
			throw new MojoExecutionException("Could not write varnishtest's report", e);
		}
	}

	private boolean isSkipped() {
		return skip || isPropertyTrueOrEmpty("skipITs") || isPropertyTrueOrEmpty("maven.test.skip");
	}

	private boolean isPropertyTrueOrEmpty(String property) {
		return Boolean.getBoolean(property) || "".equals(System.getProperty(property));
	}

	private String[] getTestCases() {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(getBasedir());
		scanner.setIncludes(getIncludes());
		scanner.setExcludes(getExcludes());
		scanner.scan();

		String[] testCases = scanner.getIncludedFiles();
		return testCases != null ? testCases : EMPTY_STRING_ARRAY;
	}

	private File getBasedir() {
		return project.getBasedir();
	}

	private String[] getExcludes() {
		if (excludes == null || excludes.isEmpty()) {
			return EMPTY_STRING_ARRAY;
		}
		return excludes.toArray(EMPTY_STRING_ARRAY);
	}

	private String[] getIncludes() {
		if (includes == null || includes.isEmpty()) {
			return DEFAULT_INCLUDE;
		}
		return includes.toArray(EMPTY_STRING_ARRAY);
	}
}
