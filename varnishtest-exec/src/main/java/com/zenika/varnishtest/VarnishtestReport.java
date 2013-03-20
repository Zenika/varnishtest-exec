package com.zenika.varnishtest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The varnishtest report class grabs information about test cases from
 * varnishtest output.
 * 
 * @author Dridi Boukelmoune
 */
public class VarnishtestReport {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^\\*\\s+top\\s+\\d+\\.\\d+ TEST (.+?)( starting)?(?<! starting)$");
	private static final Pattern STATUS_PATTERN = Pattern.compile("^\\#\\s+top\\s+TEST.+?(FAILED|passed).+$");
	private static final Pattern DURATION_PATTERN = Pattern.compile("^\\#\\s+top\\s+TEST.+\\w+\\s+?\\((.+)\\).*$");
	
	private static final String SUCCCESS = "passed";
	private static final String DEFAULT_FILENAME_FORMAT = "TEST-%04d-%s.log";
	
	private final List<String> logs = new ArrayList<String>();
	private String title = null;
	private String duration = null;
	private String success = null;
	
	/**
	 * Gathers a line of text from varnishtest output.
	 * @param line Varnishtest output
	 */
	void log(String line) {
		logs.add(line);
		title    = checkPattern(line, TITLE_PATTERN,    title);
		duration = checkPattern(line, DURATION_PATTERN, duration);
		success  = checkPattern(line, STATUS_PATTERN,   success);
	}

	private String checkPattern(String line, Pattern pattern, String currentValue) {
		if (currentValue != null) {
			return currentValue;
		}
		Matcher matcher = pattern.matcher(line);
		return matcher.matches() ? matcher.group(1) : null;
	}
	
	/**
	 * @return The test case's title or {@code null}
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @return The test case's duration in seconds or {@code null}
	 */
	public String getDuration() {
		return duration;
	}
	
	/**
	 * @return {@code true} if the test case succeeded
	 */
	public boolean getSuccess() {
		return SUCCCESS.equals(success);
	}

	/**
	 * Writes the report in a file. The file name is generated from a default
	 * template that takes a {@code number} which represents the varnishtest
	 * execution number in a serial execution. This is a facility for easy
	 * file naming that would avoid potential naming collisions and be
	 * deterministic given the same execution order.
	 * @param directory The directory where the file is written
	 * @param number The execution order number
	 * @throws IOException If an I/O error occurs
	 * @see #write(Appendable)
	 */
	public void write(File directory, int number) throws IOException {
		if (directory == null) {
			throw new NullPointerException("Null directory");
		}
		String filename = String.format(DEFAULT_FILENAME_FORMAT, number, title).replace(' ', '_');
		FileWriter writer = null;
		try {
			File reportFile = new File(directory, filename);
			writer = new FileWriter(reportFile);
			write(writer);
		}
		finally {
			try {
				if (writer != null) {
					writer.close();
				}
			}
			catch (IOException e) {}
		}
	}

	/**
	 * Writes the output of the varnishtest execution.
	 * @param appendable The object receiving the contents.
	 * @throws IOException If an I/O error occurs
	 */
	public void write(Appendable appendable) throws IOException {
		if (appendable == null) {
			throw new NullPointerException("Null appendable");
		}
		for (String line : logs) {
			appendable.append(line).append('\n');
		}
	}
}
