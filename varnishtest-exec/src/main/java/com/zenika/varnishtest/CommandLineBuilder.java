package com.zenika.varnishtest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;

/**
 * This class allows to build a proper command line for varnishtest execution.
 * The test case (vtc) file that should be part of the command line is expected
 * in the {@link VarnishtestRunner} class.
 * 
 * <p>
 * Varnishtest accepts macros, but the following name cannot be set :
 * <ul>
 *  <li>varnishd</li>
 * </ul>
 * Macro names are validated against this regex : {@value #MACRO_NAME_REGEX}
 * </p>
 * 
 * @author Dridi Boukelmoune
 * @see VarnishtestRunner#VarnishtestRunner(CommandLineBuilder)
 * @see VarnishtestRunner#runTestCase(java.io.File)
 */
public class CommandLineBuilder {
	
	private static final String MACRO_NAME_REGEX = "\\w+(\\.\\w+)*";
	private static final Pattern MACRO_NAME_PATTERN = Pattern.compile(MACRO_NAME_REGEX);

	private String varnishtestCommand = "varnishtest";
	private String varnishdCommand = "varnishd";
	
	private final LinkedHashMap<String, String> macros = new LinkedHashMap<String, String>();

	/**
	 * Constructor.
	 */
	public CommandLineBuilder() {
	}

	/**
	 * Handy copy constructor.
	 * @param builder the command line builder to copy
	 */
	public CommandLineBuilder(CommandLineBuilder builder) {
		if (builder == null) {
			throw new NullPointerException("Null builder");
		}
		this.varnishtestCommand = builder.varnishtestCommand;
		this.varnishdCommand = builder.varnishdCommand;
		this.macros.putAll(builder.macros);
	}

	/**
	 * Returns the varnishtest command, the default is {@code "varnishtest"}.
	 * @return the varnishtest command
	 */
	public String getVarnishtestCommand() {
		return varnishtestCommand;
	}

	/**
	 * Set a non-empty command line for varnishtest.
	 * @param varnishtestCommand the varnishtest command
	 * @return {@code this} builder
	 * @throws IllegalArgumentException if the command is {@code null} or empty
	 */
	public CommandLineBuilder setVarnishtestCommand(String varnishtestCommand) {
		if (varnishtestCommand == null || varnishtestCommand.isEmpty()) {
			throw new IllegalArgumentException("Null or empty varnishtest command");
		}
		this.varnishtestCommand = varnishtestCommand;
		return this;
	}

	/**
	 * Returns the varnishd command, the default is {@code "varnishd"}.
	 * @return the varnishd command
	 */
	public String getVarnishdCommand() {
		return varnishdCommand;
	}

	/**
	 * Set a non-empty command line for varnishd. This might be needed for
	 * unprivileged users without {@code /usr/sbin} in their {@code PATH}. In
	 * that case, instead of running a plain {@code varnishd} command,
	 * varnishtest could run {@code /usr/sbin/varnishd}.
	 * @param varnishdCommand the varnishd command
	 * @return {@code this} builder
	 * @throws IllegalArgumentException if the command is {@code null} or empty
	 */
	public CommandLineBuilder setVarnishdCommand(String varnishdCommand) {
		if (varnishdCommand == null || varnishdCommand.isEmpty()) {
			throw new NullPointerException("Null or empty varnishd command");
		}
		this.varnishdCommand = varnishdCommand;
		return this;
	}

	/**
	 * Returns a set of the existing macros names.
	 * @return a set of the macros names
	 */
	public Set<String> getMacros() {
		return Collections.unmodifiableSet( macros.keySet() );
	}
	
	/**
	 * Returns the value of a macro.
	 * @param name the name of the macro
	 * @return the value of the macro
	 * @throws NullPointerException if {@code name} is {@code null}
	 * @throws IllegalArgumentException if {@code name} does not exist
	 */
	public String getMacro(String name) {
		if (name == null) {
			throw new NullPointerException("Null name");
		}
		if ( ! macros.containsKey(name) ) {
			throw new IllegalArgumentException("Unknown macro : " + name);
		}
		return macros.get(name);
	}
	
	/**
	 * Sets a collection of macros.
	 * @param macros the macros to set
	 * @return {@code this} builder
	 * @throws NullPointerException if {@code macros} is {@code null}
	 * @throws NullPointerException if {@code macros} contains a {@code null} name or value
	 * @throws IllegalArgumentException if {@code macros} contains a reserved name
	 * @throws IllegalArgumentException if {@code macros} contains an invalid name
	 */
	public CommandLineBuilder setMacros(Map<String, String> macros) {
		if (macros == null) {
			throw new NullPointerException("Null macros");
		}
		for (Entry<String, String> entry : macros.entrySet()) {
			setMacro(entry.getKey(), entry.getValue());
		}
		return this;
	}
	
	/**
	 * Sets the value of a macro.
	 * @param name the name of the macro
	 * @param value the value of the macro
	 * @return {@code this} builder
	 * @throws NullPointerException if {@code name} or {@code value} is {@code null}
	 * @throws IllegalArgumentException if {@code name} is reserved
	 * @throws IllegalArgumentException if {@code name} is not valid
	 */
	public CommandLineBuilder setMacro(String name, String value) {
		if (name == null || value == null) {
			throw new NullPointerException("Null name or value");
		}
		if ( "varnishd".equals(name) ) { // the only reserved macro so far
			throw new IllegalArgumentException("The macro `varnishd' is not allowed");
		}
		if ( MACRO_NAME_PATTERN.matcher(name).matches() ) {
			macros.put(name, value);
			return this;
		}
		throw new IllegalArgumentException("Invalid macro name : " + name);
	}
	
	/**
	 * Removes all macros previously set.
	 * @return {@code this} builder
	 */
	public CommandLineBuilder clearMacros() {
		macros.clear();
		return this;
	}
	
	/**
	 * Create a commons-exec {@link CommandLine}.
	 * @return the command line
	 */
	CommandLine toCommandLine() {
		CommandLine commandLine = new CommandLine(varnishtestCommand);
		commandLine.addArgument("-v"); // we need the verbose flag for the report
		
		commandLine.addArgument("-Dvarnishd=" + varnishdCommand, false);
		
		for (Map.Entry<String, String> macro : macros.entrySet()) {
			commandLine.addArgument("-D" + macro.getKey() + "=" + macro.getValue(), false);
		}

		return commandLine;
	}

	@Override
	public String toString() {
		return toCommandLine().toString();
	}
}
