package com.zenika.plugins.varnishtest;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "run", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class RunMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "${project.build.directory}/varnishtest")
	private File outputDirectory;
	
	public void execute() throws MojoExecutionException {
		outputDirectory.mkdirs();
	}
}
