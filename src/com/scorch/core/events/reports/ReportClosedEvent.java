package com.scorch.core.events.reports;

import com.scorch.core.modules.report.Report;

public class ReportClosedEvent extends ReportEvent {

	public ReportClosedEvent(Report report) {
		super(report);
	}

}
