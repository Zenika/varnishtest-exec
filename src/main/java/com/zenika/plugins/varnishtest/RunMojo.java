package com.zenika.plugins.varnishtest;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

@Mojo(name = "run",
		threadSafe = false,
		requiresProject = true,
		defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class RunMojo extends AbstractMojo {
	
	private static final String[] EMPTY_STRING_ARRAY = new String[] {};
	private static final String[] DEFAULT_INCLUDE = new String[] { "src/test/varnish/**.vtc" };
	
	@Component
	private MavenProject project;
	
	@Parameter(defaultValue = "${project.build.directory}/varnishtest-reports", property = "varnishtest.reportsDirectory")
	private File reportsDirectory;
	
	@Parameter(defaultValue = "varnishtest", property = "varnishtest.varnishtestCommand")
	private String varnishtestCommand;
	
	@Parameter(defaultValue = "varnishd", property = "varnishtest.varnishdCommand")
	private String varnishdCommand;
	
	@Parameter
	private List<String> includes;
	
	@Parameter
	private List<String> excludes;
	
	@Parameter
	private Map<String, String> macros;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		reportsDirectory.mkdirs();
		
		CommandLine commandLine = new CommandLine(varnishtestCommand);
		commandLine.addArgument("-v");
		
		addMacro(commandLine, "varnishd", varnishdCommand);
		addMacro(commandLine, "project.basedir", getBasedir());
		addMacros(commandLine);
		
		for (String testCase : getTestCases()) {
			VarnishtestRunner runner = new VarnishtestRunner(commandLine);
			try {
				runner.runTestCase(getLog(), new File(getBasedir(), testCase), 20);
			}
			finally {
				// TODO report
			}
		}
	}
	
	private void addMacro(CommandLine commandLine, String name, Object value) throws MojoFailureException {
		if (macros != null && macros.containsKey(name)) {
			throw new MojoFailureException("The macro `" + name + "' is not allowed");
		}
		commandLine.addArgument("-D" + name + "=" + value, false);
	}
	
	private void addMacros(CommandLine commandLine) {
		if (macros == null) {
			return;
		}
		
		for (Map.Entry<String, String> macro : macros.entrySet()) {
			commandLine.addArgument("-D" + macro.getKey() + "=" + macro.getValue(), false);
		}
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
