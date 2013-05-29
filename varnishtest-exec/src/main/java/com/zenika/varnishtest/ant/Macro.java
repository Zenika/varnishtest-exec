package com.zenika.varnishtest.ant;

import com.zenika.varnishtest.CommandLineBuilder;

/**
 * Representation of a single varnishtest macro.
 *
 * @author Dridi Boukelmoune
 * @ant.element
 */
public class Macro {

	private String name;
	private String value;

	/**
	 * Add the macro to a varnishtest command-line.
	 * @param builder The command-line builder
	 */
	void addTo(CommandLineBuilder builder) {
		builder.setMacro(name, value);
	}

	/**
	 * Sets the name of the macro.
	 * <p>The name should be unique or else this macro will be overridden.</p>
	 * @param name The name of the macro
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the value of the macro.
	 * @param value The value of the macro
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
