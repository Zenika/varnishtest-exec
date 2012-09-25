package com.zenika.plugins.varnishtest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class VarnishtestReport {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^\\*\\s+top\\s+\\d+\\.\\d+ TEST Test (.+)$");
	private static final Pattern STATUS_PATTERN = Pattern.compile("^\\#\\s+top\\s+TEST.+?(FAILED|passed).+$");
	private static final Pattern DURATION_PATTERN = Pattern.compile("^\\#\\s+top\\s+TEST.+\\w+\\s+?\\((.+)\\).*$");
	
	private static final String SUCCCESS = "passed";
	
	private final List<String> logs;
	private String title = null;
	private String duration = null;
	private String success = null;
	
	VarnishtestReport() {
		this.logs = new ArrayList<String>();
	}
	
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
	
	String getTitle() {
		return title;
	}

	String getDuration() {
		return duration;
	}

	boolean getSuccess() {
		return SUCCCESS.equals(success);
	}
}
