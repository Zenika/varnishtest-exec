package com.zenika.varnishtest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class ReservedMacrosTest {

	private String macroName;

	public ReservedMacrosTest(String macroName) {
		this.macroName = macroName;
	}

	@Parameters
	public static List<String[]> reservedMacros() {
		return Arrays.asList(new String[][]{{"varnishd"}, {"pwd"}, {"tmpdir"}, {"bad_ip"}});
	}

	@Test(expected = IllegalArgumentException.class)
	public void settingReservedMacrosShouldFail() {
		new CommandLineBuilder().setMacro(macroName, "value");
	}
}
