package com.zenika.varnishtest;

import org.apache.commons.exec.ExecuteException;


public class VarnishtestException extends Exception {
	private static final long serialVersionUID = 5646021953346729687L;
	
	private final VarnishtestReport report;
	
	VarnishtestException(VarnishtestReport report, ExecuteException cause) {
		super(cause);
		this.report = report;
	}
	
	public VarnishtestReport getReport() {
		return report;
	}
}
