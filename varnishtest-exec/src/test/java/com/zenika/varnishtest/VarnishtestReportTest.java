package com.zenika.varnishtest;

import org.junit.Assert;
import org.junit.Test;

import com.zenika.varnishtest.VarnishtestReport;


public class VarnishtestReportTest {
	
	@Test
	public void shouldGrabTheTestTitle() {
		VarnishtestReport report = new VarnishtestReport();
		report.log("*    top   0.0 TEST Test The test case title");
		Assert.assertEquals("The test case title", report.getTitle());
	}
	
	@Test
	public void shouldGrabTheTestDuration() {
		VarnishtestReport report = new VarnishtestReport();
		report.log("#     top  TEST /path/to/test.vtc passed (0.666)");
		Assert.assertEquals("0.666", report.getDuration());
	}
	
	@Test
	public void shouldGrabTheTestDurationOnFailure() {
		VarnishtestReport report = new VarnishtestReport();
		report.log("#     top  TEST /path/to/test.vtc FAILED (0.666) exit=1");
		Assert.assertEquals("0.666", report.getDuration());
	}
	
	@Test
	public void shouldGrabTheStatus() {
		VarnishtestReport report = new VarnishtestReport();
		report.log("#     top  TEST /path/to/test.vtc passed (0.666)");
		Assert.assertTrue( report.getSuccess() );
	}
	
	@Test
	public void shouldGrabTheStatusOnFailure() {
		VarnishtestReport report = new VarnishtestReport();
		report.log("#     top  TEST /path/to/test.vtc FAILED (0.666) exit=1");
		Assert.assertFalse( report.getSuccess() );
	}
}
