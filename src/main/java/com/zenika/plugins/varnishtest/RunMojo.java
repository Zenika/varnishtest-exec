package com.zenika.plugins.varnishtest;

import java.io.File;
import java.util.List;

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

@Mojo(name = "run", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class RunMojo extends AbstractMojo {

	private static final String[] EMPTY_STRING_ARRAY = new String[] {};
	private static final String[] DEFAULT_INCLUDE = new String[] {"src/test/varnish/**.vtc"};
	
	@Component
	private MavenProject project;
	
	@Parameter(defaultValue = "${project.build.directory}/varnishtest")
	private File outputDirectory;
	
	@Parameter(defaultValue = "varnishtest")
	private String varnishtestCommand;
	
	@Parameter(defaultValue = "varnishd")
	private String varnishdCommand;
    
	@Parameter
	private List<String> includes;
	
    @Parameter
    protected List<String> excludes;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		outputDirectory.mkdirs();
		
		CommandLine commandLine = new CommandLine(varnishtestCommand);
		addMacro(commandLine, "varnishd", varnishdCommand);
		addMacro(commandLine, "project.basedir", getBasedir());
		
		for (String testCase : getTestCases()) {
			VarnishtestRunner runner = new VarnishtestRunner(commandLine);
			runner.runTestCase(this, new File(getBasedir(), testCase), 20);
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
	
	private void addMacro(CommandLine commandLine, String name, Object value) {
		commandLine.addArgument("-D" + name + "=" + value);
	}
}
