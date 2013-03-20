package com.zenika.varnishtest;


/**
 * Thrown to indicate that a varnishtest execution ended with a non-zero exit
 * status. It means that the test failed somehow, but not the reason.
 * @author Dridi Boukelmoune
 */
public class VarnishtestException extends Exception {
	private static final long serialVersionUID = -3499852196891155657L;

	private final transient VarnishtestReport report;

	/**
	 * Constructor.
	 * @param report the report of the test execution
	 * @param cause the cause
	 */
	VarnishtestException(VarnishtestReport report, Throwable cause) {
		super(cause);
		this.report = report;
	}

	/**
	 * Returns the varnishtest execution report.
	 * @return the report
	 */
	public VarnishtestReport getReport() {
		return report;
	}
}
