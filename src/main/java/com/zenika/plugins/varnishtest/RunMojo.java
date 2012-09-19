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
	
	@Parameter(defaultValue = "${project.build.directory}/varnishtest", property = "varnishtest.outputDirectory")
	private File outputDirectory;
	
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
		
		outputDirectory.mkdirs();
		
		CommandLine commandLine = new CommandLine(varnishtestCommand);
		addMacro(commandLine, "varnishd", varnishdCommand);
		addMacro(commandLine, "project.basedir", getBasedir());
		addMacros(commandLine);
		
		for (String testCase : getTestCases()) {
			VarnishtestRunner runner = new VarnishtestRunner(commandLine);
			runner.runTestCase(this, new File(getBasedir(), testCase), 20);
		}
		
	}
	
	private void addMacro(CommandLine commandLine, String name, Object value) {
		commandLine.addArgument("-D" + name + "=" + value);
	}
	
	private void addMacros(CommandLine commandLine) {
		if (macros == null) {
			return;
		}
		
		// TODO validate macros map against hard coded keys
		
		for (Map.Entry<String, String> macro : macros.entrySet()) {
			addMacro(commandLine, macro.getKey(), macro.getValue());
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
