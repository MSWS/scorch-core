package com.scorch.core.events.reports;

import com.scorch.core.modules.report.Report;

public class ReportCreateEvent extends ReportEvent {

	public ReportCreateEvent(Report report) {
		super(report);
	}

}
