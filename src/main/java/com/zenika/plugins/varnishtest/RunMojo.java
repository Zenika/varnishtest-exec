package com.zenika.plugins.varnishtest;

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
	 * Base directory where all reports are written to.
	 * TODO write reports
	 */
	@Parameter(defaultValue = "${project.build.directory}/varnishtest-reports", property = "varnishtest.reportsDirectory")
	private File reportsDirectory;
	
	/**
	 * The command to run varnishtest.
	 */
	@Parameter(defaultValue = "varnishtest", property = "varnishtest.varnishtestCommand")
	private String varnishtestCommand;
	
	/**
	 * The command to run varnishd.
	 */
	@Parameter(defaultValue = "varnishd", property = "varnishtest.varnishdCommand")
	private String varnishdCommand;
	
	/**
	 * A list of &lt;include> elements specifying the tests (by pattern) that
	 * should be included in testing. When not specified and when the
	 * <code>test</code> parameter is not specified, the default includes will
	 * be <code><br/>
	 * &lt;includes&gt;<br/>
	 * &nbsp;&lt;include&gt;src/test/varnish/**.vtc&lt;/include&gt;<br/>
	 * &lt;/includes&gt;<br/>
	 * </code>
	 * <p/>
	 * Each include item may also contain a comma-separated sublist of items,
	 * which will be treated as multiple &nbsp;&lt;include> entries.<br/>
	 * <p/>
	 */
	@Parameter
	private List<String> includes;
	
	/**
	 * A list of &lt;exclude> elements specifying the tests (by pattern) that
	 * should be excluded in testing. When not specified and when the
	 * <code>test</code> parameter is not specified, the default excludes will
	 * be <code><br/>
	 * &lt;excludes/&gt;<br/>
	 * </code>(which excludes nothing).<br>
	 * <p/>
	 * Each exclude item may also contain a comma-separated sublist of items,
	 * which will be treated as multiple &nbsp;&lt;exclude> entries.<br/>
	 */
	@Parameter
	private List<String> excludes;
	
	/**
	 * List of macros to pass to varnishtest.
	 */
	@Parameter
	private Map<String, String> macros;
	
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
		
		reportsDirectory.mkdirs();
		
		CommandLineBuilder builder = new CommandLineBuilder()
			.setVarnishtestCommand(varnishtestCommand)
			.setVarnishdCommand(varnishdCommand);
		
		if (macros != null) {
			builder.setMacros(macros);
		}
		
		LogOutputHandler handler = new LogOutputHandler(getLog());
		
		for (String testCase : getTestCases()) {
			VarnishtestRunner runner = new VarnishtestRunner(builder);
			try {
				runner.runTestCase(new File(getBasedir(), testCase), handler, 20); // FIXME make timeout configurable
				// TODO report
			}
			catch (VarnishtestException e) {
				// TODO report
				throw new MojoFailureException("Test failure : " + testCase, e);
			}
			catch (IOException e) {
				throw new MojoExecutionException("An error occured", e);
			}
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
